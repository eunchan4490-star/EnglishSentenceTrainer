package com.example.englishsentencetrainer.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslationManager {
    private val translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
    )

    fun translate(text: String, onSuccess: (String) -> Unit, onError: () -> Unit) {
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
            .continueWithTask { translator.translate(text) }
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener { onError() }
    }

    fun close() = translator.close()
}
