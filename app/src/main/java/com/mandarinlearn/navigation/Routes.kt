// Routes.kt — Mandarin Learn
// Typed navigation route constants per UX_SPECIFICATION.md §2 (Navigation Structure).
// All routes used in AppNavigation.kt must be declared here — no inline string literals.

package com.mandarinlearn.navigation

/**
 * Centralised route definitions for the app's NavHost.
 * UX_SPECIFICATION.md §2 lists every route.
 */
object Routes {
    // Root-level entry screens
    const val IMPORT = "import"
    const val MAIN   = "main"

    // Tab roots (hosted inside MainScaffold's bottom nav)
    const val HOME     = "home"
    const val PRACTICE = "practice"
    const val EXAM_HUB = "exam"
    const val ME       = "me"

    // Vocabulary
    const val VOCABULARY  = "vocab/{hsk}"
    const val FLASHCARDS  = "flashcards/{hsk}"

    // Reading
    const val READING_LIST = "reading/{hsk}"
    const val PASSAGE      = "passage/{id}"

    // Listening / Speaking (hsk arg)
    const val LISTENING = "listening/{hsk}"
    const val SPEAKING  = "speaking/{hsk}"

    // Exam
    const val EXAM        = "exam/{hsk}"
    const val EXAM_RESULT = "exam/result/{id}"

    // Me tab children
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"

    // --- Argument builder helpers (avoid string concatenation at call sites) ---

    fun vocabulary(hsk: Int) = "vocab/$hsk"
    fun flashcards(hsk: Int) = "flashcards/$hsk"
    fun readingList(hsk: Int) = "reading/$hsk"
    fun passage(id: String)   = "passage/$id"
    fun listening(hsk: Int)   = "listening/$hsk"
    fun speaking(hsk: Int)    = "speaking/$hsk"
    fun exam(hsk: Int)        = "exam/$hsk"
    fun examResult(id: Long)  = "exam/result/$id"
}
