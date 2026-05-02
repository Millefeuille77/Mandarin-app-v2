// FileExportHelper.kt — Mandarin Learn
// Storage Access Framework helpers for data export and import.
// Phase 9: Settings & Polish. FOLDER_STRUCTURE.md §util/.
// Uses ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT; no WRITE_EXTERNAL_STORAGE needed.

package com.mandarinlearn.util

import android.content.Intent
import android.net.Uri

/**
 * Intent builders for the Storage Access Framework document picker.
 * Composable screens call these helpers to launch the system file picker via
 * [androidx.activity.compose.rememberLauncherForActivityResult].
 */
object FileExportHelper {

    private const val JSON_MIME = "application/json"

    /**
     * Builds the intent to create a new JSON document via SAF.
     * The suggested filename includes the ISO date for easy identification.
     *
     * @param dateString ISO date string, e.g. "2026-05-02".
     * @return Intent to pass to [Intent.ACTION_CREATE_DOCUMENT] launcher.
     */
    fun createDocumentIntent(dateString: String): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = JSON_MIME
            putExtra(Intent.EXTRA_TITLE, "mandarin_progress_$dateString.json")
        }

    /**
     * Builds the intent to open an existing JSON document via SAF.
     *
     * @return Intent to pass to [Intent.ACTION_OPEN_DOCUMENT] launcher.
     */
    fun openDocumentIntent(): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = JSON_MIME
        }

    /**
     * Validates that a [Uri] returned from the SAF picker is non-null and has a
     * `content://` or `file://` scheme suitable for reading/writing.
     */
    fun isValidUri(uri: Uri?): Boolean =
        uri != null && (uri.scheme == "content" || uri.scheme == "file")
}
