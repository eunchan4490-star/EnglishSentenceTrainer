package com.example.englishsentencetrainer.data

import android.content.Context

class LocalTextRepository(context: Context) {
    private val preferences = context.getSharedPreferences("saved_study", Context.MODE_PRIVATE)

    fun load(): String = preferences.getString(KEY_TEXT, "").orEmpty()

    fun save(text: String) {
        preferences.edit().putString(KEY_TEXT, text.trim()).apply()
    }

    private companion object {
        const val KEY_TEXT = "latest_confirmed_text"
    }
}
