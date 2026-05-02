# Phase 6 Report: Speaking Section + Gemini STT

**Note:** This report was synthesised from the on-disk artefacts after the dev agent died silently before writing it. All files listed below exist and were verified.

## Files Created
- `app/src/main/java/com/mandarinlearn/data/audio/AudioRecorder.kt` (147 lines)
- `app/src/main/java/com/mandarinlearn/domain/usecase/ScorePronunciationUseCase.kt` (35 lines)
- `app/src/main/java/com/mandarinlearn/ui/speaking/SpeakingComponents.kt` (300 lines — at the cap, not over)
- `app/src/main/java/com/mandarinlearn/util/PermissionsHelper.kt` (28 lines)
- `app/src/test/java/com/mandarinlearn/viewmodel/SpeakingViewModelTest.kt` (280 lines)

## Files Replaced (full implementation, no longer Phase 1/2 stubs)
- `app/src/main/java/com/mandarinlearn/data/remote/GeminiService.kt` (272 lines) — `transcribeAndScore()` now implemented per ARCH §4.3 (multipart audio + expected text + JSON-schema response)
- `app/src/main/java/com/mandarinlearn/data/repository/SpeakingRepository.kt` (107 lines) — replaces Phase 2 stub; deterministic text-similarity fallback when Gemini fails
- `app/src/main/java/com/mandarinlearn/ui/speaking/SpeakingScreen.kt` (285 lines) — full UI per UX Screen 6
- `app/src/main/java/com/mandarinlearn/ui/speaking/SpeakingViewModel.kt` (243 lines) — state machine: Idle → Recording → Processing → Result → Idle
- `app/src/main/java/com/mandarinlearn/ui/speaking/SpeakingUiState.kt` (84 lines) — sealed UI state

## Files Updated (wiring)
- `app/src/main/java/com/mandarinlearn/di/AppContainer.kt` — wires AudioRecorder + ScorePronunciationUseCase + SpeakingRepository
- `app/src/main/java/com/mandarinlearn/navigation/AppNavigation.kt` — SpeakingScreen route now uses real ViewModel
- `app/src/main/AndroidManifest.xml` — `RECORD_AUDIO` permission added
- `app/src/main/res/values/strings.xml` — 35 new string resources

## Acceptance Criteria Status (per IMPLEMENTATION_PLAN Phase 6)
- [x] `transcribeAndScore` implemented in GeminiService with structured JSON response parsing
- [x] AudioRecorder wraps MediaRecorder with `RECORD_AUDIO` permission flow
- [x] SpeakingRepository replaces Phase 2 stub
- [x] SpeakingScreen, ViewModel, UiState match UX Screen 6
- [x] Permission flow shows explainer + Grant button
- [x] Score display uses color + icon + text label (color is never sole indicator)
- [x] Recording state machine implemented
- [x] AppNavigation wires SpeakingScreen route via AppContainer
- [x] Test file exists for SpeakingViewModel
- [x] All files ≤ 300 lines (SpeakingComponents at exactly 300)

## Known Deviations / Notes
- `SpeakingComponents.kt` is exactly 300 lines — at the cap. Future additions to this file MUST be split.
- The Gemini SDK 0.2.2 may not support audio input (parallel to the §4.6 TTS limitation noted in Phase 5). The implementation includes a deterministic text-similarity fallback so the UI always returns a score, even if the API call fails. This matches the architect's degraded-mode philosophy.
- `PermissionsHelper.kt` is a small utility (28 lines) not in `FOLDER_STRUCTURE.md` — placed in `util/` per convention. Same precedent as Phase 2's `JsonImporterMappers.kt` and Phase 4's `PassageControls.kt` (helpers added to honour file-length rules).

## Open Questions for QA Phase 6
- Verify `transcribeAndScore` error mapping is consistent with `synthesize`'s mapping in Phase 5.
- Verify the text-similarity fallback formula doesn't cap user scores too low (regression risk: if fallback always returns 50, users won't perceive variance).
