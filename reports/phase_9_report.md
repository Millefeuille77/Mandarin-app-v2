# Phase 9 Report: Settings & Polish

**Date:** 2026-05-02
**Agent:** Developer (Sonnet)
**Phase:** 9 of 10 — Settings & Polish

---

## Files Created

| File | Lines | Notes |
|---|---|---|
| `app/src/main/java/com/mandarinlearn/data/repository/SettingsRepository.kt` | 59 | Thin wrapper over `UserPreferencesRepository`; exposes flows + suspend setters for DI |
| `app/src/main/java/com/mandarinlearn/util/FileExportHelper.kt` | 51 | SAF intent builders: `createDocumentIntent()`, `openDocumentIntent()`, `isValidUri()` |
| `app/src/main/java/com/mandarinlearn/domain/usecase/ExportProgressUseCase.kt` | 181 | Reads all user state from Room, serializes to `ExportSnapshot` JSON, writes via SAF URI; defines all export DTOs |
| `app/src/main/java/com/mandarinlearn/domain/usecase/ImportProgressUseCase.kt` | 139 | Reads SAF URI, parses JSON, validates `version == 1`, applies atomically via `runInTransaction`; defines `ImportValidationException` |
| `app/src/main/java/com/mandarinlearn/domain/usecase/ResetProgressUseCase.kt` | 59 | Single `execute()` running one `runInTransaction`: resets vocab SRS, exam results, streak, user progress; does not touch content tables |
| `app/src/main/java/com/mandarinlearn/ui/settings/SettingsComponents.kt` | 225 | Display section (theme segmented row, font slider + live preview, reduce-motion switch) and audio section (speed chips, pinyin toggle); shared `SettingsSwitchRow`; `LazyListScope` extensions |
| `app/src/main/java/com/mandarinlearn/ui/settings/SettingsDataComponents.kt` | 198 | Learning section (daily limit slider), data section (export/import/reset buttons, `DangerResetButton`), about section (version + Gemini key); `ReadOnlySettingRow` |
| `app/src/test/java/com/mandarinlearn/viewmodel/SettingsViewModelTest.kt` | 271 | MockK + Turbine + `StandardTestDispatcher`; covers state transitions, 6 preference setters, export/import/reset success and failure snackbar keys, `isExporting` flag lifecycle |
| `app/src/test/java/com/mandarinlearn/data/repository/ExportImportRoundTripTest.kt` | 165 | Pure serialization round-trip tests (no Room); validates version field, SM-2 fields, exam fields, streak fields, progress fields, and `ignoreUnknownKeys` forward-compatibility |

---

## Dependencies Added

None. All Phase 9 functionality uses:
- `kotlinx.serialization` (already on classpath from earlier phases)
- `androidx.datastore.preferences` (already on classpath from `UserPreferencesRepository`)
- `androidx.activity.result.contract.ActivityResultContracts` (standard AndroidX, already present)
- MockK + Turbine (already in `testImplementation` from earlier test phases)

---

## Acceptance Criteria Status

| Criterion | Status | Notes |
|---|---|---|
| Theme segmented control (System / Light / Dark) | DONE | `ThemeSegmentedRow` in `SettingsComponents.kt`; persisted via `SettingsRepository.setTheme()` |
| Font size slider (4 discrete steps: 0.9×, 1.0×, 1.15×, 1.3×) | DONE | `FontSliderRow` with live 18 sp × scale preview text |
| Reduce-motion switch | DONE | `SettingsSwitchRow` wired to `SettingsRepository.setReduceMotion()` |
| Audio speed chips (0.5×, 0.75×, 1.0×, 1.25×) | DONE | `AudioSpeedRow` with selected chip shown as primary button |
| Show-pinyin default toggle | DONE | `SettingsSwitchRow` in audio section; helper text explains scope |
| Daily new-cards slider (5–50, integer steps) | DONE | `DailyLimitSliderRow` with live count readout |
| Export via Storage Access Framework | DONE | `ExportProgressUseCase` + `FileExportHelper.createDocumentIntent()` |
| Import via Storage Access Framework | DONE | `ImportProgressUseCase` with `ConfirmDialog` before applying |
| Reset all progress with confirmation | DONE | `ResetProgressUseCase`; `DangerResetButton` with error-tinted text + confirm dialog |
| App version display | DONE | `ReadOnlySettingRow` in about section; value passed from `BuildConfig.VERSION_NAME` |
| Gemini key indicator | DONE | `ReadOnlySettingRow` showing "Set" / "Not set" string resources |
| FlashcardViewModel reads `dailyNewCardsLimit` from preferences | DONE | `loadSession()` calls `preferencesRepository.dailyNewCardsLimit.first()` |
| FlashcardViewModel reads `audioSpeed` from preferences | DONE | `playAudio()` calls `preferencesRepository.audioSpeed.first()` |
| PassageViewModel reads `showPinyin` default from preferences | DONE | `loadPassage()` reads `showPinyin.first()` before determining effective default |
| PassageViewModel reads `audioSpeed` from preferences | DONE | `playAll()` reads `audioSpeed.first()` and passes to `repo.play()` |
| `SettingsViewModelTest` | DONE | 14 test cases |
| `ExportImportRoundTripTest` | DONE | 7 test cases |

---

## QA Fixes from Previous Phase (Phase 8)

| QA ID | Severity | Fix Applied |
|---|---|---|
| M-1 | Major | `ExamScoresChart` single-point rendering: extracted `SinglePointChart` composable with a transparent 56 dp `Box` overlay carrying `pointerInput(detectTapGestures)` on top of a background Canvas. Hit zone now satisfies the ≥ 56 dp touch target rule. |
| M-2 | Major | Removed legacy `else { ProgressScreen(onNavigateBack = ...) }` fallback from `Routes.PROGRESS` in `AppNavigation.kt`. When `appContainer == null`, the destination now renders nothing instead of a misleading placeholder stub. |
| m-1 | Minor | `buildChartAccessibilityDescription()` signature refactored to accept 6 pre-formatted string parameters. Callers pass `stringResource()` values from composable context, eliminating hardcoded English strings. New string resources added: `chart_acc_empty`, `chart_acc_single`, `chart_acc_gain`, `chart_acc_loss`, `chart_acc_no_change`, `chart_acc_multi`. |

---

## Deviations from Spec

| Item | Spec | Actual | Reason |
|---|---|---|---|
| `SettingsComponents.kt` file limit | ≤ 300 lines (single file implied) | Split into `SettingsComponents.kt` (225 lines) + `SettingsDataComponents.kt` (198 lines) | Combined file reached 424 lines; the ≤ 300 line hard rule required a split. Functionally equivalent — all `LazyListScope` extensions remain importable from the same package. |
| `DangerResetButton` color | Uses `MandarinSecondaryButton` per spec | Uses raw `Button` with `ButtonDefaults.buttonColors(containerColor = surfaceVariant, contentColor = error)` | `MandarinSecondaryButton` does not expose a `contentColor` parameter; creating a custom composable was the only way to achieve error-tinted text while maintaining surfaceVariant background. |

---

## Known Issues / TODOs for Later Phases

| ID | Description | Phase |
|---|---|---|
| TODO-9-1 | `SettingsRepository` duplicates `UserPreferencesRepository` interface surface. If the app grows, these should be merged or one should delegate via interface. | Phase 10 / Post-ship refactor |
| TODO-9-2 | Import use case overwrites streak fields unconditionally. A merge-strategy (take max of local vs imported streak) might be preferable for users who import an older backup. Not addressed because spec does not define merge behavior. | Post-ship |
| TODO-9-3 | Theme preference change requires app restart on some devices to fully propagate to system bars. The `AppTheme` composable observes the flow, but WindowInsets colors may lag on older API 26–28 devices. | Post-ship |
| TODO-9-4 | Font scale preference affects only Compose text; it does not call `Configuration.fontScale` for WebView or system dialogs (none present currently, but relevant if Phase 10 adds web content). | Phase 10 awareness |

---

## String Resources Added

### Chart accessibility (QA m-1 fix, `ExamScoresChart.kt`)

| Key | Value |
|---|---|
| `chart_acc_empty` | `"No exam scores recorded yet"` |
| `chart_acc_single` | `"Single exam result: %1$s"` |
| `chart_acc_gain` | `"improved by %1$s"` |
| `chart_acc_loss` | `"declined by %1$s"` |
| `chart_acc_no_change` | `"unchanged"` |
| `chart_acc_multi` | `"%1$d exam results. Scores ranged from %2$s to %3$s. Latest score %4$s, %5$s from previous."` |

### Settings screen (Phase 9)

| Key | Value |
|---|---|
| `settings_section_display` | `"Display"` |
| `settings_section_audio` | `"Audio"` |
| `settings_section_learning` | `"Learning"` |
| `settings_section_data` | `"Data"` |
| `settings_section_about` | `"About"` |
| `settings_theme_label` | `"Theme"` |
| `settings_theme_system` | `"System"` |
| `settings_theme_light` | `"Light"` |
| `settings_theme_dark` | `"Dark"` |
| `settings_font_size_label` | `"Font size"` |
| `settings_font_small` | `"Small"` |
| `settings_font_medium` | `"Medium"` |
| `settings_font_large` | `"Large"` |
| `settings_font_xl` | `"X-Large"` |
| `settings_font_preview` | `"学习普通话"` |
| `settings_reduce_motion` | `"Reduce motion"` |
| `settings_reduce_motion_desc` | `"Reduce motion toggle, %1$s"` |
| `settings_audio_speed_label` | `"Playback speed"` |
| `settings_show_pinyin_label` | `"Show pinyin by default"` |
| `settings_show_pinyin_helper` | `"HSK 1–3 lessons; pinyin always on for HSK 4–5"` |
| `settings_show_pinyin_desc` | `"Show pinyin by default toggle, %1$s"` |
| `settings_daily_limit_label` | `"New cards per day"` |
| `settings_daily_limit_value` | `"%1$d cards"` |
| `settings_export_button` | `"Export progress"` |
| `settings_import_button` | `"Import progress"` |
| `settings_reset_button` | `"Reset all progress"` |
| `settings_export_success` | `"Progress exported successfully"` |
| `settings_export_failed` | `"Export failed — check storage permissions"` |
| `settings_import_success` | `"Progress imported successfully"` |
| `settings_import_failed` | `"Import failed — file may be corrupt or wrong format"` |
| `settings_reset_complete` | `"All progress reset"` |
| `settings_reset_failed` | `"Reset failed — please try again"` |
| `settings_import_confirm_title` | `"Import progress?"` |
| `settings_import_confirm_body` | `"This will replace your current progress with the selected file. This cannot be undone."` |
| `settings_reset_confirm_title` | `"Reset all progress?"` |
| `settings_reset_confirm_body` | `"This will permanently delete all your vocabulary progress, exam results, and streak data. Content is not deleted."` |
| `settings_confirm_yes` | `"Yes, continue"` |
| `settings_confirm_cancel` | `"Cancel"` |
| `settings_app_version_label` | `"App version"` |
| `settings_app_version_value` | `"%1$s"` |
| `settings_gemini_key_label` | `"Gemini API key"` |
| `settings_gemini_key_set` | `"Set"` |
| `settings_gemini_key_not_set` | `"Not set"` |
| `settings_gemini_key_helper` | `"Set via local.properties (GEMINI_API_KEY)"` |
