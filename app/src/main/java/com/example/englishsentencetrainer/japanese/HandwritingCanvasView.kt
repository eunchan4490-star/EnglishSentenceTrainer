package com.example.englishsentencetrainer.japanese

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.digitalink.recognition.Ink

class HandwritingCanvasView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(45, 45, 45)
        strokeWidth = 14f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val paths = mutableListOf<Path>()
    private var currentPath: Path? = null
    private var inkBuilder = Ink.builder()
    private var strokeBuilder: Ink.Stroke.Builder? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        paths.forEach { canvas.drawPath(it, paint) }
        currentPath?.let { canvas.drawPath(it, paint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val time = System.currentTimeMillis()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                currentPath = Path().apply { moveTo(event.x, event.y) }
                strokeBuilder = Ink.Stroke.builder().apply { addPoint(Ink.Point.create(event.x, event.y, time)) }
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.lineTo(event.x, event.y)
                strokeBuilder?.addPoint(Ink.Point.create(event.x, event.y, time))
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.lineTo(event.x, event.y)
                strokeBuilder?.addPoint(Ink.Point.create(event.x, event.y, time))
                currentPath?.let(paths::add)
                strokeBuilder?.build()?.let(inkBuilder::addStroke)
                currentPath = null
                strokeBuilder = null
                parent.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        }
        invalidate()
        return true
    }

    fun ink(): Ink = inkBuilder.build()
    fun hasInk(): Boolean = paths.isNotEmpty() || currentPath != null
    fun clearInk() {
        paths.clear()
        currentPath = null
        strokeBuilder = null
        inkBuilder = Ink.builder()
        invalidate()
    }
}
