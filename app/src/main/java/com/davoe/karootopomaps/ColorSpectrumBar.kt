package com.davoe.karootopomaps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * A continuous spectrum color gradient bar for selecting hues across the full RGB rainbow.
 */
class ColorSpectrumBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val indicatorFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var selectedX = 0f
    private val rainbowColors = intArrayOf(
        Color.RED,
        Color.YELLOW,
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.RED
    )

    var onColorChangedListener: ((Int, String) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val gradient = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            rainbowColors, null, Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        if (selectedX == 0f) {
            selectedX = w / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = height / 2f
        // Draw rounded gradient bar
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), r, r, paint)

        // Draw indicator thumb
        val cx = selectedX.coerceIn(r, width.toFloat() - r)
        val cy = height / 2f
        canvas.drawCircle(cx, cy, r - 2f, indicatorFill)
        canvas.drawCircle(cx, cy, r - 2f, indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                selectedX = event.x.coerceIn(0f, width.toFloat())
                val fraction = (selectedX / width.toFloat()).coerceIn(0f, 1f)
                val color = getColorAtFraction(fraction)
                indicatorFill.color = color
                invalidate()

                val hex = String.format("#%06X", 0xFFFFFF and color)
                onColorChangedListener?.invoke(color, hex)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getColorAtFraction(fraction: Float): Int {
        val hsv = FloatArray(3)
        hsv[0] = (fraction * 360f) % 360f // Hue
        hsv[1] = 1.0f // Full saturation
        hsv[2] = 1.0f // Full brightness
        return Color.HSVToColor(hsv)
    }

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        val hueFraction = hsv[0] / 360f
        selectedX = hueFraction * (if (width > 0) width.toFloat() else 300f)
        indicatorFill.color = color
        invalidate()
    }
}
