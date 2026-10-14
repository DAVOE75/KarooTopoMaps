package com.davoe.karootopomaps

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Spinner
import com.google.android.material.materialswitch.MaterialSwitch

class CustomThemeEditorActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var btnSaveAndApply: MaterialButton
    private lateinit var btnResetDefaults: MaterialButton
    private lateinit var btnBack: ImageButton

    private lateinit var toggleFontSize: MaterialButtonToggleGroup
    private lateinit var toggleFontWeight: MaterialButtonToggleGroup
    private lateinit var spinnerFontFamily: Spinner
    private lateinit var mapPreviewView: MapPreviewView

    // Sunlight-tested outdoor color palette for instant picking on Karoo
    private val PRESET_COLORS = listOf(
        "#F5EFD7", "#F9F6F0", "#FFFFFF", "#D7E4B4", "#A3C8A7", "#88C773",
        "#1B5E20", "#B3E3F9", "#429AD2", "#0088FF", "#0D47A1", "#F48D84",
        "#EB1727", "#D32F2F", "#880E4F", "#A020F0", "#6A1B9A", "#F5D061",
        "#E5933A", "#D35400", "#8B5230", "#5C442A", "#D8D4CC", "#888888",
        "#404040", "#222222", "#121212", "#000000", "#FFD700", "#00E676"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_editor)

        container = findViewById(R.id.customElementsContainer)
        btnSaveAndApply = findViewById(R.id.btnSaveAndApply)
        btnResetDefaults = findViewById(R.id.btnResetDefaults)
        btnBack = findViewById(R.id.btnBack)

        toggleFontSize = findViewById(R.id.toggleFontSize)
        toggleFontWeight = findViewById(R.id.toggleFontWeight)
        spinnerFontFamily = findViewById(R.id.spinnerFontFamily)
        mapPreviewView = findViewById(R.id.mapPreviewView)

        btnBack.setOnClickListener { finish() }

        setupProToolsControls()
        setupTypographyControls()

        btnSaveAndApply.setOnClickListener {
            val success = CustomThemeManager.generateAndApplyCustomTheme(this)
            if (success) {
                Toast.makeText(this, getString(R.string.custom_theme_saved), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_storage_permission), Toast.LENGTH_LONG).show()
            }
        }

        btnResetDefaults.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirm_title)
                .setMessage(R.string.reset_confirm_msg)
                .setPositiveButton(R.string.btn_reset) { _, _ ->
                    CustomThemeManager.resetToDefaults(this)
                    setupProToolsControls()
                    setupTypographyControls()
                    renderElements()
                    mapPreviewView.refreshPreview()
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .show()
        }

        renderElements()
    }

    private fun setupProToolsControls() {
        val btnPresetRoad = findViewById<MaterialButton>(R.id.btnPresetRoad)
        val btnPresetMtb = findViewById<MaterialButton>(R.id.btnPresetMtb)
        val btnPresetGravel = findViewById<MaterialButton>(R.id.btnPresetGravel)
        val btnPresetDark = findViewById<MaterialButton>(R.id.btnPresetDark)

        val switchSunlight = findViewById<MaterialSwitch>(R.id.switchSunlightBooster)
        val switchWater = findViewById<MaterialSwitch>(R.id.switchWaterRadar)
        val switchMtbGrades = findViewById<MaterialSwitch>(R.id.switchMtbGrades)

        val btnExport = findViewById<MaterialButton>(R.id.btnExportProfile)
        val btnImport = findViewById<MaterialButton>(R.id.btnImportProfile)

        // Initial switch states
        switchSunlight.isChecked = CustomThemeManager.isSunlightBoosterEnabled(this)
        switchWater.isChecked = CustomThemeManager.isWaterRadarEnabled(this)
        switchMtbGrades.isChecked = CustomThemeManager.isMtbGradesEnabled(this)

        switchSunlight.setOnCheckedChangeListener { _, isChecked ->
            CustomThemeManager.setSunlightBoosterEnabled(this, isChecked)
            mapPreviewView.refreshPreview()
        }
        switchWater.setOnCheckedChangeListener { _, isChecked ->
            CustomThemeManager.setWaterRadarEnabled(this, isChecked)
            mapPreviewView.refreshPreview()
        }
        switchMtbGrades.setOnCheckedChangeListener { _, isChecked ->
            CustomThemeManager.setMtbGradesEnabled(this, isChecked)
            mapPreviewView.refreshPreview()
        }

        // 1-Touch Discipline Presets
        fun applyPreset(id: String) {
            CustomThemeManager.loadPreset(this, id)
            switchWater.isChecked = CustomThemeManager.isWaterRadarEnabled(this)
            switchMtbGrades.isChecked = CustomThemeManager.isMtbGradesEnabled(this)
            setupTypographyControls()
            renderElements()
            mapPreviewView.refreshPreview()
            Toast.makeText(this, "Preset aplicado", Toast.LENGTH_SHORT).show()
        }

        btnPresetRoad.setOnClickListener { applyPreset("road") }
        btnPresetMtb.setOnClickListener { applyPreset("mtb") }
        btnPresetGravel.setOnClickListener { applyPreset("gravel") }
        btnPresetDark.setOnClickListener { applyPreset("dark") }

        // Export Profile Code
        btnExport.setOnClickListener {
            val code = CustomThemeManager.exportProfileCode(this)
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("KarooMapProfile", code)
            clipboard.setPrimaryClip(clip)

            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_export_title)
                .setMessage("${getString(R.string.msg_profile_copied)}\n\n$code")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        // Import Profile Code
        btnImport.setOnClickListener {
            val input = EditText(this)
            input.hint = getString(R.string.dialog_import_hint)
            input.setTextColor(Color.WHITE)
            input.setHintTextColor(Color.GRAY)
            val padding = (16 * resources.displayMetrics.density).toInt()
            input.setPadding(padding, padding, padding, padding)

            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_import_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val code = input.text.toString().trim()
                    if (code.isNotEmpty() && CustomThemeManager.importProfileCode(this, code)) {
                        switchSunlight.isChecked = CustomThemeManager.isSunlightBoosterEnabled(this)
                        switchWater.isChecked = CustomThemeManager.isWaterRadarEnabled(this)
                        switchMtbGrades.isChecked = CustomThemeManager.isMtbGradesEnabled(this)
                        setupTypographyControls()
                        renderElements()
                        mapPreviewView.refreshPreview()
                        Toast.makeText(this, getString(R.string.msg_profile_imported), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.error_invalid_profile), Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .show()
        }
    }

    private fun setupTypographyControls() {
        // Size scale
        when (CustomThemeManager.getFontScale(this)) {
            0.85f -> toggleFontSize.check(R.id.btnSizeCompact)
            1.2f -> toggleFontSize.check(R.id.btnSizeLarge)
            1.4f -> toggleFontSize.check(R.id.btnSizeHuge)
            else -> toggleFontSize.check(R.id.btnSizeNormal)
        }

        toggleFontSize.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val scale = when (checkedId) {
                    R.id.btnSizeCompact -> CustomThemeManager.FONT_SCALE_COMPACT
                    R.id.btnSizeLarge -> CustomThemeManager.FONT_SCALE_LARGE
                    R.id.btnSizeHuge -> CustomThemeManager.FONT_SCALE_HUGE
                    else -> CustomThemeManager.FONT_SCALE_NORMAL
                }
                CustomThemeManager.setFontScale(this, scale)
                mapPreviewView.refreshPreview()
            }
        }

        // Weight
        when (CustomThemeManager.getFontWeight(this)) {
            CustomThemeManager.FONT_WEIGHT_NORMAL -> toggleFontWeight.check(R.id.btnWeightNormal)
            else -> toggleFontWeight.check(R.id.btnWeightBold)
        }

        toggleFontWeight.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val weight = when (checkedId) {
                    R.id.btnWeightNormal -> CustomThemeManager.FONT_WEIGHT_NORMAL
                    else -> CustomThemeManager.FONT_WEIGHT_BOLD
                }
                CustomThemeManager.setFontWeight(this, weight)
                mapPreviewView.refreshPreview()
            }
        }

        // Font Family Dropdown / Spinner
        val fontFamilies = listOf(
            Pair(getString(R.string.font_family_default), CustomThemeManager.FONT_FAMILY_DEFAULT),
            Pair(getString(R.string.font_family_condensed), CustomThemeManager.FONT_FAMILY_CONDENSED),
            Pair(getString(R.string.font_family_mono), CustomThemeManager.FONT_FAMILY_MONO)
        )

        val fontLabels = fontFamilies.map { it.first }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fontLabels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerFontFamily.adapter = adapter

        val currentFamily = CustomThemeManager.getFontFamily(this)
        val initialIndex = fontFamilies.indexOfFirst { it.second == currentFamily }.coerceAtLeast(0)
        spinnerFontFamily.setSelection(initialIndex)

        spinnerFontFamily.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ensure text inside spinner is visible with clean white text
                (view as? TextView)?.setTextColor(Color.WHITE)
                val selectedFamily = fontFamilies[position].second
                CustomThemeManager.setFontFamily(this@CustomThemeEditorActivity, selectedFamily)
                mapPreviewView.refreshPreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun renderElements() {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        var currentCategory: ElementCategory? = null

        for (element in CustomThemeSchema.ELEMENTS) {
            // Render category header if changed
            if (element.category != currentCategory) {
                currentCategory = element.category
                val catView = inflater.inflate(R.layout.item_custom_category, container, false)
                val tvCat = catView.findViewById<TextView>(R.id.tvCategoryTitle)
                val ivIcon = catView.findViewById<android.widget.ImageView>(R.id.ivCategoryIcon)
                tvCat.setText(element.category.titleRes)
                ivIcon.setImageResource(element.category.iconRes)
                container.addView(catView)
            }

            // Render element row
            val itemView = inflater.inflate(R.layout.item_custom_element, container, false)
            val tvName = itemView.findViewById<TextView>(R.id.tvElementName)
            val tvDesc = itemView.findViewById<TextView>(R.id.tvElementDesc)
            val cardColor = itemView.findViewById<MaterialCardView>(R.id.cardColorPreview)
            val viewColor = itemView.findViewById<View>(R.id.viewColorSample)
            val switchVis = itemView.findViewById<MaterialSwitch>(R.id.switchVisibility)
            val btnLineStyle = itemView.findViewById<ImageButton>(R.id.btnLineStyle)

            tvName.setText(element.nameRes)
            tvDesc.setText(element.descRes)

            val currentColorHex = CustomThemeManager.getColor(this, element.key)
            val isVisible = CustomThemeManager.isElementVisible(this, element.key)

            try {
                viewColor.setBackgroundColor(Color.parseColor(currentColorHex))
            } catch (e: Exception) {
                viewColor.setBackgroundColor(Color.GRAY)
            }

            switchVis.isChecked = isVisible
            cardColor.alpha = if (isVisible) 1.0f else 0.3f
            cardColor.isEnabled = isVisible

            if (element.isLine) {
                btnLineStyle.visibility = View.VISIBLE
                btnLineStyle.alpha = if (isVisible) 1.0f else 0.3f
                btnLineStyle.isEnabled = isVisible
                btnLineStyle.setOnClickListener {
                    showLineStyleDialog(element)
                }
            } else {
                btnLineStyle.visibility = View.GONE
            }

            switchVis.setOnCheckedChangeListener { _, isChecked ->
                CustomThemeManager.setElementVisible(this, element.key, isChecked)
                cardColor.alpha = if (isChecked) 1.0f else 0.3f
                cardColor.isEnabled = isChecked
                if (element.isLine) {
                    btnLineStyle.alpha = if (isChecked) 1.0f else 0.3f
                    btnLineStyle.isEnabled = isChecked
                }
                mapPreviewView.refreshPreview()
            }

            cardColor.setOnClickListener {
                showColorPickerDialog(element, viewColor)
            }

            container.addView(itemView)
        }
    }

    private fun showColorPickerDialog(element: CustomizableElement, colorSampleView: View) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvColorPickerTitle)
        val gridPalette = dialogView.findViewById<GridLayout>(R.id.gridPalette)
        val viewPreview = dialogView.findViewById<View>(R.id.viewSelectedColorPreview)
        val etHex = dialogView.findViewById<EditText>(R.id.etHexCode)
        val spectrumBar = dialogView.findViewById<ColorSpectrumBar>(R.id.spectrumBar)

        tvTitle.setText(element.nameRes)

        var selectedColor = CustomThemeManager.getColor(this, element.key)
        etHex.setText(selectedColor)
        try {
            val parsedInitial = Color.parseColor(selectedColor)
            viewPreview.setBackgroundColor(parsedInitial)
            spectrumBar.setColor(parsedInitial)
        } catch (ignored: Exception) {}

        val swatchCards = mutableListOf<Pair<MaterialCardView, String>>()

        fun updateSelectedSwatchHighlight(currentHex: String) {
            for ((card, hex) in swatchCards) {
                if (hex.equals(currentHex, ignoreCase = true)) {
                    card.strokeColor = Color.parseColor("#FFD700")
                    card.strokeWidth = 6
                } else {
                    card.strokeColor = Color.parseColor("#444444")
                    card.strokeWidth = 2
                }
            }
        }

        spectrumBar.onColorChangedListener = { color, hex ->
            selectedColor = hex
            etHex.setText(hex)
            viewPreview.setBackgroundColor(color)
            updateSelectedSwatchHighlight(hex)
        }

        etHex.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hex = s?.toString()?.trim() ?: ""
                if (hex.matches(Regex("""^#([A-Fa-f0-9]{6})$"""))) {
                    try {
                        val parsed = Color.parseColor(hex)
                        viewPreview.setBackgroundColor(parsed)
                        spectrumBar.setColor(parsed)
                        selectedColor = hex
                        updateSelectedSwatchHighlight(hex)
                    } catch (ignored: Exception) {}
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Populate Palette grid with 6 columns centered to fit the Karoo screen perfectly
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        // Screen width minus dialog margins and padding (approx 50dp) divided by 6 columns
        val availableWidth = (screenWidth - (48 * displayMetrics.density)).toInt()
        val swatchSize = (availableWidth / 6.5).toInt().coerceIn(48, 70)
        val swatchMargin = 4

        for (colorHex in PRESET_COLORS) {
            val isCurrent = colorHex.equals(selectedColor, ignoreCase = true)
            val swatch = MaterialCardView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = swatchSize
                    height = swatchSize
                    setMargins(swatchMargin, swatchMargin, swatchMargin, swatchMargin)
                }
                radius = swatchSize / 2f
                strokeColor = if (isCurrent) Color.parseColor("#FFD700") else Color.parseColor("#444444")
                strokeWidth = if (isCurrent) 6 else 2
                setCardBackgroundColor(Color.parseColor(colorHex))
                setOnClickListener {
                    selectedColor = colorHex
                    etHex.setText(colorHex)
                    val parsed = Color.parseColor(colorHex)
                    viewPreview.setBackgroundColor(parsed)
                    spectrumBar.setColor(parsed)
                    updateSelectedSwatchHighlight(colorHex)
                }
            }
            swatchCards.add(Pair(swatch, colorHex))
            gridPalette.addView(swatch)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_select) { _, _ ->
                val finalHex = etHex.text.toString().trim()
                if (finalHex.matches(Regex("""^#([A-Fa-f0-9]{6})$"""))) {
                    CustomThemeManager.setColor(this, element.key, finalHex)
                    try {
                        colorSampleView.setBackgroundColor(Color.parseColor(finalHex))
                    } catch (ignored: Exception) {}
                    mapPreviewView.refreshPreview()
                } else {
                    Toast.makeText(this, getString(R.string.invalid_hex_error), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showLineStyleDialog(element: CustomizableElement) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_line_style, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvLineDialogTitle)
        val toggleWidth = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleLineWidth)
        val togglePattern = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleLinePattern)

        tvTitle.setText(element.nameRes)

        var selectedWidth = CustomThemeManager.getLineWidth(this, element.key)
        var selectedPattern = CustomThemeManager.getLinePattern(this, element.key)

        when {
            selectedWidth <= 0.45f -> toggleWidth.check(R.id.btnWidthThin)
            selectedWidth <= 0.85f -> toggleWidth.check(R.id.btnWidthNormal)
            selectedWidth <= 1.25f -> toggleWidth.check(R.id.btnWidthThick)
            else -> toggleWidth.check(R.id.btnWidthExtra)
        }

        toggleWidth.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedWidth = when (checkedId) {
                    R.id.btnWidthThin -> 0.4f
                    R.id.btnWidthNormal -> 0.7f
                    R.id.btnWidthThick -> 1.1f
                    else -> 1.6f
                }
            }
        }

        when (selectedPattern) {
            "dashed" -> togglePattern.check(R.id.btnPatternDashed)
            "dotted" -> togglePattern.check(R.id.btnPatternDotted)
            else -> togglePattern.check(R.id.btnPatternSolid)
        }

        togglePattern.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedPattern = when (checkedId) {
                    R.id.btnPatternDashed -> "dashed"
                    R.id.btnPatternDotted -> "dotted"
                    else -> "solid"
                }
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_select) { _, _ ->
                CustomThemeManager.setLineWidth(this, element.key, selectedWidth)
                CustomThemeManager.setLinePattern(this, element.key, selectedPattern)
                mapPreviewView.refreshPreview()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}
