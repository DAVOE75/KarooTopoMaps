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
import com.google.android.material.materialswitch.MaterialSwitch

class CustomThemeEditorActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var btnSaveAndApply: MaterialButton
    private lateinit var btnResetDefaults: MaterialButton
    private lateinit var btnBack: ImageButton

    private lateinit var toggleFontSize: MaterialButtonToggleGroup
    private lateinit var toggleFontWeight: MaterialButtonToggleGroup
    private lateinit var toggleFontFamily: MaterialButtonToggleGroup

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
        toggleFontFamily = findViewById(R.id.toggleFontFamily)

        btnBack.setOnClickListener { finish() }

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
                .setTitle(R.string.btn_reset_defaults)
                .setMessage("Reset all colors, font settings and layer visibility back to IGN defaults?")
                .setPositiveButton("Reset") { _, _ ->
                    CustomThemeManager.resetToDefaults(this)
                    setupTypographyControls()
                    renderElements()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        renderElements()
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
            }
        }

        // Family
        when (CustomThemeManager.getFontFamily(this)) {
            CustomThemeManager.FONT_FAMILY_CONDENSED -> toggleFontFamily.check(R.id.btnFamilyCondensed)
            CustomThemeManager.FONT_FAMILY_MONO -> toggleFontFamily.check(R.id.btnFamilyMono)
            else -> toggleFontFamily.check(R.id.btnFamilyDefault)
        }

        toggleFontFamily.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val family = when (checkedId) {
                    R.id.btnFamilyCondensed -> CustomThemeManager.FONT_FAMILY_CONDENSED
                    R.id.btnFamilyMono -> CustomThemeManager.FONT_FAMILY_MONO
                    else -> CustomThemeManager.FONT_FAMILY_DEFAULT
                }
                CustomThemeManager.setFontFamily(this, family)
            }
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

        tvTitle.setText(element.nameRes)

        var selectedColor = CustomThemeManager.getColor(this, element.key)
        etHex.setText(selectedColor)
        try {
            viewPreview.setBackgroundColor(Color.parseColor(selectedColor))
        } catch (ignored: Exception) {}

        etHex.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hex = s?.toString()?.trim() ?: ""
                if (hex.matches(Regex("""^#([A-Fa-f0-9]{6})$"""))) {
                    try {
                        val parsed = Color.parseColor(hex)
                        viewPreview.setBackgroundColor(parsed)
                        selectedColor = hex
                    } catch (ignored: Exception) {}
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Populate Palette grid
        for (colorHex in PRESET_COLORS) {
            val swatch = MaterialCardView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 80
                    height = 80
                    setMargins(8, 8, 8, 8)
                }
                radius = 40f
                strokeColor = Color.parseColor("#444444")
                strokeWidth = 2
                setCardBackgroundColor(Color.parseColor(colorHex))
                setOnClickListener {
                    selectedColor = colorHex
                    etHex.setText(colorHex)
                    viewPreview.setBackgroundColor(Color.parseColor(colorHex))
                }
            }
            gridPalette.addView(swatch)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Select") { _, _ ->
                val finalHex = etHex.text.toString().trim()
                if (finalHex.matches(Regex("""^#([A-Fa-f0-9]{6})$"""))) {
                    CustomThemeManager.setColor(this, element.key, finalHex)
                    try {
                        colorSampleView.setBackgroundColor(Color.parseColor(finalHex))
                    } catch (ignored: Exception) {}
                } else {
                    Toast.makeText(this, "Invalid hex color format (use #RRGGBB)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
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
            .setPositiveButton("OK") { _, _ ->
                CustomThemeManager.setLineWidth(this, element.key, selectedWidth)
                CustomThemeManager.setLinePattern(this, element.key, selectedPattern)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
