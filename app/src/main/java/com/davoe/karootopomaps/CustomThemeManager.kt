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

    /**
     * Resets all colors, typography and visibility toggles back to defaults.
     */
    fun resetToDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Loads preset colors from an existing bundled theme (e.g. IGN, Gravel, Dark)
     */
    fun loadPreset(context: Context, presetThemeId: String) {
        when (presetThemeId) {
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
                setColor(context, "color_place_label", "#DDDDDD")
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
                setColor(context, "color_residential_street", "#FFFFFF")
                setColor(context, "color_gravel_track", "#8B5230")
                setColor(context, "color_place_label", "#333333")
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
            val building = if (buildingVis) getColor(context, "color_building") else "#00000000"
            val industrialVis = isElementVisible(context, "color_industrial")
            val industrial = if (industrialVis) getColor(context, "color_industrial") else bg

            customXml = customXml.replace(Regex("""<m v="residential">\s*<area mesh="true" fill="[^"]*""""), """<m v="residential"><area mesh="true" fill="$residential"""")
            if (!buildingVis) {
                // If building is disabled, remove the building polygon render rule completely
                customXml = customXml.replace(Regex("""<m e="way" k="building" v="\*"\s+zoom-min="14">[\s\S]*?</m>"""), "")
            } else {
                customXml = customXml.replace(Regex("""<m e="way" k="building" v="\*"\s+zoom-min="14">\s*<area mesh="true" fill="[^"]*""""),
                    """<m e="way" k="building" v="*" zoom-min="14"><area mesh="true" fill="$building"""")
            }
            customXml = customXml.replace(Regex("""<m v="industrial\|railway">\s*<area mesh="true" fill="[^"]*""""), """<m v="industrial|railway"><area mesh="true" fill="$industrial"""")

            // Contours
            val cMajorVis = isElementVisible(context, "color_contour_major")
            val cMinorVis = isElementVisible(context, "color_contour_minor")
            val cLabelVis = isElementVisible(context, "color_contour_label")
            val cMajor = if (cMajorVis) getColor(context, "color_contour_major") else "#00000000"
            val cMinor = if (cMinorVis) getColor(context, "color_contour_minor") else "#00000000"
            val cLabel = if (cLabelVis) getColor(context, "color_contour_label") else "#00000000"

            if (!cMinorVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="natural\|contour_ext" v="minor_contour_line\|elevation_minor\|elevation_medium"[^>]*>[\s\S]*?</m>"""), "")
            } else {
                customXml = customXml.replace(
                    Regex("""(<m e="way" k="natural\|contour_ext" v="minor_contour_line\|elevation_minor\|elevation_medium" zoom-min="14">\s*<line stroke=")[^"]*(")"""),
                    "$1$cMinor$2"
                )
            }

            if (!cMajorVis) {
                customXml = customXml.replace(Regex("""<m e="way" k="natural\|contour_ext" v="major_contour_line\|elevation_major"[^>]*>[\s\S]*?</m>"""), "")
            } else {
                customXml = customXml.replace(
                    Regex("""(<m e="way" k="natural\|contour_ext" v="major_contour_line\|elevation_major" zoom-min="11">\s*<line stroke=")[^"]*(")"""),
                    "$1$cMajor$2"
                )
            }
            customXml = customXml.replace(Regex("""fill="#5C442A""""), """fill="$cLabel"""")

            // Roads, Gravel & Trails
            val mtbVis = isElementVisible(context, "color_mtb_singletrack")
            val mtbSingletrack = if (mtbVis) getColor(context, "color_mtb_singletrack") else "#00000000"
            val cyclewayVis = isElementVisible(context, "color_cycleway")
            val cycleway = if (cyclewayVis) getColor(context, "color_cycleway") else "#00000000"
            val gravelVis = isElementVisible(context, "color_gravel_track")
            val gravel = if (gravelVis) getColor(context, "color_gravel_track") else "#00000000"

            customXml = customXml.replace(Regex("""<style-line id="path" stroke="[^"]*""""), """<style-line id="path" stroke="$mtbSingletrack"""")
            customXml = customXml.replace(Regex("""<style-line id="paved_cycleway_casing" stroke="[^"]*""""), """<style-line id="paved_cycleway_casing" stroke="$cycleway"""")
            customXml = customXml.replace(Regex("""<style-line id="unpaved_casing" stroke="[^"]*""""), """<style-line id="unpaved_casing" stroke="$gravel"""")

            // Labels & POIs
            val placeVis = isElementVisible(context, "color_place_label")
            val placeLabel = if (placeVis) getColor(context, "color_place_label") else "#00000000"
            customXml = customXml.replace(Regex("""<style-text id="road_label" k="name" fill="[^"]*""""), """<style-text id="road_label" k="name" fill="$placeLabel"""")

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
