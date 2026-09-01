package com.example.englishsentencetrainer.japanese

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink

class JapaneseHandwritingManager {
    private val model = DigitalInkRecognitionModel.builder(DigitalInkRecognitionModelIdentifier.JA).build()
    private val recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build())

    fun recognize(ink: Ink, onSuccess: (List<String>) -> Unit, onError: () -> Unit) {
        try {
            RemoteModelManager.getInstance().download(model, DownloadConditions.Builder().build())
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception ?: IllegalStateException("Model download failed")
                    recognizer.recognize(ink)
                }
                .addOnSuccessListener { result -> onSuccess(result.candidates.map { it.text }) }
                .addOnFailureListener { onError() }
        } catch (_: Exception) {
            onError()
        }
    }

    fun close() = recognizer.close()
}
