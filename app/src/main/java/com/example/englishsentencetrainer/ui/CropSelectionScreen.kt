package com.example.englishsentencetrainer.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CropSelectionScreen(
    bitmap: Bitmap,
    pageNumber: Int,
    processing: Boolean,
    onRetake: () -> Unit,
    onUseCrop: (left: Float, top: Float, right: Float, bottom: Float) -> Unit
) {
    var left by remember(bitmap) { mutableFloatStateOf(0.05f) }
    var right by remember(bitmap) { mutableFloatStateOf(0.95f) }
    var top by remember(bitmap) { mutableFloatStateOf(0.05f) }
    var bottom by remember(bitmap) { mutableFloatStateOf(0.95f) }

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Text("사진 $pageNumber / 10 · 본문 영역 선택", fontSize = 21.sp)
        Text("노란 사각형 안쪽만 글자로 인식합니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "촬영한 페이지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Canvas(Modifier.fillMaxSize()) {
                val imageAspect = bitmap.width.toFloat() / bitmap.height
                val boxAspect = size.width / size.height
                val shownWidth: Float
                val shownHeight: Float
                if (imageAspect > boxAspect) {
                    shownWidth = size.width
                    shownHeight = size.width / imageAspect
                } else {
                    shownHeight = size.height
                    shownWidth = size.height * imageAspect
                }
                val imageLeft = (size.width - shownWidth) / 2f
                val imageTop = (size.height - shownHeight) / 2f
                val crop = Rect(
                    imageLeft + shownWidth * left,
                    imageTop + shownHeight * top,
                    imageLeft + shownWidth * right,
                    imageTop + shownHeight * bottom
                )
                val shade = Color.Black.copy(alpha = 0.55f)
                drawRect(shade, Offset(imageLeft, imageTop), Size(shownWidth, crop.top - imageTop))
                drawRect(shade, Offset(imageLeft, crop.bottom), Size(shownWidth, imageTop + shownHeight - crop.bottom))
                drawRect(shade, Offset(imageLeft, crop.top), Size(crop.left - imageLeft, crop.height))
                drawRect(shade, Offset(crop.right, crop.top), Size(imageLeft + shownWidth - crop.right, crop.height))
                drawRect(Color(0xFFFFC107), crop.topLeft, crop.size, style = Stroke(width = 5f))
            }
        }
        CropSlider("왼쪽", left, 0f..(right - 0.1f)) { left = it }
        CropSlider("오른쪽", right, (left + 0.1f)..1f) { right = it }
        CropSlider("위", top, 0f..(bottom - 0.1f)) { top = it }
        CropSlider("아래", bottom, (top + 0.1f)..1f) { bottom = it }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRetake, enabled = !processing, modifier = Modifier.weight(1f).height(56.dp)) {
                Text("다시 찍기", fontSize = 17.sp)
            }
            Button(
                onClick = { onUseCrop(left, top, right, bottom) },
                enabled = !processing,
                modifier = Modifier.weight(1.4f).height(56.dp)
            ) { Text(if (processing) "인식 중" else "이 영역 인식", fontSize = 17.sp) }
        }
    }
}

@Composable
private fun CropSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(48.dp))
        Slider(value = value, onValueChange = onChange, valueRange = range, modifier = Modifier.weight(1f))
    }
}
