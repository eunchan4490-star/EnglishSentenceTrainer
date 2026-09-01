package com.example.englishsentencetrainer.japanese

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.google.mlkit.vision.digitalink.recognition.RecognitionContext
import com.google.mlkit.vision.digitalink.recognition.WritingArea

class JapaneseHandwritingManager {
    private val model = DigitalInkRecognitionModel.builder(
        requireNotNull(DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP"))
    ).build()
    private val recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build())

    fun recognize(ink: Ink, width: Float, height: Float, onSuccess: (List<String>) -> Unit, onError: () -> Unit) {
        RemoteModelManager.getInstance().download(model, DownloadConditions.Builder().build())
            .continueWithTask {
                val context = RecognitionContext.builder()
                    .setWritingArea(WritingArea(width, height))
                    .build()
                recognizer.recognize(ink, context)
            }
            .addOnSuccessListener { result -> onSuccess(result.candidates.map { it.text }) }
            .addOnFailureListener { onError() }
    }

    fun close() = recognizer.close()
}
