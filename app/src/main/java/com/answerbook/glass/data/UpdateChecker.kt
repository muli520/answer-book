package com.answerbook.glass.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
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

enum class UpdateProvider(val label: String) {
    GITHUB("GitHub"),
    GITEE("Gitee")
}

data class UpdateConfig(
    val provider: UpdateProvider = UpdateProvider.GITHUB,
    val repository: String = ""
) {
    val isConfigured: Boolean
        get() = REPOSITORY_PATTERN.matches(repository.trim())

    companion object {
        private val REPOSITORY_PATTERN = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")
    }
}

data class UpdateInfo(
    val versionLabel: String,
    val notes: String,
    val releaseUrl: String,
    val apkUrl: String?
)

sealed interface UpdateResult {
    data class Available(val info: UpdateInfo) : UpdateResult
    data class UpToDate(val latestVersion: String) : UpdateResult
}

class UpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 检查 GitHub / Gitee 的 latest release。
 *
 * 只读取公开仓库的 Release，不涉及任何 API Key。
 * 有 APK 资产时返回直链；没有时仍然返回 Release 页面。
 */
class UpdateChecker {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun check(
        config: UpdateConfig,
        currentVersion: String
    ): UpdateResult {
        if (!config.isConfigured) {
            throw UpdateException("请先填写 owner/repo，例如 your-name/answer-book。")
        }

        val repository = config.repository.trim().trim('/')
        val url = when (config.provider) {
            UpdateProvider.GITHUB -> "https://api.github.com/repos/" + repository + "/releases/latest"
            UpdateProvider.GITEE -> "https://gitee.com/api/v5/repos/" + repository + "/releases/latest"
        }

        val request = Request.Builder()
            .url(url)
            .header("Accept", if (config.provider == UpdateProvider.GITHUB) {
                "application/vnd.github+json"
            } else {
                "application/json"
            })
            .header("User-Agent", "AnswerBook-Android")
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).await()
                response.use { parseResponse(it, config, repository, currentVersion) }
            } catch (e: UpdateException) {
                throw e
            } catch (e: UnknownHostException) {
                throw UpdateException("找不到更新服务器，请检查网络。", e)
            } catch (e: SocketTimeoutException) {
                throw UpdateException("检查更新超时，请稍后再试。", e)
            } catch (e: SSLException) {
                throw UpdateException("安全连接失败，请稍后再试。", e)
            } catch (e: IOException) {
                throw UpdateException("网络没有接上，请检查连接后重试。", e)
            }
        }
    }

    private fun parseResponse(
        response: Response,
        config: UpdateConfig,
        repository: String,
        currentVersion: String
    ): UpdateResult {
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            val detail = runCatching {
                JSONObject(body).optString("message")
            }.getOrNull()?.takeIf { it.isNotBlank() }
            val hint = when (response.code) {
                401, 403 -> "仓库不可访问，或接口触发了频率限制。"
                404 -> "没有找到这个仓库的 Release，请检查 owner/repo 是否正确、仓库是否公开。"
                429 -> "请求太频繁，请稍后再试。"
                in 500..599 -> "更新服务器暂时不可用（HTTP " + response.code + "）。"
                else -> "检查更新失败（HTTP " + response.code + "）。"
            }
            throw UpdateException(if (detail != null) hint + "\n" + detail else hint)
        }

        val json = runCatching { JSONObject(body) }.getOrNull()
            ?: throw UpdateException("更新服务器返回了无法解析的数据。")

        val tag = json.optString("tag_name").ifBlank { json.optString("name") }
        if (tag.isBlank()) throw UpdateException("这个 Release 没有版本号。")

        val versionLabel = tag.trim()
        val notes = json.optString("body").trim()
        val htmlUrl = json.optString("html_url").trim()
        val releaseUrl = when {
            htmlUrl.startsWith("https://") -> htmlUrl
            config.provider == UpdateProvider.GITHUB ->
                "https://github.com/" + repository + "/releases/tag/" + tag
            else -> "https://gitee.com/" + repository + "/releases/tag/" + tag
        }

        val apkUrl = findApkUrl(json.optJSONArray("assets"))

        return if (isNewer(versionLabel, currentVersion)) {
            UpdateResult.Available(
                UpdateInfo(
                    versionLabel = versionLabel,
                    notes = notes.take(500),
                    releaseUrl = releaseUrl,
                    apkUrl = apkUrl
                )
            )
        } else {
            UpdateResult.UpToDate(versionLabel)
        }
    }

    private fun findApkUrl(assets: JSONArray?): String? {
        if (assets == null) return null
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            val name = asset.optString("name")
            val url = asset.optString("browser_download_url")
            if (name.endsWith(".apk", ignoreCase = true) && url.startsWith("https://")) {
                return url
            }
        }
        return null
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val latestParts = versionParts(latest)
        val currentParts = versionParts(current)
        if (latestParts.isEmpty()) return false
        val size = maxOf(latestParts.size, currentParts.size)
        for (index in 0 until size) {
            val left = latestParts.getOrElse(index) { 0 }
            val right = currentParts.getOrElse(index) { 0 }
            if (left != right) return left > right
        }
        return false
    }

    private fun versionParts(value: String): List<Int> =
        Regex("\\d+").findAll(value)
            .mapNotNull { it.value.toIntOrNull() }
            .take(4)
            .toList()
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
