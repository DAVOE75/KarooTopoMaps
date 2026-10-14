package com.davoe.karootopomaps

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import org.xmlpull.v1.XmlPullParserFactory

object CustomThemeManager {

    private const val TAG = "CustomThemeManager"
    private const val PREFS_NAME = "karoo_custom_theme_colors"
    const val CUSTOM_THEME_ID = "custom_user_theme"

    fun getColor(context: Context, key: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultColor = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultColorHex ?: "#000000"
        return prefs.getString(key, defaultColor) ?: defaultColor
    }

    fun setColor(context: Context, key: String, hexColor: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, hexColor.uppercase()).apply()
    }

    fun isElementVisible(context: Context, key: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultVis = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultEnabled ?: true
        return prefs.getBoolean("${key}_visible", defaultVis)
    }

    fun setElementVisible(context: Context, key: String, visible: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("${key}_visible", visible).apply()
    }

    fun getLineWidth(context: Context, key: String): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultWidth = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultWidth ?: 0.6f
        return prefs.getFloat("${key}_width", defaultWidth)
    }

    fun setLineWidth(context: Context, key: String, width: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat("${key}_width", width).apply()
    }

    fun getLinePattern(context: Context, key: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultPattern = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultDashArray ?: "solid"
        return prefs.getString("${key}_pattern", defaultPattern) ?: defaultPattern
    }

    fun setLinePattern(context: Context, key: String, pattern: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("${key}_pattern", pattern).apply()
    }

    // Typography Constants
    const val FONT_SCALE_COMPACT = "0.85"
    const val FONT_SCALE_NORMAL = "1.0"
    const val FONT_SCALE_LARGE = "1.2"
    const val FONT_SCALE_HUGE = "1.4"

    const val FONT_WEIGHT_NORMAL = "normal"
    const val FONT_WEIGHT_BOLD = "bold"

    const val FONT_FAMILY_DEFAULT = "default"
    const val FONT_FAMILY_CONDENSED = "thin"
    const val FONT_FAMILY_MONO = "monospace"

    fun getFontScale(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("font_scale", FONT_SCALE_NORMAL)?.toFloatOrNull() ?: 1.0f
    }

    fun setFontScale(context: Context, scale: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("font_scale", scale).apply()
    }

    fun getFontWeight(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("font_weight", FONT_WEIGHT_BOLD) ?: FONT_WEIGHT_BOLD
    }

    fun setFontWeight(context: Context, weight: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("font_weight", weight).apply()
    }

    fun getFontFamily(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("font_family", FONT_FAMILY_DEFAULT) ?: FONT_FAMILY_DEFAULT
    }

    fun setFontFamily(context: Context, family: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("font_family", family).apply()
    }

    // PRO Special Feature Toggles
    const val PREF_SUNLIGHT_BOOSTER = "pro_sunlight_booster"
    const val PREF_WATER_RADAR = "pro_water_radar"
    const val PREF_MTB_GRADES = "pro_mtb_grades"

    fun isSunlightBoosterEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_SUNLIGHT_BOOSTER, false)
    }

    fun setSunlightBoosterEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_SUNLIGHT_BOOSTER, enabled).apply()
    }

    fun isWaterRadarEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_WATER_RADAR, false)
    }

    fun setWaterRadarEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_WATER_RADAR, enabled).apply()
    }

    fun isMtbGradesEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_MTB_GRADES, false)
    }

    fun setMtbGradesEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_MTB_GRADES, enabled).apply()
    }

    fun exportProfileCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val all = prefs.all
        val json = org.json.JSONObject()
        for ((k, v) in all) {
            json.put(k, v)
        }
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    fun importProfileCode(context: Context, code: String): Boolean {
        return try {
            val bytes = android.util.Base64.decode(code.trim(), android.util.Base64.NO_WRAP)
            val jsonStr = String(bytes, Charsets.UTF_8)
            val json = org.json.JSONObject(jsonStr)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                when (val value = json.get(key)) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is String -> editor.putString(key, value)
                }
            }
            editor.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing profile code", e)
            false
        }
    }

    fun resetToDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun loadPreset(context: Context, presetThemeId: String) {
        when (presetThemeId) {
            "road" -> {
                setColor(context, "color_bg", "#FFFFFF")
                setColor(context, "color_motorway", "#E05A47")
                setColor(context, "color_primary_road", "#E5933A")
                setColor(context, "color_secondary_road", "#F5D061")
                setColor(context, "color_residential_street", "#EEEEEE")
                setLineWidth(context, "color_primary_road", 1.4f)
                setLineWidth(context, "color_secondary_road", 1.2f)
                setElementVisible(context, "color_contour_major", false)
                setElementVisible(context, "color_contour_minor", false)
                setElementVisible(context, "color_contour_label", false)
                setElementVisible(context, "color_mtb_singletrack", false)
                setElementVisible(context, "color_gravel_track", false)
                setElementVisible(context, "color_water_body", true)
                setElementVisible(context, "color_place_label", true)
                setFontWeight(context, FONT_WEIGHT_BOLD)
            }
            "mtb" -> {
                setColor(context, "color_bg", "#F9F6F0")
                setColor(context, "color_meadow", "#E2EBD3")
                setColor(context, "color_forest", "#A3C8A7")
                setColor(context, "color_contour_major", "#6D5339")
                setColor(context, "color_contour_minor", "#9E7E5A")
                setColor(context, "color_contour_label", "#4A3728")
                setLineWidth(context, "color_contour_major", 1.0f)
                setLineWidth(context, "color_contour_minor", 0.5f)
                setElementVisible(context, "color_contour_major", true)
                setElementVisible(context, "color_contour_minor", true)
                setElementVisible(context, "color_contour_label", true)
                setColor(context, "color_mtb_singletrack", "#FF0000")
                setLineWidth(context, "color_mtb_singletrack", 1.2f)
                setLinePattern(context, "color_mtb_singletrack", "dashed")
                setColor(context, "color_gravel_track", "#8B5230")
                setElementVisible(context, "color_mtb_singletrack", true)
                setElementVisible(context, "color_gravel_track", true)
                setWaterRadarEnabled(context, true)
                setMtbGradesEnabled(context, true)
            }
            "gravel" -> {
                setColor(context, "color_bg", "#F5EFE6")
                setColor(context, "color_gravel_track", "#A0522D")
                setColor(context, "color_mtb_singletrack", "#8B4513")
                setLineWidth(context, "color_gravel_track", 1.4f)
                setLineWidth(context, "color_primary_road", 0.8f)
                setLineWidth(context, "color_secondary_road", 0.7f)
                setColor(context, "color_secondary_road", "#D3C89B")
                setElementVisible(context, "color_contour_major", true)
                setElementVisible(context, "color_contour_minor", true)
                setWaterRadarEnabled(context, true)
            }
            "dark" -> {
                setColor(context, "color_bg", "#000000")
                setColor(context, "color_meadow", "#111111")
                setColor(context, "color_forest", "#162816")
                setColor(context, "color_rock", "#1F1F1F")
                setColor(context, "color_water_body", "#0A2540")
                setColor(context, "color_river", "#1565C0")
                setColor(context, "color_residential", "#1E1E1E")
                setColor(context, "color_building", "#1E1E1E")
                setColor(context, "color_contour_major", "#8A7050")
                setColor(context, "color_contour_minor", "#5A4A35")
                setColor(context, "color_contour_label", "#D8C5A8")
                setColor(context, "color_primary_road", "#FF9900")
                setColor(context, "color_secondary_road", "#FFD700")
                setColor(context, "color_residential_street", "#555555")
                setColor(context, "color_gravel_track", "#CC8844")
                setColor(context, "color_mtb_singletrack", "#E040FB")
                setColor(context, "color_place_label", "#FFFFFF")
                setElementVisible(context, "color_contour_major", true)
                setElementVisible(context, "color_contour_minor", true)
            }
            else -> {
                resetToDefaults(context)
            }
        }
    }

    /**
     * Verifies that the generated XML string is 100% syntactically valid XML.
     * Prevents any malformed XML from ever being saved to /sdcard/mtb_v2.xml.
     */
    private fun isValidXml(xml: String): Boolean {
        return try {
            val cleanXml = xml.removePrefix("\uFEFF").trim()
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(cleanXml))
            while (parser.next() != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                // Parse through whole document
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "XML validation failed!", e)
            false
        }
    }

    /**
     * Generates a fully dynamic Mapsforge VTM theme XML with all user-customized colors
     * and applies it cleanly and safely to the Karoo target file (/sdcard/mtb_v2.xml).
     */
    fun generateAndApplyCustomTheme(context: Context): Boolean {
        ThemeManager.ensureBackupExists()
        val target = ThemeManager.getTargetThemeFile()

        return try {
            // Load base template (IGN topo as baseline template) and strip any UTF-8 BOM
            val rawTemplate = context.assets.open("themes/ign_topo.xml").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val template = rawTemplate.removePrefix("\uFEFF").trimStart()

            var customXml = template

            // 1. Background color
            val bg = getColor(context, "color_bg")
            customXml = customXml.replace(Regex("""map-background="[^"]*""""), """map-background="$bg"""")
            customXml = customXml.replace(
                Regex("""(<m e="way" k="natural" v="nosea">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""),
                "$1$bg$2"
            )

            // 2. Water
            val waterVis = isElementVisible(context, "color_water_body")
            val water = if (waterVis) getColor(context, "color_water_body") else bg
            val riverVis = isElementVisible(context, "color_river")
            val river = if (riverVis) getColor(context, "color_river") else bg

            customXml = customXml.replace(Regex("""(<style-area fill=")[^"]*("\s*id="water")"""), "$1$water$2")
            customXml = customXml.replace(Regex("""(<style-line cap="butt" id="water" stroke=")[^"]*("\s*width="1\.0"/>)"""), "$1$river$2")

            // 3. Terrain & Landuse
            val meadow = if (isElementVisible(context, "color_meadow")) getColor(context, "color_meadow") else bg
            val forest = if (isElementVisible(context, "color_forest")) getColor(context, "color_forest") else bg
            val rock = if (isElementVisible(context, "color_rock")) getColor(context, "color_rock") else bg
            val sand = if (isElementVisible(context, "color_sand")) getColor(context, "color_sand") else bg
            val park = if (isElementVisible(context, "color_park")) getColor(context, "color_park") else bg

            customXml = customXml.replace(Regex("""(<m v="meadow">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""), "$1$meadow$2")
            customXml = customXml.replace(Regex("""(<m v="forest">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""), "$1$forest$2")
            customXml = customXml.replace(Regex("""(<m v="wood">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""), "$1$forest$2")
            customXml = customXml.replace(Regex("""(<m v="rock\|bare_rock\|stone\|scree\|glacier\|cliff">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""), "$1$rock$2")
            customXml = customXml.replace(Regex("""(<m v="sand\|beach">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""), "$1$sand$2")
            customXml = customXml.replace(Regex("""(<m v="park\|common\|green\|golf_course">\s*<area mesh="true" fill=")[^"]*("\s*/>)"""), "$1$park$2")

            // 4. Urban & Buildings
            val residential = if (isElementVisible(context, "color_residential")) getColor(context, "color_residential") else bg
            val residentialStroke = if (isElementVisible(context, "color_residential")) "#c8554b" else bg
            val building = if (isElementVisible(context, "color_building")) getColor(context, "color_building") else bg
            val buildingStroke = if (isElementVisible(context, "color_building")) "#a84038" else bg
            val industrial = if (isElementVisible(context, "color_industrial")) getColor(context, "color_industrial") else bg

            customXml = customXml.replace(
                Regex("""(<m v="residential">\s*<area mesh="true" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width="0\.3"/>)"""),
                "$1$residential$2$residentialStroke$3"
            )
            customXml = customXml.replace(
                Regex("""(<m v="retail">\s*<area mesh="true" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width="0\.3"/>)"""),
                "$1$residential$2$residentialStroke$3"
            )
            customXml = customXml.replace(
                Regex("""(<m e="way" k="building" v="\*"\s+zoom-min="14">\s*<area mesh="true" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width=")[^"]*("\s*/>)"""),
                "$1$building$2$buildingStroke$3" + (if (isElementVisible(context, "color_building")) "0.35" else "0.0") + "$4"
            )
            customXml = customXml.replace(
                Regex("""(<m v="industrial\|railway">\s*<area mesh="true" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width="0\.3"/>)"""),
                "$1$industrial$2" + (if (isElementVisible(context, "color_industrial")) "#B496B0" else bg) + "$3"
            )

            // 5. Contours
            val cMajorVis = isElementVisible(context, "color_contour_major")
            val cMinorVis = isElementVisible(context, "color_contour_minor")
            val cLabelVis = isElementVisible(context, "color_contour_label")

            val cMajor = if (cMajorVis) getColor(context, "color_contour_major") else bg
            val cMajorWidth = if (cMajorVis) getLineWidth(context, "color_contour_major") else 0.0f
            val cMinor = if (cMinorVis) getColor(context, "color_contour_minor") else bg
            val cMinorWidth = if (cMinorVis) getLineWidth(context, "color_contour_minor") else 0.0f
            val cLabel = if (cLabelVis && cMajorVis) getColor(context, "color_contour_label") else bg

            customXml = customXml.replace(
                Regex("""(<m e="way" k="natural\|contour_ext" v="minor_contour_line\|elevation_minor\|elevation_medium" zoom-min="14">\s*<line stroke=")[^"]*("\s*width=")[^"]*("\s*cap="round"/>)"""),
                "$1$cMinor$2$cMinorWidth$3"
            )
            customXml = customXml.replace(
                Regex("""(<m e="way" k="natural\|contour_ext" v="major_contour_line\|elevation_major" zoom-min="11">\s*<line stroke=")[^"]*("\s*width=")[^"]*("\s*cap="round"/>)"""),
                "$1$cMajor$2$cMajorWidth$3"
            )
            customXml = customXml.replace(
                Regex("""(<text k="name" caption="true" size="11" priority="5" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width=")[^"]*("\s*style="bold"/>)"""),
                "$1$cLabel$2" + (if (cLabelVis) "#FFFFFF" else bg) + "$3" + (if (cLabelVis) "2.0" else "0.0") + "$4"
            )
            customXml = customXml.replace(
                Regex("""(<text k="ele" caption="true" size="11" priority="5" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width=")[^"]*("\s*style="bold"/>)"""),
                "$1$cLabel$2" + (if (cLabelVis) "#FFFFFF" else bg) + "$3" + (if (cLabelVis) "2.0" else "0.0") + "$4"
            )

            // 6. Roads (Motorways, Primary, Secondary, Residential)
            val motorwayVis = isElementVisible(context, "color_motorway")
            val motorwayColor = if (motorwayVis) getColor(context, "color_motorway") else bg
            val motorwayWidth = if (motorwayVis) getLineWidth(context, "color_motorway") else 0.0f
            customXml = customXml.replace(
                Regex("""(<m e="way" k="highway" v="motorway\|motorway_link\|trunk\|trunk_link">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$motorwayColor$2" + (motorwayWidth * 1.3f) + "$3$motorwayColor$4$motorwayWidth$5" + (if (motorwayVis) "#FFFFFF" else bg) + "$6" + (if (motorwayVis) "0.45" else "0.0") + "$7"
            )

            val priRoadVis = isElementVisible(context, "color_primary_road")
            val priColor = if (priRoadVis) getColor(context, "color_primary_road") else bg
            val priWidth = if (priRoadVis) getLineWidth(context, "color_primary_road") else 0.0f
            customXml = customXml.replace(
                Regex("""(<m e="way" k="highway" v="primary\|primary_link">\s*<m zoom-min="12">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$priColor$2$priWidth$3"
            )
            customXml = customXml.replace(
                Regex("""(<m zoom-max="11" zoom-min="8">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>\s*</m>\s*<m zoom-min="8">\s*<text use="road_label")"""),
                "$1$priColor$2" + (priWidth * 0.85f) + "$3"
            )

            val secRoadVis = isElementVisible(context, "color_secondary_road")
            val secColor = if (secRoadVis) getColor(context, "color_secondary_road") else bg
            val secWidth = if (secRoadVis) getLineWidth(context, "color_secondary_road") else 0.0f
            customXml = customXml.replace(
                Regex("""(<m e="way" k="highway" v="secondary\|secondary_link">\s*<m zoom-min="13">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$secColor$2$secWidth$3"
            )
            customXml = customXml.replace(
                Regex("""(<m zoom-max="12" zoom-min="10">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>\s*</m>\s*<m zoom-min="10">\s*<text use="road_label")"""),
                "$1$secColor$2" + (secWidth * 0.85f) + "$3"
            )
            customXml = customXml.replace(
                Regex("""(<m e="way" k="highway" v="tertiary\|tertiary_link">\s*<m zoom-min="14">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$secColor$2$secWidth$3"
            )

            val resStreetVis = isElementVisible(context, "color_residential_street")
            val resColor = if (resStreetVis) getColor(context, "color_residential_street") else bg
            val resWidth = if (resStreetVis) getLineWidth(context, "color_residential_street") else 0.0f
            customXml = customXml.replace(
                Regex("""(<m e="way" k="highway" v="residential\|living_street">\s*<m zoom-min="15">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$resColor$2$resWidth$3"
            )
            customXml = customXml.replace(
                Regex("""(<m e="way" k="highway" v="unclassified\|road">\s*<m zoom-min="15">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$resColor$2$resWidth$3"
            )

            // 7. Cycleways and Paths
            val cyclewayVis = isElementVisible(context, "color_cycleway")
            val cyclewayColor = if (cyclewayVis) getColor(context, "color_cycleway") else bg
            val cyclewayWidth = if (cyclewayVis) getLineWidth(context, "color_cycleway") else 0.0f
            customXml = customXml.replace(
                Regex("""(<style-line id="paved_cycleway_casing" stroke=")[^"]*("\s*cap="round"/>)"""),
                "$1$cyclewayColor$2"
            )
            customXml = customXml.replace(
                Regex("""(<style-line id="cycling_path" stroke=")[^"]*("\s*dasharray=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$cyclewayColor$2" + (if (cyclewayVis) "8,8" else "0,100") + "$3$cyclewayWidth$4"
            )

            // 8. Singletracks and Gravel Tracks
            val mtbVis = isElementVisible(context, "color_mtb_singletrack")
            val mtbColor = if (mtbVis) getColor(context, "color_mtb_singletrack") else bg
            val mtbWidth = if (mtbVis) getLineWidth(context, "color_mtb_singletrack") else 0.0f
            val mtbPattern = getLinePattern(context, "color_mtb_singletrack")
            val mtbDash = when (mtbPattern) {
                "solid" -> "0,0"
                "dotted" -> "3,3"
                else -> "6,4"
            }

            val gravelVis = isElementVisible(context, "color_gravel_track")
            val gravelColor = if (gravelVis) getColor(context, "color_gravel_track") else bg

            customXml = customXml.replace(
                Regex("""(<style-line id="path" stroke=")[^"]*("\s*dasharray=")[^"]*("\s*cap="round"\s*width=")[^"]*("/>)"""),
                "$1$mtbColor$2" + (if (mtbVis) mtbDash else "0,100") + "$3$mtbWidth$4"
            )
            customXml = customXml.replace(
                Regex("""(<style-line id="unpaved_casing" stroke=")[^"]*("\s*cap="round"/>)"""),
                "$1$gravelColor$2"
            )

            // 9. Place & Road Labels
            val placeVis = isElementVisible(context, "color_place_label")
            val labelColor = if (placeVis) getColor(context, "color_place_label") else bg
            customXml = customXml.replace(
                Regex("""(<style-text id="road_label" k="name" fill=")[^"]*("\s*stroke=")[^"]*("\s*stroke-width=")[^"]*("\s*style="bold"/>)"""),
                "$1$labelColor$2" + (if (placeVis) "#FFFFFF" else bg) + "$3" + (if (placeVis) "2.0" else "0.0") + "$4"
            )

            // 10. Typography Scale, Weight, and Family
            val fontScale = getFontScale(context)
            if (fontScale != 1.0f) {
                customXml = customXml.replace(Regex("""size="(\d+)"""")) { m ->
                    val original = m.groupValues[1].toIntOrNull() ?: 12
                    val scaled = (original * fontScale).toInt().coerceAtLeast(8)
                    """size="$scaled""""
                }
            }

            val fontWeight = getFontWeight(context)
            if (fontWeight == FONT_WEIGHT_NORMAL) {
                customXml = customXml.replace(Regex("""style="bold""""), """style="normal"""")
            } else if (fontWeight == FONT_WEIGHT_BOLD) {
                customXml = customXml.replace(Regex("""style="normal""""), """style="bold"""")
            }

            val fontFamily = getFontFamily(context)
            if (fontFamily != FONT_FAMILY_DEFAULT) {
                customXml = customXml.replace(Regex("""font-family="[^"]*""""), """font-family="$fontFamily"""")
            }

            // 11. Pro special features
            if (isWaterRadarEnabled(context)) {
                customXml = customXml.replace(
                    Regex("""(<m v="drinking_water" zoom-min=")[^"]*(">[\s\S]*?<symbol id="drinking-water" symbol-percent=")[^"]*(")"""),
                    """$111$2175$3"""
                )
            }

            if (isMtbGradesEnabled(context)) {
                customXml = customXml.replace(
                    Regex("""<line stroke="#854E85" cap="round" width="1\.0"/>"""),
                    """<line stroke="#00C853" cap="round" width="1.2"/>"""
                )
                customXml = customXml.replace(
                    Regex("""<line stroke="#46B414" cap="round" width="1\.0"/>"""),
                    """<line stroke="#2979FF" cap="round" width="1.3"/>"""
                )
                customXml = customXml.replace(
                    Regex("""<line stroke="#005297" cap="round" width="1\.0"/>"""),
                    """<line stroke="#FF1744" cap="round" width="1.4"/>"""
                )
                customXml = customXml.replace(
                    Regex("""<line stroke="#000000" cap="round" width="1\.0"/>"""),
                    """<line stroke="#212121" cap="round" width="1.5"/>"""
                )
            }

            if (isSunlightBoosterEnabled(context)) {
                customXml = customXml.replace(Regex("""width="(\d+\.?\d*)"""")) { m ->
                    val w = m.groupValues[1].toFloatOrNull() ?: 1.0f
                    val boosted = String.format(java.util.Locale.US, "%.2f", w * 1.25f)
                    """width="$boosted""""
                }
            }

            // --- STRICT XML VALIDITY CHECK BEFORE WRITING TO KAROO ---
            if (!isValidXml(customXml)) {
                Log.e(TAG, "FATAL: Generated custom XML is not valid! Aborting save to protect Karoo Ride app.")
                return false
            }

            // Write validated XML to Karoo target file
            FileOutputStream(target).use { output ->
                output.write(customXml.toByteArray(Charsets.UTF_8))
                output.flush()
                output.fd.sync()
            }

            val prefs = context.getSharedPreferences("karoo_topo_maps", Context.MODE_PRIVATE)
            prefs.edit().putString("selected_theme_id", CUSTOM_THEME_ID).apply()

            val intent = android.content.Intent("io.hammerhead.intent.action.RIDE_APP_PRELOAD")
            context.sendBroadcast(intent)
            Log.d(TAG, "Custom theme verified valid and applied successfully to ${target.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate and apply custom theme", e)
            false
        }
    }
}
