# Daily meal validity

## Goal and authorization
Implement only the first recommendation: guarantee daily preference coverage with at most two distinct meals, and atomic replacement. Weekly generation, member-management limits, variety policies, and revalidation after preference/catalog changes are out of scope.

## Acceptance criteria
- A saved complete day covers exactly all current members (1–3), using existing meals each recipient likes, with at most two distinct meals.
- Random generation never falls back to three individual meals; failure preserves the previous day.
- Manual editing saves a complete validated day draft; rejected changes preserve persisted data.
- Single-member assignment/removal cannot persist an invalid incomplete plan.
- Clearing a day is explicitly labelled as clearing the whole plan, not removing one person's meal.
- Day replacement uses one Room transaction; concurrent proposal serialization is not promised.

## Tasks
- [ ] T1 (implemented; integration checks pending) Implement shared validation, constrained generation, atomic persistence, and safe day editing with regression tests.
- [ ] T2 (unit/build/review verified; device checks blocked) Verify focused tests, integration/build checks where available, review the candidate, and record all limitations.
- [ ] T3 (blocked) Resolve instrumentation dependency availability and connect an Android device; execute Room/UI verification before closing T1/T2.

## Routing
T1: delegated to gentle-ai-worker; multiple non-trivial source files and reading preparing writes.
T2: delegated to gentle-ai-verify as required by candidate assessment/environment limitations; parent coordinates native review.
T3: verifier after device/dependency availability is resolved; no downloads or emulator creation authorized.

## Verification plan
Use test-first for runnable deterministic domain regressions (three exclusive meals must fail; complete liked coverage succeeds; incomplete/unliked/unknown/duplicate assignments fail; seeded generation). Verify DAO rollback and use-case rejection/preservation with instrumentation if a device and runner are available. Observe RED before production changes and GREEN after. If environment blocks the runner, record that fact rather than fabricate lifecycle evidence; perform structural checks and report tests as pending.

## Progress and evidence
Exploration completed. Baseline `sh ./gradlew :app:testDebugUnitTest --no-daemon --console=plain` exited 1 before tests: SDK location not found (Windows sdk.dir). Gradle 9.6.0 reached configuration on Java 25. No connected adb devices.
Parent checked generated build report is Git-ignored; odd/ is the authorized task document, .codegraph/ predates implementation. No tracked source changes.
Process-local ANDROID_HOME baseline succeeded in 48s; testDebugUnitTest was NO-SOURCE (no tests executed). Gradle unexpectedly installed SDK Build-Tools 34.0.0. Parent diagnosed installation separately: package exists, no tracked source/config diff. Further commands must use --offline and -Pandroid.builder.sdkDownload=false. No cleanup or environment/config changes authorized. assembleDebug baseline was not run.
T1 writer returned partial: shared validator, bounded common/split generation, atomic Room replacement, validated mutations, saving/error state, and full-day Spanish editor implemented. Authored diff including tests: 483 lines. No commits or config changes.
Observed RED: exclusive three-meal regression failed (1 test). Observed GREEN: 12 validator + 5 optimizer tests passed; 100 deterministic seeds checked.
Combined unit/assembleDebug/assembleDebugAndroidTest failed on uncached Android instrumentation dependencies (JUnit Android 1.1.5, Espresso 3.5.1, Compose UI test 1.7.0). Instrumentation test has 3 integration cases including rollback, not yet compiled/executed; UI runtime checks pending. Independent verifier now runs unit suite and APK assembly separately offline.
ASSESS returned unassessable because intended-untracked selection is required; unknown review outcome mandates independent verification. Native inspect returned pre-lineage untracked selection. New validator/tests were included; .codegraph/ and tracking document excluded from review candidate.
Independent verifier confirmed testDebugUnitTest SUCCESS (17 tests, zero failures/errors/skips), assembleDebug SUCCESS and git diff --check clean except pre-existing line-ending warnings. APK: app/build/outputs/apk/debug/app-debug.apk (17,331,462 bytes).
Native medium-risk reliability review approved candidate, lineage review-bb0b72340d12a6f5. Exact acknowledge-approved returned native-approved-acknowledgement-completed with authority burned. No subsequent STATUS issued.
Non-blocking advisory R3-stale-dialog-error: a validation error may survive dialog dismissal and appear for another date; separate follow-up, not changed after review. Not a persistence violation.
T1/T2 remain unchecked because Room instrumentation and UI interaction checks are not executed. Three instrumentation tests authored but not compiled because offline dependencies missing; no device connected. No further downloads authorized.

## Delivery
Strategy: ask-on-risk. Forecast: approximately 350–650 authored diff lines including tests and minimal editor integration; size is advisory, not a reason to omit checks. No push, PR, merge, or commits authorized. Work-unit commit requirement is blocked by the explicit harness rule requiring a user commit request; leave changes uncommitted and record this limitation.

## Next step
Report implemented behavior, successful 17-test suite/APK/native review, and pending Room/UI verification honestly. Resolve T3 before full feature closure. No source changes after reviewed candidate; no commit, push, or PR.
