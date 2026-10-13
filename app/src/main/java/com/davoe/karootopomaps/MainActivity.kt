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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var themesContainer: LinearLayout
    private lateinit var btnRestore: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        themesContainer = findViewById(R.id.themesContainer)
        btnRestore = findViewById(R.id.btnRestore)

        btnRestore.setOnClickListener {
            handleRestore()
        }

        checkAndRequestPermissions()
        renderThemes()
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
        val activeIs3D = ThemeManager.isTheme3DActive(this)
        val inflater = LayoutInflater.from(this)

        for (theme in ThemeManager.AVAILABLE_THEMES) {
            val cardView = inflater.inflate(R.layout.item_theme_card, themesContainer, false) as MaterialCardView
            val tvName = cardView.findViewById<TextView>(R.id.tvThemeName)
            val tvDesc = cardView.findViewById<TextView>(R.id.tvThemeDesc)
            val tvBadge = cardView.findViewById<TextView>(R.id.tvBadge)
            val cardPreview = cardView.findViewById<MaterialCardView>(R.id.cardPreview)
            val ivPreview = cardView.findViewById<ImageView>(R.id.ivPreview)
            val cardOptions = cardView.findViewById<MaterialCardView>(R.id.cardOptions)
            val switchRelief3D = cardView.findViewById<SwitchMaterial>(R.id.switchRelief3D)
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

            if (theme.has3DReliefOption) {
                cardOptions.visibility = View.VISIBLE
                val pref3D = ThemeManager.isTheme3DPreference(this, theme.id)
                switchRelief3D.setOnCheckedChangeListener(null)
                switchRelief3D.isChecked = if (isSelected) activeIs3D else pref3D

                switchRelief3D.setOnCheckedChangeListener { _, isChecked ->
                    ThemeManager.setTheme3DPreference(this, theme.id, isChecked)
                    if (isSelected) {
                        if (isChecked != activeIs3D) {
                            btnSelect.text = getString(R.string.btn_apply_change)
                            btnSelect.isEnabled = true
                            btnSelect.setBackgroundColor(0xFFFFD700.toInt())
                            btnSelect.setTextColor(0xFF000000.toInt())
                        } else {
                            btnSelect.text = getString(R.string.btn_active)
                            btnSelect.isEnabled = false
                            btnSelect.setBackgroundColor(0xFF333333.toInt())
                            btnSelect.setTextColor(0xFF888888.toInt())
                        }
                    }
                }
            } else {
                cardOptions.visibility = View.GONE
            }

            if (isSelected) {
                cardView.strokeColor = 0xFFFFD700.toInt()
                cardView.strokeWidth = 4
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = if (theme.has3DReliefOption) {
                    if (activeIs3D) getString(R.string.badge_active_3d) else getString(R.string.badge_active_flat)
                } else {
                    getString(R.string.badge_active)
                }
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
                val use3D = if (theme.has3DReliefOption) switchRelief3D.isChecked else false
                applyTheme(theme, use3D)
            }

            themesContainer.addView(cardView)
        }
    }

    private fun applyTheme(theme: MapTheme, use3D: Boolean = false) {
        val success = ThemeManager.applyTheme(this, theme, use3D)
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
