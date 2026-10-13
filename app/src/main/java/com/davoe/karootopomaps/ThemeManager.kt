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
            previewDrawableRes = R.drawable.preview_ign,
            badgeRes = R.string.badge_rec
        ),
        MapTheme(
            id = "vintage",
            nameRes = R.string.theme_vintage_name,
            descriptionRes = R.string.theme_vintage_desc,
            assetFileName = "themes/theme_vintage.xml",
            previewDrawableRes = R.drawable.preview_vintage
        ),
        MapTheme(
            id = "gravel",
            nameRes = R.string.theme_gravel_name,
            descriptionRes = R.string.theme_gravel_desc,
            assetFileName = "themes/theme_gravel.xml",
            previewDrawableRes = R.drawable.preview_gravel
        ),
        MapTheme(
            id = "dem",
            nameRes = R.string.theme_dem_name,
            descriptionRes = R.string.theme_dem_desc,
            assetFileName = "themes/theme_dem.xml",
            previewDrawableRes = R.drawable.preview_dem
        ),
        MapTheme(
            id = "trail",
            nameRes = R.string.theme_trail_name,
            descriptionRes = R.string.theme_trail_desc,
            assetFileName = "themes/theme_trail.xml",
            previewDrawableRes = R.drawable.preview_trail
        ),
        MapTheme(
            id = "granite",
            nameRes = R.string.theme_granite_name,
            descriptionRes = R.string.theme_granite_desc,
            assetFileName = "themes/theme_granite.xml",
            previewDrawableRes = R.drawable.preview_granite
        ),
        MapTheme(
            id = "dark",
            nameRes = R.string.theme_dark_name,
            descriptionRes = R.string.theme_dark_desc,
            assetFileName = "themes/theme_dark.xml",
            previewDrawableRes = R.drawable.preview_dark
        ),
        MapTheme(
            id = "elevate_mtb",
            nameRes = R.string.theme_elevate_name,
            descriptionRes = R.string.theme_elevate_desc,
            assetFileName = "themes/elevate_mtb.xml",
            previewDrawableRes = R.drawable.preview_elevate
        ),
        MapTheme(
            id = "alt_mtb",
            nameRes = R.string.theme_alt_name,
            descriptionRes = R.string.theme_alt_desc,
            assetFileName = "themes/alt_mtb.xml",
            previewDrawableRes = R.drawable.preview_alt
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

            triggerKarooRefresh(context)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme ${theme.id}", e)
            false
        }
    }

    fun restoreOriginalTheme(context: Context): Boolean {
        val target = getTargetThemeFile()
        val backup = getBackupFile()

        // 1. Prefer user's authentic device backup created when app first ran
        if (backup.exists()) {
            return try {
                backup.copyTo(target, overwrite = true)
                saveCurrentThemeId(context, null)
                Log.d(TAG, "Restored original theme from device backup: ${backup.absolutePath}")
                triggerKarooRefresh(context)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring from device backup, trying bundled fallback", e)
                restoreFromBundledAsset(context, target)
            }
        }

        // 2. Fallback to factory bundled original_karoo.xml asset if no device backup is found
        return restoreFromBundledAsset(context, target)
    }

    private fun restoreFromBundledAsset(context: Context, target: File): Boolean {
        return try {
            context.assets.open("themes/original_karoo.xml").use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                    output.flush()
                    output.fd.sync()
                }
            }
            saveCurrentThemeId(context, null)
            Log.d(TAG, "Restored original theme from bundled asset")
            triggerKarooRefresh(context)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from bundled original_karoo asset", e)
            false
        }
    }

    private fun triggerKarooRefresh(context: Context) {
        try {
            val intent = android.content.Intent("io.hammerhead.intent.action.RIDE_APP_PRELOAD")
            context.sendBroadcast(intent)
        } catch (ignored: Exception) {}
    }

    fun getCurrentThemeId(context: Context): String? {
        val prefs = context.getSharedPreferences("karoo_topo_maps", Context.MODE_PRIVATE)
        return prefs.getString("selected_theme_id", null)
    }

    private fun saveCurrentThemeId(context: Context, themeId: String?) {
        val prefs = context.getSharedPreferences("karoo_topo_maps", Context.MODE_PRIVATE)
        if (themeId != null) {
            prefs.edit().putString("selected_theme_id", themeId).apply()
        } else {
            prefs.edit().remove("selected_theme_id").apply()
        }
    }
}
