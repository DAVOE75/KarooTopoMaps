package com.davoe.karootopomaps

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ThemeManager {
    private const val TAG = "ThemeManager"
    private const val TARGET_THEME_NAME = "mtb_v2.xml"
    private const val BACKUP_THEME_NAME = "mtb_v2.original.backup.xml"

    val AVAILABLE_THEMES = listOf(
        MapTheme(
            id = "ign_topo",
            nameRes = R.string.theme_ign_name,
            descriptionRes = R.string.theme_ign_desc,
            assetFileName = "themes/ign_topo.xml",
            badgeRes = R.string.badge_rec
        ),
        MapTheme(
            id = "vintage",
            nameRes = R.string.theme_vintage_name,
            descriptionRes = R.string.theme_vintage_desc,
            assetFileName = "themes/theme_vintage.xml"
        ),
        MapTheme(
            id = "gravel",
            nameRes = R.string.theme_gravel_name,
            descriptionRes = R.string.theme_gravel_desc,
            assetFileName = "themes/theme_gravel.xml"
        ),
        MapTheme(
            id = "dem",
            nameRes = R.string.theme_dem_name,
            descriptionRes = R.string.theme_dem_desc,
            assetFileName = "themes/theme_dem.xml"
        ),
        MapTheme(
            id = "trail",
            nameRes = R.string.theme_trail_name,
            descriptionRes = R.string.theme_trail_desc,
            assetFileName = "themes/theme_trail.xml"
        ),
        MapTheme(
            id = "granite",
            nameRes = R.string.theme_granite_name,
            descriptionRes = R.string.theme_granite_desc,
            assetFileName = "themes/theme_granite.xml"
        ),
        MapTheme(
            id = "dark",
            nameRes = R.string.theme_dark_name,
            descriptionRes = R.string.theme_dark_desc,
            assetFileName = "themes/theme_dark.xml"
        ),
        MapTheme(
            id = "elevate_mtb",
            nameRes = R.string.theme_elevate_name,
            descriptionRes = R.string.theme_elevate_desc,
            assetFileName = "themes/elevate_mtb.xml"
        ),
        MapTheme(
            id = "alt_mtb",
            nameRes = R.string.theme_alt_name,
            descriptionRes = R.string.theme_alt_desc,
            assetFileName = "themes/alt_mtb.xml"
        )
    )

    fun getTargetThemeFile(): File {
        val storageDir = Environment.getExternalStorageDirectory()
        return File(storageDir, TARGET_THEME_NAME)
    }

    private fun getBackupFile(): File {
        val storageDir = Environment.getExternalStorageDirectory()
        return File(storageDir, BACKUP_THEME_NAME)
    }

    fun ensureBackupExists(): Boolean {
        val target = getTargetThemeFile()
        val backup = getBackupFile()

        if (!backup.exists() && target.exists()) {
            return try {
                target.copyTo(backup, overwrite = false)
                Log.d(TAG, "Backup created: ${backup.absolutePath}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create backup", e)
                false
            }
        }
        return true
    }

    fun applyTheme(context: Context, theme: MapTheme): Boolean {
        ensureBackupExists()
        val target = getTargetThemeFile()

        return try {
            context.assets.open(theme.assetFileName).use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                    output.flush()
                    output.fd.sync()
                }
            }
            saveCurrentThemeId(context, theme.id)
            Log.d(TAG, "Theme applied: ${theme.id} to ${target.absolutePath}")

            // Trigger Karoo system refresh intents
            try {
                val intent = android.content.Intent("io.hammerhead.intent.action.RIDE_APP_PRELOAD")
                context.sendBroadcast(intent)
            } catch (ignored: Exception) {}

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme ${theme.id}", e)
            false
        }
    }

    fun restoreOriginalTheme(context: Context): Boolean {
        val target = getTargetThemeFile()

        // 1. Try restoring from factory original_karoo.xml asset
        try {
            context.assets.open("themes/original_karoo.xml").use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                }
            }
            saveCurrentThemeId(context, "original_karoo")
            Log.d(TAG, "Restored from original_karoo asset")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from original asset, trying backup", e)
        }

        // 2. Fallback to backup file if exists
        val backup = getBackupFile()
        if (backup.exists()) {
            return try {
                backup.copyTo(target, overwrite = true)
                saveCurrentThemeId(context, "original_karoo")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring backup", e)
                false
            }
        }

        return false
    }

    fun getCurrentThemeId(context: Context): String {
        val prefs = context.getSharedPreferences("karoo_topo_maps", Context.MODE_PRIVATE)
        return prefs.getString("selected_theme_id", "ign_topo") ?: "ign_topo"
    }

    private fun saveCurrentThemeId(context: Context, themeId: String) {
        val prefs = context.getSharedPreferences("karoo_topo_maps", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_theme_id", themeId).apply()
    }
}
