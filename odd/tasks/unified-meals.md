# Unified meals and likes

## Objective
Replace separate Comidas/Gustos sections with one Comidas flow displaying and editing food names and liking family members. Remove description from visible UI; retain legacy database column and contents to avoid migration/data loss.

## Scope and constraints
Preserve food IDs, existing likes and saved plans. Allow zero likes and creation with likes. Preserve Spanish UI conventions. No automatic plan repair or deletion UX. Preserve pre-existing changes in CalendarScreen.kt and gradle/libs.versions.toml. No commits without explicit user request.

## Tasks
- [ ] T1 (in progress): Implement reactive meal-centric state, transactional name/likes saving, unified UI/navigation and focused regression tests.
- [ ] T2 (pending device checks): Unit tests/build/lint passed; native review approved and acknowledged. Instrumentation execution remains pending.
- [ ] T3 (pending authorization): Resolve pre-existing calendar instrumentation import blocker, then compile Android tests and run on a device.

## Acceptance and checks
Food cards show liking members; edit preselects current likes; create/edit saves name and likes atomically; cancel writes nothing; no description input/display or separate Gustos destination. Preserve legacy schema/data and IDs. Run applicable unit tests, assembleDebug, lintDebug and device tests when device available. Use test-first for deterministic behavior; document unavailable checks honestly.

## Evidence
T1 code and regression tests implemented by bounded writer. Structural diff check passed; no stale Preferences routes or description UI references. Transactional saving preserves IDs and stored legacy descriptions. Initial checks failed before execution: wrapper exit 126, then SDK missing exit 1; adb absent PATH. Found existing macOS SDK. User approved sdk.dir-only fix in local.properties; applied and verifier rerunning checks. Tests written first but RED/GREEN not observed. Working tree contains preserved unrelated changes. RDD enabled globally.

## Verification results
- Focused MealsUiStateTest: exit 0, 3 tests passed.
- Full unit tests, assembleDebug and lintDebug: exit 0.
- adb devices: exit 0, no devices; connected tests not executed.
- assembleDebugAndroidTest: exit 1 due to pre-existing unresolved androidx.compose.ui.test.onNode import in ui/screens/calendar/GenerateWeekTest.kt:7. No unified-meals error reported.
- No observed RED; tests were written first but initial execution blocked by environment.

## Next step
Native review review-05cfff9890db71e4 approved and exact acknowledgement completed (authority burned). Nonblocking warning R3-save-failure-coverage at MealsViewModel.kt:69–78 is a separate follow-up. ASSESS unavailable due explicit-untracked declaration requirement; independent verifier already ran the conservative checks. T1 remains partial while instrumentation compilation fails; unrelated test fix requires scope authorization. Device checks pending device. No commits made; pending explicit user authorization.
