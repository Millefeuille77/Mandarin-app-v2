# QA Report: Research Agent — Data Validation
**Date:** 2026-05-01
**Agent Under Review:** Research Agent (Claude claude-sonnet-4-6)
**Verdict:** PASS WITH WARNINGS

---

## Summary

All 18 JSON files are structurally valid, UTF-8 encoded, and use diacritic tone marks throughout — no blockers were found on format or structural grounds. However, HSK 5 vocabulary coverage is critically low at 23% of target (300/1300 entries) and is entirely model-generated, HSK 4 vocabulary is at 52% (310/600), four duplicate character entries exist in HSK 3, and the HSK 3 reading and writing sample question sections fall significantly below the 10-question minimum. All reading passages, audio content, and sample questions are model-generated with no web sourcing. The CONTENT_REPORT.md accurately self-discloses all of these issues.

---

## Statistics
- Files reviewed: 18 JSON + 1 Markdown (CONTENT_REPORT.md) = 19 total
- Total checks: 27 (12 automated check categories + 5 manual spot-checks per level)
- Passed: 22
- Blockers: 0
- Major issues: 5
- Minor issues: 2

---

## Blockers (must fix before proceeding)

*None.*

---

## Major Issues (should fix)

- [ ] **HSK 5 vocabulary critically underpopulated** — `data/vocabulary/hsk5_vocab.json` — 300 entries vs ~1300 target (23% coverage). Entire file is model-generated. For a 60-year-old beginner app, incorrect HSK 5 words or missing high-frequency words could be misleading if users eventually reach this level. Fix: Obtain the official Hanban/CTI HSK 5 word list and populate to ~1300 entries; commission native speaker review of model-generated entries.

- [ ] **HSK 4 vocabulary significantly underpopulated** — `data/vocabulary/hsk4_vocab.json` — 310 entries vs ~600 target (52% coverage). Fix: Complete remaining ~290 entries from mandarinbean.com HSK 4 full list (the source used for the first 310 entries).

- [ ] **4 duplicate character entries in HSK 3 vocab** — `data/vocabulary/hsk3_vocab.json` — Duplicates: 帮忙 (entries 11 & 297), 过 (entries 89 & 299), 花 (entries 95 & 298), 只 (entries 275 & 295, with differing pinyin zhǐ/zhī indicating different senses). Real unique count is 296, not 300. Fix: Remove duplicate entries; for 只, retain both senses but deduplicate by appending a sense number or using compound IDs.

- [ ] **Sample questions below target in 4 sections** — `data/exams/sample_questions.json` — HSK 3 reading: 5/10, HSK 3 writing: 5/10, HSK 4 writing: 7/10, HSK 5 writing: 8/10. Total shortfall: 15 questions. Fix: Add the missing questions to reach the 10-question minimum per section.

- [ ] **All reading passages, audio content, and sample questions are model-generated** — `data/reading/hsk{1-5}_readings.json`, `data/audio/tone_drills.json`, `data/audio/conversation_phrases.json`, `data/exams/sample_questions.json` — No external sourcing. For beginner learners (especially 60+), unverified AI-generated Chinese content carries risk of unnatural phrasing or subtle errors. Fix: Commission review by a native Mandarin speaker or certified HSK instructor before production deployment.

---

## Minor Issues (fix when convenient)

- [ ] **HSK 3 unique character count is 296, not 300** — `data/vocabulary/hsk3_vocab.json` — The CONTENT_REPORT.md reports 300 entries met, but after deduplication the true unique count is 296. Fix: After removing duplicates, add 4 new unique HSK 3 entries to restore the count to 300.

- [ ] **HSK 3.0 (2021 revision) compliance not verified** — `data/exams/hsk{1-5}_exam_structure.json` — The exam structures are based on HSK 2.0. The CONTENT_REPORT.md acknowledges this. Fix when convenient: Verify against the 2021 HSK 3.0 standard if the target users or market require it.

---

## Checks Passed

- [x] **JSON Validity** — All 18 JSON files parse without errors.
- [x] **UTF-8 Encoding** — All 18 files are valid UTF-8 with no BOM issues.
- [x] **Pinyin Tone Marks (Vocabulary)** — No tone numbers (e.g., ni3) found in any of the 5 vocab files; 1204/1213 entries carry diacritic marks (the remainder are neutral-tone syllables, which is correct).
- [x] **Pinyin Tone Marks (Reading)** — No tone numbers found in pinyin_annotations across all 5 reading files.
- [x] **Pinyin Tone Marks (Audio)** — No tone numbers found in tone_drills.json or conversation_phrases.json; all 100 conversation phrases carry tone marks.
- [x] **Empty Field Detection** — No null or empty string fields in any vocabulary file.
- [x] **HSK 1 Vocabulary Count** — 153 entries vs ~150 target (102%).
- [x] **HSK 2 Vocabulary Count** — 150 entries vs ~150 target (100%).
- [x] **HSK 3 Vocabulary Count** — 300 entries vs ~300 target (100%), though 4 are duplicates (see Minor issue).
- [x] **Reading Passage Counts** — All 5 levels have 8 passages, above the 5-passage minimum.
- [x] **Pinyin Coverage (HSK 1-3 Readings)** — All 24 passages (8 per level) have character-level pinyin_annotations in `{"character": "X", "pinyin": "x"}` dict format with annotation counts meeting or exceeding character counts.
- [x] **Exam Structure Required Fields** — All 5 exam structure files contain: hsk_level, total_duration_minutes, sections, total_max_score, total_passing_score.
- [x] **Exam Scoring Standard** — HSK 1-2: 200 max / 120 pass; HSK 3-5: 300 max / 180 pass. Matches official HSK 2.0 standard.
- [x] **Section Score Arithmetic** — Section max_scores sum to total_max_score in all 5 exam files (e.g., HSK3: 100+100+100=300).
- [x] **HSK5 Writing Format** — 2 questions (summary + essay) is correct for official HSK 5 writing section format.
- [x] **Duplicate Detection (HSK 1, 2, 4, 5)** — No duplicate characters found in HSK 1, 2, 4, or 5 vocab files.
- [x] **Field Schema Consistency** — All vocab entries across all 5 levels use identical schemas: [character, example_sentence, hsk_level, id, part_of_speech, pinyin, translation].
- [x] **hsk_level Tag Consistency** — All vocab entries and reading passages carry the correct hsk_level integer tag matching their file.
- [x] **Tone Drills Completeness** — 20 tone-pair drills covering all combinations including neutral tones.
- [x] **Conversation Phrases Count** — 20 phrases per level × 5 levels = 100 total, all with tone marks.
- [x] **Sample Questions — correct_answer fields** — All 115 questions have a correct_answer field; all match one of the provided option keys.
- [x] **CONTENT_REPORT.md exists and discloses sources** — File present at data/CONTENT_REPORT.md (248 lines). All model-generated content is explicitly flagged. Source URLs provided for web-sourced content (mandarinbean.com, improvemandarin.com, purpleculture.net).

---

## Manual Spot-Check Results (5 entries × 5 levels)

**HSK 1** (本, 那, 爸爸, 年, 明天): Translations accurate. Example sentences simple, grammatical, and natural. Appropriate for absolute beginners. No issues.

**HSK 2** (机场, 从, 累, 因为, 送): Translations accurate. Sentences use appropriate HSK 2 grammar patterns (因为...所以 is a well-chosen example). No issues.

**HSK 3** (鼻子, 被, 地方, 简单, 结婚): Translations accurate. 被 passive particle is correctly explained. Sentences are natural and appropriately complex for HSK 3. No issues.

**HSK 4** (永远, 力气, 倍, 至少, 购物): Translations accurate. Example sentences are notably brief (fragment-length) compared to HSK 1-3 — e.g., "Together forever." rather than full subject-verb-object sentences. Flagged as mild concern for a learning app targeting beginners but not a blocker.

**HSK 5** (向往, 预防, 欢乐, 尊严, 麻醉): Translations are accurate against standard HSK 5 vocabulary lists. 麻醉 (anesthesia) is a legitimate HSK 5 word. Example sentences are also notably brief/fragmentary. The model-generated nature is evident in the somewhat formulaic structure but content is linguistically correct.

---

## Recommendation

**REWORK** (limited scope — do not re-run the full Research agent)

The pipeline may conditionally proceed to the Architect stage while the following targeted fixes are applied in parallel or as a pre-launch gate:

1. **[Before production]** Source official HSK 5 word list from Hanban/CTI and expand `hsk5_vocab.json` from 300 to ~1300 entries. This is the highest-priority fix.
2. **[Before production]** Complete HSK 4 vocabulary to ~600 entries using the existing mandarinbean.com source.
3. **[Before launch]** Add 15 sample questions to close the gaps in HSK 3 reading (5), HSK 3 writing (5), HSK 4 writing (3), and HSK 5 writing (2).
4. **[Before launch]** Deduplicate the 4 repeated characters in `hsk3_vocab.json` (帮忙, 过, 花, 只) and add 4 replacement unique entries.
5. **[Before production]** Commission native Mandarin speaker or HSK instructor review of all model-generated content (passages, phrases, tone drills, sample questions).
6. **[Advisory]** Expand HSK 4 example sentences to full subject-verb-object structure for pedagogical consistency with HSK 1-3.
