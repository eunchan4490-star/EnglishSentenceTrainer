package com.example.englishsentencetrainer.ocr

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionManager {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognize(imageProxy: ImageProxy, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            onError(IllegalStateException("촬영 이미지를 읽을 수 없습니다."))
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { onSuccess(it.text) }
            .addOnFailureListener(onError)
            .addOnCompleteListener { imageProxy.close() }
    }

    fun close() = recognizer.close()
}
