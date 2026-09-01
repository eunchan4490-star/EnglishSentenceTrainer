package com.example.englishsentencetrainer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.englishsentencetrainer.ocr.TextRecognitionManager
import com.example.englishsentencetrainer.ocr.OcrMode
import kotlin.math.roundToInt

@Composable
fun CameraScreen(onRecognized: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit, mode: OcrMode = OcrMode.ENGLISH) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }

    if (!granted) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("본문을 촬영하려면 카메라 권한이 필요합니다.", fontSize = 20.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("카메라 권한 허용") }
            AppNavigationButtons(onBack, onHome)
        }
    } else {
        CameraPreview(onRecognized, onBack, onHome, mode)
    }
}

@Composable
private fun CameraPreview(onRecognized: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit, mode: OcrMode) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val recognizer = remember(mode) { TextRecognitionManager(mode) }
    var processing by remember { mutableStateOf(false) }
    var awaitingNextAction by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val recognizedPages = remember { mutableStateListOf<String>() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            capturedBitmap?.recycle()
            if (cameraProviderFuture.isDone) cameraProviderFuture.get().unbindAll()
            recognizer.close()
        }
    }

    capturedBitmap?.let { bitmap ->
        CropSelectionScreen(
            bitmap = bitmap,
            pageNumber = recognizedPages.size + 1,
            processing = processing,
            onRetake = {
                bitmap.recycle()
                capturedBitmap = null
                error = null
            },
            onUseCrop = { left, top, right, bottom ->
                processing = true
                error = null
                val cropped = cropBitmap(bitmap, left, top, right, bottom)
                recognizer.recognize(
                    cropped,
                    onSuccess = { text ->
                        processing = false
                        cropped.recycle()
                        bitmap.recycle()
                        capturedBitmap = null
                        if (text.isBlank()) error = "선택 영역에서 ${if (mode == OcrMode.ENGLISH) "영어 본문" else "일본어 단어"}를 찾지 못했습니다. 다시 촬영하세요."
                        else {
                            recognizedPages.add(text.trim())
                            awaitingNextAction = true
                        }
                    },
                    onError = {
                        processing = false
                        cropped.recycle()
                        error = "문자 인식에 실패했습니다. 영역을 조절하거나 다시 촬영하세요."
                    }
                )
            }
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.align(Alignment.Center).fillMaxWidth(0.9f).fillMaxHeight(0.68f)
                .border(BorderStroke(2.dp, Color.White), MaterialTheme.shapes.small)
        )
        Row(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            AppNavigationButtons(onBack, onHome, Color.White)
        }
        Text(
            if (awaitingNextAction) "OCR 완료" else "페이지가 흰 선 안에 들어오게 촬영하세요",
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp))
        Column(Modifier.align(Alignment.BottomCenter).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "사진 ${if (awaitingNextAction) recognizedPages.size else recognizedPages.size + 1} / 10",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            error?.let { Text(it, color = Color.White) }
            if (awaitingNextAction) {
                Button(
                    onClick = { awaitingNextAction = false; error = null },
                    enabled = recognizedPages.size < 10,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text(if (recognizedPages.size < 10) "다음 부분 촬영" else "최대 10장 촬영 완료", fontSize = 18.sp) }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onRecognized(recognizedPages.joinToString("\n")) },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("촬영 완료", fontSize = 18.sp) }
                TextButton(onClick = {
                    recognizedPages.removeAt(recognizedPages.lastIndex)
                    awaitingNextAction = false
                    error = null
                }) { Text("이 사진 다시 찍기", color = Color.White, fontSize = 17.sp) }
            } else {
                Button(
                    onClick = {
                        processing = true; error = null
                        imageCapture.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                try {
                                    val source = image.toBitmap()
                                    capturedBitmap = rotateBitmap(source, image.imageInfo.rotationDegrees)
                                    if (capturedBitmap !== source) source.recycle()
                                    processing = false
                                } catch (_: Exception) {
                                    processing = false
                                    error = "촬영 이미지를 열지 못했습니다. 다시 촬영하세요."
                                } finally {
                                    image.close()
                                }
                            }
                            override fun onError(exception: ImageCaptureException) {
                                processing = false; error = "사진 촬영에 실패했습니다."
                            }
                        })
                    },
                    enabled = !processing && recognizedPages.size < 10,
                    modifier = Modifier.size(82.dp)
                ) { Text(if (processing) "인식 중" else "촬영") }
            }
        }
    }
}

private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return source
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun cropBitmap(source: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap {
    val x = (source.width * left).roundToInt().coerceIn(0, source.width - 1)
    val y = (source.height * top).roundToInt().coerceIn(0, source.height - 1)
    val cropRight = (source.width * right).roundToInt().coerceIn(x + 1, source.width)
    val cropBottom = (source.height * bottom).roundToInt().coerceIn(y + 1, source.height)
    return Bitmap.createBitmap(source, x, y, cropRight - x, cropBottom - y)
}
