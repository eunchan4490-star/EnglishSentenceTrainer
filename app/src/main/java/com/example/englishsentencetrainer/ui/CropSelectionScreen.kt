package com.example.englishsentencetrainer.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot

private enum class CropDrag { NONE, MOVE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

@Composable
fun CropSelectionScreen(bitmap: Bitmap, pageNumber: Int, processing: Boolean, onRetake: () -> Unit,
    onUseCrop: (left: Float, top: Float, right: Float, bottom: Float) -> Unit) {
    var left by remember(bitmap) { mutableFloatStateOf(0.05f) }
    var right by remember(bitmap) { mutableFloatStateOf(0.95f) }
    var top by remember(bitmap) { mutableFloatStateOf(0.05f) }
    var bottom by remember(bitmap) { mutableFloatStateOf(0.95f) }
    var drag by remember { mutableStateOf(CropDrag.NONE) }

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Text("사진 $pageNumber / 10 · 본문 영역 선택", fontSize = 21.sp)
        Text("모서리를 끌어 크기를 바꾸고, 안쪽을 끌어 이동하세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Image(bitmap.asImageBitmap(), "촬영한 페이지", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            Canvas(Modifier.fillMaxSize().pointerInput(bitmap) {
                fun imageBounds(): Rect {
                    val imageAspect = bitmap.width.toFloat() / bitmap.height
                    val boxAspect = size.width.toFloat() / size.height
                    val shownWidth = if (imageAspect > boxAspect) size.width.toFloat() else size.height * imageAspect
                    val shownHeight = if (imageAspect > boxAspect) size.width / imageAspect else size.height.toFloat()
                    return Rect((size.width - shownWidth) / 2f, (size.height - shownHeight) / 2f,
                        (size.width + shownWidth) / 2f, (size.height + shownHeight) / 2f)
                }
                fun cropRect(bounds: Rect) = Rect(bounds.left + bounds.width * left, bounds.top + bounds.height * top,
                    bounds.left + bounds.width * right, bounds.top + bounds.height * bottom)
                detectDragGestures(
                    onDragStart = { point ->
                        val crop = cropRect(imageBounds())
                        val radius = 48.dp.toPx()
                        fun near(target: Offset) = hypot(point.x - target.x, point.y - target.y) <= radius
                        drag = when {
                            near(crop.topLeft) -> CropDrag.TOP_LEFT
                            near(crop.topRight) -> CropDrag.TOP_RIGHT
                            near(crop.bottomLeft) -> CropDrag.BOTTOM_LEFT
                            near(crop.bottomRight) -> CropDrag.BOTTOM_RIGHT
                            crop.contains(point) -> CropDrag.MOVE
                            else -> CropDrag.NONE
                        }
                    },
                    onDragEnd = { drag = CropDrag.NONE }, onDragCancel = { drag = CropDrag.NONE }
                ) { change, amount ->
                    change.consume()
                    val bounds = imageBounds()
                    val dx = amount.x / bounds.width
                    val dy = amount.y / bounds.height
                    when (drag) {
                        CropDrag.TOP_LEFT -> { left = (left + dx).coerceIn(0f, right - 0.1f); top = (top + dy).coerceIn(0f, bottom - 0.1f) }
                        CropDrag.TOP_RIGHT -> { right = (right + dx).coerceIn(left + 0.1f, 1f); top = (top + dy).coerceIn(0f, bottom - 0.1f) }
                        CropDrag.BOTTOM_LEFT -> { left = (left + dx).coerceIn(0f, right - 0.1f); bottom = (bottom + dy).coerceIn(top + 0.1f, 1f) }
                        CropDrag.BOTTOM_RIGHT -> { right = (right + dx).coerceIn(left + 0.1f, 1f); bottom = (bottom + dy).coerceIn(top + 0.1f, 1f) }
                        CropDrag.MOVE -> {
                            val width = right - left; val height = bottom - top
                            left = (left + dx).coerceIn(0f, 1f - width); right = left + width
                            top = (top + dy).coerceIn(0f, 1f - height); bottom = top + height
                        }
                        CropDrag.NONE -> Unit
                    }
                }
            }) {
                val imageAspect = bitmap.width.toFloat() / bitmap.height
                val boxAspect = size.width / size.height
                val shownWidth = if (imageAspect > boxAspect) size.width else size.height * imageAspect
                val shownHeight = if (imageAspect > boxAspect) size.width / imageAspect else size.height
                val imageLeft = (size.width - shownWidth) / 2f; val imageTop = (size.height - shownHeight) / 2f
                val crop = Rect(imageLeft + shownWidth * left, imageTop + shownHeight * top,
                    imageLeft + shownWidth * right, imageTop + shownHeight * bottom)
                val shade = Color.Black.copy(alpha = 0.55f)
                drawRect(shade, Offset(imageLeft, imageTop), Size(shownWidth, crop.top - imageTop))
                drawRect(shade, Offset(imageLeft, crop.bottom), Size(shownWidth, imageTop + shownHeight - crop.bottom))
                drawRect(shade, Offset(imageLeft, crop.top), Size(crop.left - imageLeft, crop.height))
                drawRect(shade, Offset(crop.right, crop.top), Size(imageLeft + shownWidth - crop.right, crop.height))
                val yellow = Color(0xFFFFC107)
                drawRect(yellow, crop.topLeft, crop.size, style = Stroke(width = 6f))
                listOf(crop.topLeft, crop.topRight, crop.bottomLeft, crop.bottomRight).forEach {
                    drawCircle(Color.White, 16f, it); drawCircle(yellow, 16f, it, style = Stroke(width = 6f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onRetake, enabled = !processing, modifier = Modifier.weight(1f).height(56.dp)) { Text("다시 찍기", fontSize = 17.sp) }
            Button(onClick = { onUseCrop(left, top, right, bottom) }, enabled = !processing,
                modifier = Modifier.weight(1.4f).height(56.dp)) { Text(if (processing) "인식 중" else "이 영역 인식", fontSize = 17.sp) }
        }
        Spacer(Modifier.height(28.dp))
    }
}
