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
 * Gradient bar for adjusting color saturation and brightness (intensity/tint).
 * Sliders from very light/pastel tint (0%) to 100% full vibrant pure color, down to dark tones.
 */
class ColorIntensityBar @JvmOverloads constructor(
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

    private var baseColor: Int = Color.RED
    private var selectedFraction = 1.0f

    var onIntensityChangedListener: ((Int, String) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGradient()
    }

    private fun updateGradient() {
        if (width <= 0) return
        // Gradient from light pastel tint (white/low saturation) to 100% pure saturated base color
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        
        // 0% fraction: light pastel tint (saturation = 0.15, brightness = 1.0)
        val lightColor = Color.HSVToColor(floatArrayOf(hsv[0], 0.15f, 1.0f))
        // 50% fraction: medium intensity (saturation = 0.60, brightness = 1.0)
        val medColor = Color.HSVToColor(floatArrayOf(hsv[0], 0.60f, 1.0f))
        // 100% fraction: 100% full vibrant color (saturation = 1.0, brightness = 1.0)
        val fullColor = Color.HSVToColor(floatArrayOf(hsv[0], 1.0f, 1.0f))

        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            intArrayOf(lightColor, medColor, fullColor), null, Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = height / 2f
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), r, r, paint)

        val cx = (selectedFraction * width.toFloat()).coerceIn(r, width.toFloat() - r)
        val cy = height / 2f
        canvas.drawCircle(cx, cy, r - 2f, indicatorFill)
        canvas.drawCircle(cx, cy, r - 2f, indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                selectedFraction = (event.x / width.toFloat()).coerceIn(0f, 1f)
                val color = calculateColorAtFraction(selectedFraction)
                indicatorFill.color = color
                invalidate()

                val hex = String.format("#%06X", 0xFFFFFF and color)
                onIntensityChangedListener?.invoke(color, hex)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun calculateColorAtFraction(fraction: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        // fraction from 0.0 (light pastel) to 1.0 (100% intense)
        val sat = 0.15f + (0.85f * fraction)
        hsv[1] = sat.coerceIn(0.15f, 1.0f)
        hsv[2] = 1.0f
        return Color.HSVToColor(hsv)
    }

    /**
     * Sets the base hue from which intensity/tint is varied.
     */
    fun setBaseColor(color: Int, currentHex: String? = null) {
        baseColor = color
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        // If current hex provided, deduce current saturation fraction
        selectedFraction = ((hsv[1] - 0.15f) / 0.85f).coerceIn(0.1f, 1.0f)
        val currentColor = calculateColorAtFraction(selectedFraction)
        indicatorFill.color = currentColor
        updateGradient()
    }
}
