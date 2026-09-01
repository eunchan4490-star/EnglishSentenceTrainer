package com.example.englishsentencetrainer.ui

import android.Manifest
import android.content.pm.PackageManager
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

@Composable
fun CameraScreen(onRecognized: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }

    if (!granted) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("본문을 촬영하려면 카메라 권한이 필요합니다.", fontSize = 20.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("카메라 권한 허용") }
            TextButton(onClick = onBack) { Text("처음으로") }
        }
    } else {
        CameraPreview(onRecognized, onBack)
    }
}

@Composable
private fun CameraPreview(onRecognized: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val recognizer = remember { TextRecognitionManager() }
    var processing by remember { mutableStateOf(false) }
    var awaitingNextAction by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val recognizedPages = remember { mutableStateListOf<String>() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            if (cameraProviderFuture.isDone) cameraProviderFuture.get().unbindAll()
            recognizer.close()
        }
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
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Text("← 취소", color = Color.White, fontSize = 18.sp)
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
                                recognizer.recognize(image,
                                    onSuccess = { text ->
                                        processing = false
                                        if (text.isBlank()) error = "영어 본문을 찾지 못했습니다. 다시 촬영하세요."
                                        else {
                                            recognizedPages.add(text.trim())
                                            awaitingNextAction = true
                                        }
                                    },
                                    onError = { processing = false; error = "문자 인식에 실패했습니다. 다시 촬영하세요." }
                                )
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
