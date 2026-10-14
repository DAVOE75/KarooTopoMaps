package com.davoe.karootopomaps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var themesContainer: LinearLayout
    private lateinit var btnRestore: MaterialButton
    private lateinit var toggleContourMode: MaterialButtonToggleGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        themesContainer = findViewById(R.id.themesContainer)
        btnRestore = findViewById(R.id.btnRestore)
        toggleContourMode = findViewById(R.id.toggleContourMode)

        setupContourToggle()

        btnRestore.setOnClickListener {
            handleRestore()
        }

        checkAndRequestPermissions()
        renderThemes()
    }

    private fun setupContourToggle() {
        val currentMode = ThemeManager.getContourMode(this)
        when (currentMode) {
            ThemeManager.CONTOUR_MODE_NONE -> toggleContourMode.check(R.id.btnContourNone)
            ThemeManager.CONTOUR_MODE_ALL -> toggleContourMode.check(R.id.btnContourAll)
            else -> toggleContourMode.check(R.id.btnContourMajor)
        }

        toggleContourMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newMode = when (checkedId) {
                    R.id.btnContourNone -> ThemeManager.CONTOUR_MODE_NONE
                    R.id.btnContourAll -> ThemeManager.CONTOUR_MODE_ALL
                    else -> ThemeManager.CONTOUR_MODE_MAJOR
                }
                if (newMode != ThemeManager.getContourMode(this)) {
                    ThemeManager.setContourMode(this, newMode)
                    // If a custom theme is currently active, re-apply it with the new contour settings
                    val activeId = ThemeManager.getCurrentThemeId(this)
                    if (activeId != null) {
                        ThemeManager.reapplyCurrentTheme(this)
                        Toast.makeText(this, getString(R.string.theme_applied_success), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        renderThemes()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    101
                )
            }
        }
    }

    private fun renderThemes() {
        themesContainer.removeAllViews()
        val currentThemeId = ThemeManager.getCurrentThemeId(this)
        val inflater = LayoutInflater.from(this)

        for (theme in ThemeManager.AVAILABLE_THEMES) {
            val cardView = inflater.inflate(R.layout.item_theme_card, themesContainer, false) as MaterialCardView
            val tvName = cardView.findViewById<TextView>(R.id.tvThemeName)
            val tvDesc = cardView.findViewById<TextView>(R.id.tvThemeDesc)
            val tvBadge = cardView.findViewById<TextView>(R.id.tvBadge)
            val cardPreview = cardView.findViewById<MaterialCardView>(R.id.cardPreview)
            val ivPreview = cardView.findViewById<ImageView>(R.id.ivPreview)
            val btnSelect = cardView.findViewById<MaterialButton>(R.id.btnSelectTheme)

            tvName.text = getString(theme.nameRes)
            tvDesc.text = getString(theme.descriptionRes)

            if (theme.previewDrawableRes != null) {
                cardPreview.visibility = View.VISIBLE
                ivPreview.setImageResource(theme.previewDrawableRes)
            } else {
                cardPreview.visibility = View.GONE
            }

            val isSelected = (theme.id == currentThemeId)

            if (isSelected) {
                cardView.strokeColor = 0xFFFFD700.toInt()
                cardView.strokeWidth = 4
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = getString(R.string.badge_active)
                tvBadge.setBackgroundColor(0xFF2E7D32.toInt())
                btnSelect.text = getString(R.string.btn_active)
                btnSelect.isEnabled = false
                btnSelect.setBackgroundColor(0xFF333333.toInt())
                btnSelect.setTextColor(0xFF888888.toInt())
            } else {
                cardView.strokeColor = 0xFF333333.toInt()
                cardView.strokeWidth = 2
                if (theme.badgeRes != null) {
                    tvBadge.visibility = View.VISIBLE
                    tvBadge.text = getString(theme.badgeRes)
                    tvBadge.setBackgroundColor(0xFFC77800.toInt())
                } else {
                    tvBadge.visibility = View.GONE
                }
                btnSelect.text = getString(R.string.btn_apply)
                btnSelect.isEnabled = true
                btnSelect.setBackgroundColor(0xFFFFD700.toInt())
                btnSelect.setTextColor(0xFF000000.toInt())
            }

            btnSelect.setOnClickListener {
                applyTheme(theme)
            }

            themesContainer.addView(cardView)
        }
    }

    private fun applyTheme(theme: MapTheme) {
        val success = ThemeManager.applyTheme(this, theme)
        if (success) {
            Toast.makeText(this, getString(R.string.theme_applied_success), Toast.LENGTH_SHORT).show()
            renderThemes()
        } else {
            Toast.makeText(this, getString(R.string.error_storage_permission), Toast.LENGTH_LONG).show()
        }
    }

    private fun handleRestore() {
        val success = ThemeManager.restoreOriginalTheme(this)
        if (success) {
            Toast.makeText(this, getString(R.string.theme_restore_success), Toast.LENGTH_SHORT).show()
            renderThemes()
        } else {
            Toast.makeText(this, getString(R.string.error_storage_permission), Toast.LENGTH_LONG).show()
        }
    }
}
