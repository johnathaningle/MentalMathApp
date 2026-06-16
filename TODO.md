# Bugs Found During Audit (2026-06-16)

- **CustomDifficultyDialog dismisses on validation failure** — The `AlertDialog` auto-dismisses after the positive button listener returns, even when validation fails. All user-entered values are lost; only a fleeting Toast is shown. Fix: set button listener to `null` in the builder and use `setOnShowListener` with a custom click handler that conditionally calls `dismiss()`. (`CustomDifficultyDialog.kt:45-108`)

- **No upper-bound validation in CustomDifficultyDialog** — Only lower bounds (`<1`, `<2`) and min/max consistency are checked. A user can set, e.g., `smallMax = 50000`, producing `50000 × 50000 = 2.5×10⁹` which overflows `Int` silently and yields wrong answers. Fix: add reasonable upper limits (e.g., `≤1000`) for all number range fields. (`CustomDifficultyDialog.kt:67-75`)

- **Game-over postDelayed can crash** — `showFeedback()` schedules `navigateToEnd()` without checking `isAdded`. If the user presses Back during the 1-second delay, `findNavController()` throws `IllegalStateException`. Fix: add `if (isAdded)` guard, matching the pattern already used at line 110. (`GameFragment.kt:105`)

- **Exam progress display is off by one** — Shows `questions.size` (already-answered count) as the numerator. The last question reads `"14 / 15"` instead of `"15 / 15"`. Fix: display `questions.size + 1` (capped at `config.questionCount`). (`GameFragment.kt:143`)
