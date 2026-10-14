package com.davoe.karootopomaps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

/**
 * Ultra-fast native Canvas simulation view that previews the custom topographic map in real-time
 * as colors, line widths, patterns, and typography are tweaked by the cyclist in CustomThemeEditorActivity.
 */
class MapPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val polygonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val casingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val meadowPath = Path()
    private val rockPath = Path()
    private val forestPath = Path()
    private val urbanPath = Path()
    private val waterPath = Path()
    private val riverPath = Path()
    private val contourMajorPath = Path()
    private val contourMinorPath = Path()
    private val roadPath = Path()
    private val secondaryPath = Path()
    private val singletrackPath = Path()

    private val clipRect = RectF()
    private var cornerRadius = 24f

    init {
        val density = resources.displayMetrics.density
        cornerRadius = 12f * density
    }

    /**
     * Refreshes the preview canvas with current user settings.
     */
    fun refreshPreview() {
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clipRect.set(0f, 0f, w.toFloat(), h.toFloat())

        val fw = w.toFloat()
        val fh = h.toFloat()

        // 1. Meadow area (left-center hill)
        meadowPath.reset()
        meadowPath.moveTo(0f, fh * 0.15f)
        meadowPath.cubicTo(fw * 0.25f, fh * 0.05f, fw * 0.5f, fh * 0.35f, fw * 0.45f, fh * 0.7f)
        meadowPath.lineTo(0f, fh * 0.75f)
        meadowPath.close()

        // 2. Forest area (top right)
        forestPath.reset()
        forestPath.moveTo(fw * 0.45f, 0f)
        forestPath.lineTo(fw, 0f)
        forestPath.lineTo(fw, fh * 0.48f)
        forestPath.cubicTo(fw * 0.8f, fh * 0.45f, fw * 0.65f, fh * 0.25f, fw * 0.45f, 0f)
        forestPath.close()

        // 3. High Mountain Rock / Scree (top corner inside forest/peak)
        rockPath.reset()
        rockPath.moveTo(fw * 0.72f, 0f)
        rockPath.lineTo(fw, 0f)
        rockPath.lineTo(fw, fh * 0.22f)
        rockPath.cubicTo(fw * 0.88f, fh * 0.18f, fw * 0.82f, fh * 0.08f, fw * 0.72f, 0f)
        rockPath.close()

        // 4. Urban / Village area (bottom left corner)
        urbanPath.reset()
        urbanPath.moveTo(0f, fh * 0.62f)
        urbanPath.lineTo(fw * 0.32f, fh * 0.58f)
        urbanPath.lineTo(fw * 0.28f, fh)
        urbanPath.lineTo(0f, fh)
        urbanPath.close()

        // 5. Lake / Water body (bottom right)
        waterPath.reset()
        waterPath.moveTo(fw * 0.62f, fh)
        waterPath.cubicTo(fw * 0.55f, fh * 0.75f, fw * 0.75f, fh * 0.68f, fw * 0.95f, fh * 0.75f)
        waterPath.lineTo(fw, fh * 0.78f)
        waterPath.lineTo(fw, fh)
        waterPath.close()

        // 6. River winding from top-right to lake
        riverPath.reset()
        riverPath.moveTo(fw * 0.92f, fh * 0.25f)
        riverPath.cubicTo(fw * 0.75f, fh * 0.38f, fw * 0.82f, fh * 0.55f, fw * 0.70f, fh * 0.78f)

        // 7. Elevation Contours
        contourMajorPath.reset()
        contourMajorPath.moveTo(0f, fh * 0.32f)
        contourMajorPath.cubicTo(fw * 0.3f, fh * 0.25f, fw * 0.6f, fh * 0.45f, fw, fh * 0.36f)

        contourMinorPath.reset()
        contourMinorPath.moveTo(0f, fh * 0.22f)
        contourMinorPath.cubicTo(fw * 0.3f, fh * 0.15f, fw * 0.6f, fh * 0.35f, fw, fh * 0.26f)

        contourMinorPath.moveTo(0f, fh * 0.42f)
        contourMinorPath.cubicTo(fw * 0.3f, fh * 0.35f, fw * 0.6f, fh * 0.55f, fw, fh * 0.46f)

        // 8. Primary National Road (bold curve across screen)
        roadPath.reset()
        roadPath.moveTo(0f, fh * 0.72f)
        roadPath.cubicTo(fw * 0.35f, fh * 0.70f, fw * 0.55f, fh * 0.45f, fw, fh * 0.52f)

        // 9. Secondary Road
        secondaryPath.reset()
        secondaryPath.moveTo(fw * 0.35f, fh * 0.70f)
        secondaryPath.cubicTo(fw * 0.45f, fh * 0.85f, fw * 0.50f, fh * 0.95f, fw * 0.55f, fh)

        // 10. MTB Singletrack / Trail heading up mountain
        singletrackPath.reset()
        singletrackPath.moveTo(fw * 0.35f, fh * 0.70f)
        singletrackPath.cubicTo(fw * 0.30f, fh * 0.50f, fw * 0.48f, fh * 0.35f, fw * 0.68f, fh * 0.18f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val fw = width.toFloat()
        val fh = height.toFloat()

        // Clip rounded corners for a sleek GPS screen look
        val clipPath = Path().apply {
            addRoundRect(clipRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val isSunlight = CustomThemeManager.isSunlightBoosterEnabled(context)
        val isWaterRadar = CustomThemeManager.isWaterRadarEnabled(context)
        val isMtbGrades = CustomThemeManager.isMtbGradesEnabled(context)

        // Base Terrain / Background
        val bgColor = if (CustomThemeManager.isElementVisible(context, "color_bg")) {
            parseSafeColor(CustomThemeManager.getColor(context, "color_bg"), "#F5EFD7")
        } else {
            Color.WHITE
        }
        bgPaint.color = bgColor
        canvas.drawRect(0f, 0f, fw, fh, bgPaint)

        // Meadow Area
        if (CustomThemeManager.isElementVisible(context, "color_meadow")) {
            polygonPaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_meadow"), "#D7E4B4")
            canvas.drawPath(meadowPath, polygonPaint)
        }

        // Forest Area
        if (CustomThemeManager.isElementVisible(context, "color_forest")) {
            polygonPaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_forest"), "#A3C8A7")
            canvas.drawPath(forestPath, polygonPaint)
        }

        // Rock / Scree Area
        if (CustomThemeManager.isElementVisible(context, "color_rock")) {
            polygonPaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_rock"), "#D8D4CC")
            canvas.drawPath(rockPath, polygonPaint)
        }

        // Urban Area
        if (CustomThemeManager.isElementVisible(context, "color_residential")) {
            polygonPaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_residential"), "#F48D84")
            canvas.drawPath(urbanPath, polygonPaint)
        }

        // Water Body / Lake
        if (CustomThemeManager.isElementVisible(context, "color_water_body")) {
            polygonPaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_water_body"), "#B3E3F9")
            canvas.drawPath(waterPath, polygonPaint)
        }

        // River
        if (CustomThemeManager.isElementVisible(context, "color_river")) {
            linePaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_river"), "#429AD2")
            linePaint.strokeWidth = 2.5f * density
            linePaint.pathEffect = null
            canvas.drawPath(riverPath, linePaint)
        }

        // Elevation Contours - Minor
        if (CustomThemeManager.isElementVisible(context, "color_contour_minor")) {
            val width = CustomThemeManager.getLineWidth(context, "color_contour_minor") * density * if (isSunlight) 1.25f else 1.0f
            applyLineStyle(linePaint, "color_contour_minor", width)
            linePaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_contour_minor"), "#9E7E5A")
            canvas.drawPath(contourMinorPath, linePaint)
        }

        // Elevation Contours - Major
        if (CustomThemeManager.isElementVisible(context, "color_contour_major")) {
            val width = CustomThemeManager.getLineWidth(context, "color_contour_major") * 1.5f * density * if (isSunlight) 1.25f else 1.0f
            applyLineStyle(linePaint, "color_contour_major", width)
            linePaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_contour_major"), "#855F3C")
            canvas.drawPath(contourMajorPath, linePaint)
        }

        // Secondary Road
        if (CustomThemeManager.isElementVisible(context, "color_secondary_road")) {
            val width = CustomThemeManager.getLineWidth(context, "color_secondary_road") * 3.5f * density * if (isSunlight) 1.25f else 1.0f
            // Casing
            casingPaint.strokeWidth = width + 2f * density
            casingPaint.color = if (isSunlight) Color.BLACK else Color.parseColor("#777777")
            canvas.drawPath(secondaryPath, casingPaint)

            applyLineStyle(linePaint, "color_secondary_road", width)
            linePaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_secondary_road"), "#F5D061")
            canvas.drawPath(secondaryPath, linePaint)
        }

        // Primary National Road
        if (CustomThemeManager.isElementVisible(context, "color_primary_road")) {
            val width = CustomThemeManager.getLineWidth(context, "color_primary_road") * 4.5f * density * if (isSunlight) 1.25f else 1.0f
            // Casing
            casingPaint.strokeWidth = width + 2.5f * density
            casingPaint.color = if (isSunlight) Color.BLACK else Color.parseColor("#555555")
            canvas.drawPath(roadPath, casingPaint)

            applyLineStyle(linePaint, "color_primary_road", width)
            linePaint.color = parseSafeColor(CustomThemeManager.getColor(context, "color_primary_road"), "#E5933A")
            canvas.drawPath(roadPath, linePaint)
        }

        // MTB Singletrack / Trail
        if (CustomThemeManager.isElementVisible(context, "color_mtb_singletrack")) {
            val width = CustomThemeManager.getLineWidth(context, "color_mtb_singletrack") * 2.5f * density * if (isSunlight) 1.25f else 1.0f
            applyLineStyle(linePaint, "color_mtb_singletrack", width)
            val trailColor = if (isMtbGrades) {
                Color.parseColor("#D32F2F") // Official IMBA Difficult Red
            } else {
                parseSafeColor(CustomThemeManager.getColor(context, "color_mtb_singletrack"), "#A020F0")
            }
            linePaint.color = trailColor
            canvas.drawPath(singletrackPath, linePaint)
        }

        // POI: Drinking Water Fountain (Survival Mode)
        if (CustomThemeManager.isElementVisible(context, "color_water_fountain")) {
            val fountainX = fw * 0.36f
            val fountainY = fh * 0.65f
            val fountainColor = if (isWaterRadar) Color.parseColor("#00E5FF") else parseSafeColor(CustomThemeManager.getColor(context, "color_water_fountain"), "#0088FF")
            val radius = (if (isWaterRadar) 6.5f else 4.5f) * density

            iconPaint.color = Color.WHITE
            canvas.drawCircle(fountainX, fountainY, radius + 1.5f * density, iconPaint)
            iconPaint.color = fountainColor
            canvas.drawCircle(fountainX, fountainY, radius, iconPaint)
        }

        // POI: Mountain Peak
        if (CustomThemeManager.isElementVisible(context, "color_peak_mountain")) {
            val peakX = fw * 0.72f
            val peakY = fh * 0.18f
            val peakColor = parseSafeColor(CustomThemeManager.getColor(context, "color_peak_mountain"), "#8B4513")
            val trianglePath = Path().apply {
                moveTo(peakX, peakY - 6f * density)
                lineTo(peakX - 5f * density, peakY + 4f * density)
                lineTo(peakX + 5f * density, peakY + 4f * density)
                close()
            }
            iconPaint.color = peakColor
            canvas.drawPath(trianglePath, iconPaint)
        }

        // Typography: Place / Peak Label
        val scale = CustomThemeManager.getFontScale(context)
        val weight = CustomThemeManager.getFontWeight(context)
        val family = CustomThemeManager.getFontFamily(context)

        textPaint.textSize = 10f * density * scale
        textPaint.isFakeBoldText = (weight == CustomThemeManager.FONT_WEIGHT_BOLD)
        textPaint.typeface = when (family) {
            CustomThemeManager.FONT_FAMILY_MONO -> Typeface.MONOSPACE
            CustomThemeManager.FONT_FAMILY_CONDENSED -> Typeface.SANS_SERIF
            else -> Typeface.DEFAULT
        }
        textPaint.color = if (CustomThemeManager.isElementVisible(context, "color_place_label")) {
            parseSafeColor(CustomThemeManager.getColor(context, "color_place_label"), "#222222")
        } else {
            Color.TRANSPARENT
        }

        haloPaint.strokeWidth = 2.5f * density
        haloPaint.textSize = textPaint.textSize
        haloPaint.isFakeBoldText = textPaint.isFakeBoldText
        haloPaint.typeface = textPaint.typeface
        // Halo matches background or inverse for contrast
        haloPaint.color = if (bgColor == Color.BLACK) Color.parseColor("#333333") else Color.WHITE

        val labelX = fw * 0.58f
        val labelY = fh * 0.18f
        canvas.drawText("Alto del Puerto 1.482m", labelX, labelY, haloPaint)
        canvas.drawText("Alto del Puerto 1.482m", labelX, labelY, textPaint)

        // Contour Elevation Label
        if (CustomThemeManager.isElementVisible(context, "color_contour_label")) {
            val contourLabelColor = parseSafeColor(CustomThemeManager.getColor(context, "color_contour_label"), "#5C442A")
            textPaint.textSize = 8.5f * density * scale
            textPaint.color = contourLabelColor
            canvas.drawText("1.400m", fw * 0.15f, fh * 0.30f, haloPaint)
            canvas.drawText("1.400m", fw * 0.15f, fh * 0.30f, textPaint)
        }

        // Border stroke around preview card
        casingPaint.style = Paint.Style.STROKE
        casingPaint.strokeWidth = 2f * density
        casingPaint.color = Color.parseColor("#FFD700")
        canvas.drawRoundRect(clipRect, cornerRadius, cornerRadius, casingPaint)
    }

    private fun applyLineStyle(paint: Paint, key: String, width: Float) {
        paint.strokeWidth = width
        val pattern = CustomThemeManager.getLinePattern(context, key)
        val density = resources.displayMetrics.density
        when (pattern) {
            "dashed" -> paint.pathEffect = DashPathEffect(floatArrayOf(8f * density, 4f * density), 0f)
            "dotted" -> paint.pathEffect = DashPathEffect(floatArrayOf(2f * density, 3f * density), 0f)
            else -> paint.pathEffect = null
        }
    }

    private fun parseSafeColor(hex: String, defaultHex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.parseColor(defaultHex)
        }
    }
}
