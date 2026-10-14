package com.davoe.karootopomaps

import android.content.Context
import java.security.MessageDigest

/**
 * Handles offline license validation for KarooTopoMaps Premium / Pro Custom Theme Editor.
 *
 * Licensing scheme:
 * - A 16-character alphanumeric key formatted as: KTOP-XXXX-YYYY-ZZZZ
 * - Validated using a deterministic cryptographic SHA-256 HMAC checksum algorithm.
 * - Works 100% offline without requiring internet access or Google Play Services on Hammerhead Karoo.
 */
object LicenseManager {

    private const val PREFS_NAME = "karoo_topo_maps_license"
    private const val KEY_IS_PRO = "is_pro_activated"
    private const val KEY_LICENSE_KEY = "activated_license_key"

    // Salt known only to the generator and app
    private const val SALT = "KTOP_HAMMERHEAD_SECRET_SALT_2026_@!"

    /**
     * Checks if the user has activated the Pro / Custom Theme feature.
     */
    fun isProActivated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isPro = prefs.getBoolean(KEY_IS_PRO, false)
        val key = prefs.getString(KEY_LICENSE_KEY, null)
        if (isPro && key != null) {
            return verifyLicenseKey(key)
        }
        return false
    }

    /**
     * Attempts to activate Pro with the provided license key.
     * Returns true if the key is valid and activation succeeds.
     */
    fun activatePro(context: Context, inputKey: String): Boolean {
        val cleanKey = sanitizeKey(inputKey)
        if (verifyLicenseKey(cleanKey)) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_PRO, true)
                .putString(KEY_LICENSE_KEY, cleanKey)
                .apply()
            return true
        }
        return false
    }

    /**
     * Deactivates Pro (for testing or reset).
     */
    fun deactivatePro(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Cleans up user input: removes spaces, dashes, uppercase.
     */
    fun sanitizeKey(key: String): String {
        return key.trim().uppercase().replace("-", "").replace(" ", "")
    }

    /**
     * Formats a raw 16-char string into KTOP-XXXX-YYYY-ZZZZ format for display.
     */
    fun formatKey(raw: String): String {
        val clean = sanitizeKey(raw)
        if (clean.length == 16) {
            return "${clean.substring(0, 4)}-${clean.substring(4, 8)}-${clean.substring(8, 12)}-${clean.substring(12, 16)}"
        }
        return raw
    }

    /**
     * Validates whether a key conforms to KTOP + 4 hex payload + 8 hex checksum.
     * Total clean length = 16 characters (e.g. KTOPA1B2C3D4E5F6).
     */
    fun verifyLicenseKey(cleanKey: String): Boolean {
        if (cleanKey.length != 16) return false
        if (!cleanKey.startsWith("KTOP")) return false

        val payload = cleanKey.substring(4, 8)     // 4 chars (16 bits arbitrary payload)
        val checksum = cleanKey.substring(8, 16)   // 8 chars expected checksum

        val expectedChecksum = computeChecksum(payload)
        return checksum.equals(expectedChecksum, ignoreCase = true)
    }

    /**
     * Computes the 8-character hex checksum for a given 4-character payload.
     */
    fun computeChecksum(payload: String): String {
        val input = "$SALT:$payload"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02X".format(it) }
        return hex.substring(0, 8)
    }

    /**
     * Generates a valid license key for a given seed (used for dispensing keys).
     */
    fun generateKey(seed: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(seed.toByteArray(Charsets.UTF_8)).joinToString("") { "%02X".format(it) }
        val payload = hash.substring(0, 4).uppercase()
        val checksum = computeChecksum(payload)
        val clean = "KTOP$payload$checksum"
        return formatKey(clean)
    }
}
