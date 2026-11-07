package com.answerbook.glass.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LlmException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 极简 OpenAI 兼容客户端。
 *
 * 这里刻意不保存任何 messages 历史：每次请求仅包含
 * system prompt + 当前问题，从协议层保证“只进行一次对话”。
 */
class LlmClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun ask(
        config: LlmConfig,
        question: String,
        stance: AnswerStance,
        safety: SafetyCategory
    ): String {
        val url = chatCompletionsUrl(config.baseUrl)
        val payload = JSONObject().apply {
            put("model", config.model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", LlmPrompt.system(stance, safety))
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", LlmPrompt.userMessage(question, stance, safety))
                })
            })
            put("temperature", 1.0)
            put("top_p", 0.95)
            put("max_tokens", 120)
            put("frequency_penalty", 0.1)
            put("presence_penalty", 0.1)
            put("stream", false)
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + config.apiKey)
            .header("Accept", "application/json")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).await()
                response.use { parseResponse(it) }
            } catch (e: LlmException) {
                throw e
            } catch (e: UnknownHostException) {
                throw LlmException("找不到接口地址，请检查网络或 Base URL。", e)
            } catch (e: SocketTimeoutException) {
                throw LlmException("书页翻得太慢了，请稍后再试。", e)
            } catch (e: SSLException) {
                throw LlmException("安全连接失败，请检查接口地址是否正确。", e)
            } catch (e: IOException) {
                throw LlmException("网络没有接上，请检查连接后重试。", e)
            }
        }
    }

    private fun parseResponse(response: Response): String {
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            val detail = runCatching {
                JSONObject(body).optJSONObject("error")?.optString("message")
            }.getOrNull()?.takeIf { it.isNotBlank() }

            val hint = when (response.code) {
                401, 403 -> "密钥没有通过验证，请检查 API Key。"
                404 -> "接口地址或模型名不正确（404）。"
                429 -> "请求太频繁，请稍后再试。"
                in 500..599 -> "模型服务暂时不可用（HTTP " + response.code + "）。"
                else -> "请求失败（HTTP " + response.code + "）。"
            }
            throw LlmException(if (detail != null) hint + "\n" + detail else hint)
        }

        val content = runCatching {
            JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .opt("content")
        }.getOrNull() ?: throw LlmException("模型没有给出回答，请换一个模型再试。")

        val text = when (content) {
            is String -> content
            is JSONArray -> buildString {
                for (i in 0 until content.length()) {
                    val part = content.optJSONObject(i) ?: continue
                    val value = part.optString("text")
                    if (value.isNotBlank()) append(value)
                }
            }
            else -> content.toString()
        }

        return sanitize(text)
    }

    private fun sanitize(raw: String): String {
        var text = raw.trim().trim('"', '“', '”', '「', '」', '『', '』').trim()

        for (prefix in listOf("答案：", "答案:", "回答：", "回答:", "书页：", "书页:")) {
            if (text.startsWith(prefix)) {
                text = text.removePrefix(prefix).trim()
                break
            }
        }
        text = text
            .replace("*", "")
            .replace("_", "")
            .replace("#", "")
            .replace("\u0060", "")
            .replace(">", "")
            .trim()

        val firstBlock = text.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()

        val sentence = firstBlock.split('。', '！', '？', '；')
            .map { it.trim() }
            .firstOrNull { it.length >= 6 }
            ?: firstBlock

        var result = sentence.trim().trimEnd('，', '、', '：', ';', ',')
        if (result.isNotEmpty() && result.last() !in "。！？") result += "。"

        if (result.length > 46) {
            result = result.take(45).trimEnd('，', '、', '：', ';', ',') + "。"
        }
        return result.ifBlank { "书页空白，也是一种回答。" }
    }

    private fun chatCompletionsUrl(baseUrl: String): String {
        val base = baseUrl.trim().trimEnd('/')
        if (base.endsWith("/chat/completions")) return base
        return base + "/chat/completions"
    }
}

private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) continuation.resume(response)
            else response.close()
        }
    })
}
