package com.example.englishsentencetrainer.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.example.englishsentencetrainer.japanese.JapaneseTextSanitizer

enum class OcrMode { ENGLISH, JAPANESE }

class TextRecognitionManager(private val mode: OcrMode = OcrMode.ENGLISH) {
    private val recognizer = when (mode) {
        OcrMode.ENGLISH -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        OcrMode.JAPANESE -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }

    fun recognize(bitmap: Bitmap, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener {
                onSuccess(if (mode == OcrMode.ENGLISH) extractEnglishText(it) else extractJapaneseText(it))
            }
            .addOnFailureListener(onError)
    }

    private fun extractEnglishText(result: Text): String = result.textBlocks
        .flatMap { it.lines }
        .mapNotNull { line ->
            line.elements
                .map { it.text.trim() }
                .filter(::isEnglishElement)
                .joinToString(" ")
                .takeIf(String::isNotBlank)
        }
        .joinToString("\n")

    private fun isEnglishElement(value: String): Boolean {
        if (value.none { it in 'A'..'Z' || it in 'a'..'z' }) return false
        if (value.any { it in '\uAC00'..'\uD7A3' }) return false
        val letters = value.count { it in 'A'..'Z' || it in 'a'..'z' }
        val digits = value.count(Char::isDigit)
        if (digits > 0 && letters > 0 && !value.matches(Regex("[A-Za-z]+-?\\d+[.,!?]?"))) return false
        val unusual = value.count { !it.isLetterOrDigit() && it !in "'’.,!?;:()-/\"" }
        return unusual == 0 && letters.toFloat() / value.length >= 0.45f
    }

    private fun extractJapaneseText(result: Text): String = JapaneseTextSanitizer.cleanLines(
        result.textBlocks.flatMap { block -> block.lines.map { it.text } }
    )

    fun close() = recognizer.close()
}
