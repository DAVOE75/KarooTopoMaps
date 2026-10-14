package com.davoe.karootopomaps

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object CustomThemeManager {

    private const val TAG = "CustomThemeManager"
    private const val PREFS_NAME = "karoo_custom_theme_colors"
    const val CUSTOM_THEME_ID = "custom_user_theme"

    /**
     * Gets user-configured color for a specific element key, or default if not set.
     */
    fun getColor(context: Context, key: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultColor = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultColorHex ?: "#000000"
        return prefs.getString(key, defaultColor) ?: defaultColor
    }

    /**
     * Saves a color for a specific element key.
     */
    fun setColor(context: Context, key: String, hexColor: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, hexColor.uppercase()).apply()
    }

    /**
     * Checks if an element is enabled/visible on the map.
     */
    fun isElementVisible(context: Context, key: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultVis = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultEnabled ?: true
        return prefs.getBoolean("${key}_visible", defaultVis)
    }

    /**
     * Sets whether an element is visible on the map.
     */
    fun setElementVisible(context: Context, key: String, visible: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("${key}_visible", visible).apply()
    }

    /**
     * Line styling: Stroke width multiplier (0.5 to 2.0)
     */
    fun getLineWidth(context: Context, key: String): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultWidth = CustomThemeSchema.ELEMENTS.find { it.key == key }?.defaultWidth ?: 0.6f
        return prefs.getFloat("${key}_width", defaultWidth)
    }

    fun setLineWidth(context: Context, key: String, width: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat("${key}_width", width).apply()
    }

    /**
     * Line styling: Pattern ("solid", "dashed", "dotted")
     */
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

    /**
     * Exports current map theme profile as a base64 or JSON string for sharing.
     */
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

    /**
     * Imports a map profile code from base64 string.
     */
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

    /**
     * Resets all colors, typography and visibility toggles back to defaults.
     */
    fun resetToDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Loads preset discipline configurations: Road, MTB, Gravel, AMOLED Dark.
     */
    fun loadPreset(context: Context, presetThemeId: String) {
        when (presetThemeId) {
            "road" -> {
                // High contrast clean road navigation
                setColor(context, "color_bg", "#FFFFFF")
                setColor(context, "color_motorway", "#E05A47")
                setColor(context, "color_primary_road", "#E5933A")
                setColor(context, "color_secondary_road", "#F5D061")
                setColor(context, "color_residential_street", "#EEEEEE")
                setLineWidth(context, "color_primary_road", 1.4f)
                setLineWidth(context, "color_secondary_road", 1.2f)
                // Disable contour noise for high speed clarity
                setElementVisible(context, "color_contour_major", false)
                setElementVisible(context, "color_contour_minor", false)
                setElementVisible(context, "color_contour_label", false)
                // Disable rough singletracks
                setElementVisible(context, "color_mtb_singletrack", false)
                setElementVisible(context, "color_gravel_track", false)
                setElementVisible(context, "color_water_body", true)
                setElementVisible(context, "color_place_label", true)
                setFontWeight(context, FONT_WEIGHT_BOLD)
            }
            "mtb" -> {
                // Ultra high precision mountain topography
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
                setColor(context, "color_mtb_singletrack", "#FF0000") // high-vis red
                setLineWidth(context, "color_mtb_singletrack", 1.2f)
                setLinePattern(context, "color_mtb_singletrack", "dashed")
                setColor(context, "color_gravel_track", "#8B5230")
                setElementVisible(context, "color_mtb_singletrack", true)
                setElementVisible(context, "color_gravel_track", true)
                setMtbGradesEnabled(context, true)
                setWaterRadarEnabled(context, true)
            }
            "gravel" -> {
                setColor(context, "color_bg", "#F9F6F0")
                setColor(context, "color_meadow", "#E2EBD3")
                setColor(context, "color_rock", "#D0C8B8")
                setColor(context, "color_water_body", "#A2D8F0")
                setColor(context, "color_river", "#3588B8")
                setColor(context, "color_forest", "#8FA882")
                setColor(context, "color_residential", "#DFD4C4")
                setColor(context, "color_contour_major", "#6D5339")
                setColor(context, "color_contour_minor", "#D7C49E")
                setColor(context, "color_contour_label", "#4A3728")
                setElementVisible(context, "color_contour_major", true)
                setElementVisible(context, "color_contour_minor", true)
                setColor(context, "color_residential_street", "#FFFFFF")
                setColor(context, "color_gravel_track", "#8B5230")
                setLineWidth(context, "color_gravel_track", 1.1f)
                setColor(context, "color_place_label", "#333333")
                setElementVisible(context, "color_gravel_track", true)
                setElementVisible(context, "color_mtb_singletrack", true)
                setWaterRadarEnabled(context, true)
            }
            "dark" -> {
                setColor(context, "color_bg", "#121212")
                setColor(context, "color_meadow", "#1C241D")
                setColor(context, "color_rock", "#2C2C2C")
                setColor(context, "color_water_body", "#103045")
                setColor(context, "color_river", "#164B6E")
                setColor(context, "color_forest", "#162818")
                setColor(context, "color_residential", "#252525")
                setColor(context, "color_contour_major", "#5A5A5A")
                setColor(context, "color_contour_minor", "#383838")
                setColor(context, "color_contour_label", "#CCCCCC")
                setColor(context, "color_residential_street", "#404040")
                setColor(context, "color_gravel_track", "#705030")
                setColor(context, "color_mtb_singletrack", "#00E676")
                setColor(context, "color_place_label", "#DDDDDD")
            }
            else -> {
                // Baseline IGN
                resetToDefaults(context)
            }
        }
    }

    /**
     * Generates a fully dynamic Mapsforge VTM theme XML with all user-customized colors
     * and applies it to the Karoo target file (/sdcard/mtb_v2.xml).
     */
    fun generateAndApplyCustomTheme(context: Context): Boolean {
        ThemeManager.ensureBackupExists()
        val target = ThemeManager.getTargetThemeFile()
        val contourMode = ThemeManager.getContourMode(context)

        return try {
            // Load base template (IGN topo as baseline template)
            val template = context.assets.open("themes/ign_topo.xml").bufferedReader(Charsets.UTF_8).use { it.readText() }

            // Replace colors dynamically based on user preferences
            var customXml = template

            val bg = getColor(context, "color_bg")
            customXml = customXml.replace(Regex("""map-background="[^"]*""""), """map-background="$bg"""")
            customXml = customXml.replace(Regex("""<m e="way" k="natural" v="nosea">\s*<area mesh="true" fill="[^"]*"/>"""),
                """<m e="way" k="natural" v="nosea"><area mesh="true" fill="$bg"/>""")

            // Water
            val waterVis = isElementVisible(context, "color_water_body")
            val water = if (waterVis) getColor(context, "color_water_body") else "#00000000"
            val riverVis = isElementVisible(context, "color_river")
            val river = if (riverVis) getColor(context, "color_river") else "#00000000"

            customXml = customXml.replace(Regex("""<style-area fill="[^"]*" id="water""""), """<style-area fill="$water" id="water"""")
            customXml = customXml.replace(Regex("""<style-line cap="butt" id="water" stroke="[^"]*""""), """<style-line cap="butt" id="water" stroke="$river"""")

            // Forest & Meadow & Rock
            val forestVis = isElementVisible(context, "color_forest")
            val forest = if (forestVis) getColor(context, "color_forest") else bg
            val meadowVis = isElementVisible(context, "color_meadow")
            val meadow = if (meadowVis) getColor(context, "color_meadow") else bg
            val rockVis = isElementVisible(context, "color_rock")
            val rock = if (rockVis) getColor(context, "color_rock") else bg
            val sandVis = isElementVisible(context, "color_sand")
            val sand = if (sandVis) getColor(context, "color_sand") else bg
            val parkVis = isElementVisible(context, "color_park")
            val park = if (parkVis) getColor(context, "color_park") else bg

            customXml = customXml.replace(Regex("""<m v="forest">\s*<area mesh="true" fill="[^"]*"/>"""), """<m v="forest"><area mesh="true" fill="$forest"/>""")
            customXml = customXml.replace(Regex("""<m v="wood">\s*<area mesh="true" fill="[^"]*"/>"""), """<m v="wood"><area mesh="true" fill="$forest"/>""")
            customXml = customXml.replace(Regex("""<m v="meadow">\s*<area mesh="true" fill="[^"]*"/>"""), """<m v="meadow"><area mesh="true" fill="$meadow"/>""")
            customXml = customXml.replace(Regex("""<m v="rock\|bare_rock\|stone\|scree\|glacier\|cliff">\s*<area mesh="true" fill="[^"]*"/>"""), """<m v="rock|bare_rock|stone|scree|glacier|cliff"><area mesh="true" fill="$rock"/>""")
            customXml = customXml.replace(Regex("""<m v="sand\|beach">\s*<area mesh="true" fill="[^"]*"/>"""), """<m v="sand|beach"><area mesh="true" fill="$sand"/>""")
            customXml = customXml.replace(Regex("""<m v="park\|common\|green\|golf_course">\s*<area mesh="true" fill="[^"]*"/>"""), """<m v="park|common|green|golf_course"><area mesh="true" fill="$park"/>""")

            // Urban & Buildings
            val residentialVis = isElementVisible(context, "color_residential")
            val residential = if (residentialVis) getColor(context, "color_residential") else bg
            val buildingVis = isElementVisible(context, "color_building")
            val building = if (buildingVis) getColor(context, "color_building") else bg
            val industrialVis = isElementVisible(context, "color_industrial")
            val industrial = if (industrialVis) getColor(context, "color_industrial") else bg

            if (!residentialVis) {
                customXml = customXml.replace(Regex("""<m v="residential">\s*<area mesh="true"[^>]*/>"""), """<m v="residential"><area mesh="true" fill="$bg"/>""")
                customXml = customXml.replace(Regex("""<m v="retail">\s*<area mesh="true"[^>]*/>"""), """<m v="retail"><area mesh="true" fill="$bg"/>""")
            } else {
                customXml = customXml.replace(Regex("""<m v="residential">\s*<area mesh="true"[^>]*/>"""), """<m v="residential"><area mesh="true" fill="$residential" stroke="$residential" stroke-width="0.1"/>""")
            }

            if (!buildingVis) {
                // Remove building footprint rule entirely so buildings are completely invisible
                customXml = customXml.replace(Regex("""<m e="way" k="building" v="\*"\s+zoom-min="14">[\s\S]*?</m>"""), "")
            } else {
                customXml = customXml.replace(Regex("""<m e="way" k="building" v="\*"\s+zoom-min="14">\s*<area mesh="true"[^>]*/>"""),
                    """<m e="way" k="building" v="*" zoom-min="14"><area mesh="true" fill="$building" stroke="$building" stroke-width="0.3"/>""")
            }
            if (!industrialVis) {
                customXml = customXml.replace(Regex("""<m v="industrial\|railway">\s*<area mesh="true"[^>]*/>"""), """<m v="industrial|railway"><area mesh="true" fill="$bg"/>""")
            } else {
                customXml = customXml.replace(Regex("""<m v="industrial\|railway">\s*<area mesh="true"[^>]*/>"""), """<m v="industrial|railway"><area mesh="true" fill="$industrial"/>""")
            }

            // Contours
            val cMajorVis = isElementVisible(context, "color_contour_major")
            val cMinorVis = isElementVisible(context, "color_contour_minor")
            val cLabelVis = isElementVisible(context, "color_contour_label")
            val cMajor = if (cMajorVis) getColor(context, "color_contour_major") else "#00000000"
            val cMinor = if (cMinorVis) getColor(context, "color_contour_minor") else "#00000000"
            val cLabel = if (cLabelVis) getColor(context, "color_contour_label") else "#00000000"

            val cMajorWidth = getLineWidth(context, "color_contour_major")
            val cMinorWidth = getLineWidth(context, "color_contour_minor")
            val cMajorPattern = getLinePattern(context, "color_contour_major")
            val cMinorPattern = getLinePattern(context, "color_contour_minor")

            fun patternToDash(pattern: String): String = when (pattern) {
                "dashed" -> """ dasharray="8,6""""
                "dotted" -> """ dasharray="3,3""""
                else -> ""
            }

            if (!cMinorVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="natural\|contour_ext" v="minor_contour_line\|elevation_minor\|elevation_medium"[^>]*>[\s\S]*?</m>"""), "")
            } else {
                val dash = patternToDash(cMinorPattern)
                customXml = customXml.replace(
                    Regex("""(<m e="way" k="natural\|contour_ext" v="minor_contour_line\|elevation_minor\|elevation_medium" zoom-min="14">\s*<line stroke=")[^"]*("\s*width=")[^"]*(")"""),
                    "$1$cMinor$2$cMinorWidth$3$dash"
                )
            }

            if (!cMajorVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="natural\|contour_ext" v="major_contour_line\|elevation_major"[^>]*>[\s\S]*?</m>"""), "")
            } else {
                val dash = patternToDash(cMajorPattern)
                customXml = customXml.replace(
                    Regex("""(<m e="way" k="natural\|contour_ext" v="major_contour_line\|elevation_major" zoom-min="11">\s*<line stroke=")[^"]*("\s*width=")[^"]*(")"""),
                    "$1$cMajor$2$cMajorWidth$3$dash"
                )
            }
            customXml = customXml.replace(Regex("""fill="#5C442A""""), """fill="$cLabel"""")

            // Roads, Highways & Streets
            val motorwayVis = isElementVisible(context, "color_motorway")
            val priRoadVis = isElementVisible(context, "color_primary_road")
            val secRoadVis = isElementVisible(context, "color_secondary_road")
            val resStreetVis = isElementVisible(context, "color_residential_street")
            val gravelVis = isElementVisible(context, "color_gravel_track")
            val mtbVis = isElementVisible(context, "color_mtb_singletrack")
            val cyclewayVis = isElementVisible(context, "color_cycleway")

            // 1. Motorways / Trunks
            if (!motorwayVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="motorway\|motorway_link\|trunk\|trunk_link">[\s\S]*?</m>"""), "")
            } else {
                val mwColor = getColor(context, "color_motorway")
                val mwWidth = getLineWidth(context, "color_motorway")
                customXml = customXml.replace(
                    Regex("""<m e="way" k="highway" v="motorway\|motorway_link\|trunk\|trunk_link">[\s\S]*?</m>"""),
                    """<m e="way" k="highway" v="motorway|motorway_link|trunk|trunk_link"><line stroke="$mwColor" cap="round" width="$mwWidth"/><text use="road_label" size="13" priority="14"/></m>"""
                )
            }

            // 2. Primary roads (casings + fills)
            if (!priRoadVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="primary\|primary_link">[\s\S]*?</m>"""), "")
            } else {
                val priColor = getColor(context, "color_primary_road")
                val priWidth = getLineWidth(context, "color_primary_road")
                // Replace inner fill
                customXml = customXml.replace(Regex("""(<m e="way" k="highway" v="primary\|primary_link">\s*<m zoom-min="12">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*(")"""),
                    "$1$priColor$2$priWidth$3")
            }

            // 3. Secondary & Tertiary roads (casings + fills)
            if (!secRoadVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="secondary\|secondary_link">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="tertiary\|tertiary_link">[\s\S]*?</m>"""), "")
            } else {
                val secColor = getColor(context, "color_secondary_road")
                val secWidth = getLineWidth(context, "color_secondary_road")
                customXml = customXml.replace(Regex("""(<m e="way" k="highway" v="secondary\|secondary_link">\s*<m zoom-min="13">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*(")"""),
                    "$1$secColor$2$secWidth$3")
            }

            // 4. Residential, unclassified, service, living_street, pedestrian
            if (!resStreetVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="residential\|living_street">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="unclassified\|road">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="service">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="pedestrian">[\s\S]*?</m>"""), "")
            } else {
                val resColor = getColor(context, "color_residential_street")
                val resWidth = getLineWidth(context, "color_residential_street")
                customXml = customXml.replace(Regex("""(<m e="way" k="highway" v="residential\|living_street">\s*<m zoom-min="15">\s*<line stroke=")[^"]*("\s*cap="round"\s*width=")[^"]*(")"""),
                    "$1$resColor$2$resWidth$3")
            }

            // 5. Cycling networks & Cycleways
            if (!cyclewayVis) {
                customXml = customXml.replace(Regex("""<!-- Cycling networks[\s\S]*?</m>\s*</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="cycleway">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<style-line id="paved_cycleway_casing"[^/]*/>"""), """<style-line id="paved_cycleway_casing" stroke="#00000000" width="0.0"/>""")
                customXml = customXml.replace(Regex("""<style-line id="unpaved_cycleway_casing"[^/]*/>"""), """<style-line id="unpaved_cycleway_casing" stroke="#00000000" width="0.0"/>""")
                customXml = customXml.replace(Regex("""<style-line id="cycling_path"[^/]*/>"""), """<style-line id="cycling_path" stroke="#00000000" width="0.0"/>""")
            } else {
                val cycleway = getColor(context, "color_cycleway")
                val cyclewayWidth = getLineWidth(context, "color_cycleway")
                customXml = customXml.replace(Regex("""<style-line id="paved_cycleway_casing"[^/]*/>"""), """<style-line id="paved_cycleway_casing" stroke="$cycleway" width="$cyclewayWidth" cap="round"/>""")
            }

            // 6. Gravel & Tracks & Singletracks
            val mtbSingletrack = if (mtbVis) getColor(context, "color_mtb_singletrack") else "#00000000"
            val mtbWidth = if (mtbVis) getLineWidth(context, "color_mtb_singletrack") else 0.0f
            val mtbPattern = getLinePattern(context, "color_mtb_singletrack")
            val mtbDash = when (mtbPattern) {
                "solid" -> ""
                "dotted" -> """ dasharray="3,3""""
                else -> """ dasharray="6,4""""
            }

            val gravel = if (gravelVis) getColor(context, "color_gravel_track") else "#00000000"
            val gravelWidth = if (gravelVis) getLineWidth(context, "color_gravel_track") else 0.0f

            if (!gravelVis && !mtbVis) {
                // Strip all path/track/bridleway and footways completely
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="path\|track\|bridleway">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="way" k="highway" v="footway">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<style-line id="path"[^/]*/>"""), """<style-line id="path" stroke="#00000000" width="0.0"/>""")
                customXml = customXml.replace(Regex("""<style-line id="unpaved_casing"[^/]*/>"""), """<style-line id="unpaved_casing" stroke="#00000000" width="0.0"/>""")
            } else {
                customXml = customXml.replace(
                    Regex("""<style-line id="path"[^/]*/>"""),
                    """<style-line id="path" stroke="$mtbSingletrack" width="$mtbWidth"$mtbDash cap="round"/>"""
                )
                customXml = customXml.replace(
                    Regex("""<style-line id="unpaved_casing"[^/]*/>"""),
                    """<style-line id="unpaved_casing" stroke="$gravel" width="$gravelWidth" cap="round"/>"""
                )
            }

            // 7. Railways
            if (!residentialVis && !resStreetVis) {
                // If user disabled town/streets, also strip railway lines
                customXml = customXml.replace(Regex("""<m e="way" k="railway">[\s\S]*?</m>"""), "")
            }

            // Labels & POIs
            val placeVis = isElementVisible(context, "color_place_label")
            if (!placeVis) {
                customXml = customXml.replace(Regex("""<m e="node" k="place">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<style-text id="road_label"[^/]*/>"""), """<style-text id="road_label" k="name" fill="#00000000"/>""")
            } else {
                val placeLabel = getColor(context, "color_place_label")
                customXml = customXml.replace(Regex("""<style-text id="road_label" k="name" fill="[^"]*""""), """<style-text id="road_label" k="name" fill="$placeLabel"""")
            }

            // POIs (amenities, fountains, shops)
            val fountainVis = isElementVisible(context, "color_water_fountain")
            val cafeVis = isElementVisible(context, "color_cafe_food")
            val shopVis = isElementVisible(context, "color_bike_shop")
            if (!fountainVis && !cafeVis && !shopVis) {
                customXml = customXml.replace(Regex("""<m e="node" k="amenity">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="node" k="shop">[\s\S]*?</m>"""), "")
                customXml = customXml.replace(Regex("""<m e="node" k="tourism">[\s\S]*?</m>"""), "")
            }

            // Typography & Font modifications
            val fontScale = getFontScale(context)
            val fontWeight = getFontWeight(context)
            val fontFamily = getFontFamily(context)

            // Adjust size attributes in captions and text tags according to fontScale
            if (fontScale != 1.0f) {
                customXml = customXml.replace(Regex("""size="(\d+)"""")) { m ->
                    val original = m.groupValues[1].toIntOrNull() ?: 12
                    val scaled = (original * fontScale).toInt().coerceAtLeast(8)
                    """size="$scaled""""
                }
            }

            // Adjust font style (normal vs bold)
            if (fontWeight == FONT_WEIGHT_NORMAL) {
                customXml = customXml.replace(Regex("""style="bold""""), """style="normal"""")
            } else if (fontWeight == FONT_WEIGHT_BOLD) {
                customXml = customXml.replace(Regex("""style="normal""""), """style="bold"""")
            }

            // Adjust font family
            if (fontFamily != FONT_FAMILY_DEFAULT) {
                customXml = customXml.replace(Regex("""font-family="[^"]*""""), """font-family="$fontFamily"""")
            }

            // --- PRO CYCLIST FEATURES INJECTION ---

            // 1. Survival Drinking Water Radar: Highlight water fountains with max zoom visibility & large icon
            if (isWaterRadarEnabled(context)) {
                customXml = customXml.replace(
                    Regex("""<m v="drinking_water" zoom-min="15">[\s\S]*?</m>"""),
                    """<m v="drinking_water" zoom-min="11"><symbol id="drinking-water" symbol-percent="175" src="file:/icons/drinking-water-15-bg.svg"/><text use="poi_label" size="14" fill="#0088FF" priority="1"/></m>"""
                )
            }

            // 2. IMBA MTB Grading: Enforce distinct green, blue, red, black trail colors
            if (isMtbGradesEnabled(context)) {
                customXml = customXml.replace(
                    Regex("""<line stroke="#854E85" cap="round" width="1.0"/>"""),
                    """<line stroke="#00C853" cap="round" width="1.2"/>""" // Grade 1: Easy Green
                )
                customXml = customXml.replace(
                    Regex("""<line stroke="#46B414" cap="round" width="1.0"/>"""),
                    """<line stroke="#2979FF" cap="round" width="1.3"/>""" // Grade 2: Moderate Blue
                )
                customXml = customXml.replace(
                    Regex("""<line stroke="#005297" cap="round" width="1.0"/>"""),
                    """<line stroke="#FF1744" cap="round" width="1.4"/>""" // Grade 3: Difficult Red
                )
                customXml = customXml.replace(
                    Regex("""<line stroke="#000000" cap="round" width="1.0"/>"""),
                    """<line stroke="#212121" cap="round" width="1.5"/>""" // Grade 4: Very Difficult Black
                )
            }

            // 3. High-Glare Sunlight Booster: Adds dark high-contrast borders & boldizes text
            if (isSunlightBoosterEnabled(context)) {
                // Boost stroke widths by +20% and force bold font
                customXml = customXml.replace(Regex("""width="(\d+\.?\d*)"""")) { m ->
                    val w = m.groupValues[1].toFloatOrNull() ?: 1.0f
                    val boosted = String.format(java.util.Locale.US, "%.2f", w * 1.25f)
                    """width="$boosted""""
                }
                customXml = customXml.replace(Regex("""stroke-width="(\d+\.?\d*)"""")) { m ->
                    val sw = m.groupValues[1].toFloatOrNull() ?: 0.3f
                    val boosted = String.format(java.util.Locale.US, "%.2f", (sw * 1.4f).coerceAtLeast(0.5f))
                    """stroke-width="$boosted""""
                }
                customXml = customXml.replace(Regex("""style="normal""""), """style="bold"""")
            }

            // Write to Karoo file
            FileOutputStream(target).use { output ->
                output.write(customXml.toByteArray(Charsets.UTF_8))
                output.flush()
                output.fd.sync()
            }

            // Save active theme ID
            val prefs = context.getSharedPreferences("karoo_topo_maps", Context.MODE_PRIVATE)
            prefs.edit().putString("selected_theme_id", CUSTOM_THEME_ID).apply()

            // Trigger Karoo refresh
            val intent = android.content.Intent("io.hammerhead.intent.action.RIDE_APP_PRELOAD")
            context.sendBroadcast(intent)
            Log.d(TAG, "Custom theme applied successfully to ${target.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate and apply custom theme", e)
            false
        }
    }
}
