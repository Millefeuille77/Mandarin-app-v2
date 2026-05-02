// PermissionsHelper.kt — Mandarin Learn
// Utility for runtime permission checks.
// Per IMPLEMENTATION_PLAN.md Phase 6 §B and FOLDER_STRUCTURE.md.

package com.mandarinlearn.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Utility object for checking runtime permissions.
 * Does NOT request permissions — requesting is the responsibility of the UI layer,
 * which uses ActivityResultContracts.RequestPermission (in SpeakingScreen).
 */
object PermissionsHelper {

    /**
     * Returns true iff [Manifest.permission.RECORD_AUDIO] has been granted.
     * Called by [SpeakingViewModel] before starting a recording.
     */
    fun hasRecordAudioPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
}
