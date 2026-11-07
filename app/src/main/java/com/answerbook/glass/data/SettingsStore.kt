package com.answerbook.glass.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class LlmConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val model: String = DEFAULT_MODEL,
    val apiKey: String = ""
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && model.isNotBlank() && apiKey.isNotBlank()

    companion object {
        const val DEFAULT_BASE_URL = "https://api.deepseek.com/v1"
        const val DEFAULT_MODEL = "deepseek-chat"
    }
}

class SettingsStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var config: LlmConfig by mutableStateOf(read())
        private set

    fun save(baseUrl: String, model: String, apiKey: String) {
        val next = LlmConfig(
            baseUrl = baseUrl.trim().trimEnd('/'),
            model = model.trim(),
            apiKey = apiKey.trim()
        )
        preferences.edit()
            .putString(KEY_BASE_URL, next.baseUrl)
            .putString(KEY_MODEL, next.model)
            .putString(KEY_API_KEY, next.apiKey)
            .apply()
        config = next
    }

    fun clearKey() {
        preferences.edit().remove(KEY_API_KEY).apply()
        config = config.copy(apiKey = "")
    }

    private fun read(): LlmConfig = LlmConfig(
        baseUrl = preferences.getString(KEY_BASE_URL, LlmConfig.DEFAULT_BASE_URL)
            ?: LlmConfig.DEFAULT_BASE_URL,
        model = preferences.getString(KEY_MODEL, LlmConfig.DEFAULT_MODEL)
            ?: LlmConfig.DEFAULT_MODEL,
        apiKey = preferences.getString(KEY_API_KEY, "") ?: ""
    )

    private companion object {
        const val PREFS_NAME = "answer_book_settings"
        const val KEY_BASE_URL = "llm_base_url"
        const val KEY_MODEL = "llm_model"
        const val KEY_API_KEY = "llm_api_key"
    }
}
