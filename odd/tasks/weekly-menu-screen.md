# Weekly menu screen

## Goal and authorization
User explicitly requested replacing the monthly calendar with visible Monday–Sunday menus. Reading requires no taps; tapping is for editing. Show dishes and recipients, seven scrollable cards, previous/next week. Keep existing route and editor, generation, variety and atomic replacement. Spanish project UI conventions.

## Scope and constraints
CalendarScreen, CalendarViewModel, HomeScreen, new pure WeekMenuPresentation helper/tests, weekly UI regression tests. No database/domain/schema/config/dependency/splash changes. No installs, emulator/device operations, resets, staging, commits or publishing. Existing tracked worktree is clean on master at planning time; .codegraph/ untracked. Prior source work appears externally committed; do not overwrite it.

## Acceptance
- Current week starts Monday; seven ordered dates always visible through scrolling, cross-month/year supported.
- Every assigned dish and recipient is displayed without opening an editor; empty days explicitly marked.
- Week browsing independent of editor selection; +/-7-day navigation.
- Distinguish loading, loaded-empty and error/retry; editor never invisibly blocks all taps awaiting a query. Cancellation and close/reopen work.
- Preserve manual save, random day, clear, replacement confirmation, weekly confirmation frozen to target week and atomicity.
- No claim this presentation change fixes the reported black startup; runtime cause remains unknown.

## Tasks
- [ ] W1 (implemented; UI runtime verification pending) Implement weekly presentation/state and regressions with a bounded single writer.
- [ ] W2 (JVM/APK/independent/native review verified; Android runtime pending) Verify focused/full unit tests and APK, independent verification/native review as available; document runtime blockers.
- [ ] W3 Validate on device: visible menus, navigate cross-month, edit/close/reopen/save, weekly confirmation and cold launch. Pending user/device evidence.

## Checks and evidence
Meaningful pure JVM behavior uses test-first RED/GREEN: Monday/Sunday/year/leap boundaries, seven empty/populated days, grouped dishes with recipients, safe missing-name fallback. Compose tests adapted/added but existing uncached offline Android-test dependencies and no authorized device runner prevent observed UI RED/GREEN. Record this honestly rather than substituting unit evidence for runtime checks.
Use ANDROID_HOME=/Users/sunva/Library/Android/sdk sh ./gradlew with --offline -Pandroid.builder.sdkDownload=false --no-daemon --console=plain; focused test then full :app:testDebugUnitTest and separate :app:assembleDebug, git diff --check. No dependency download or SDK install. Prior42domain tests preserved. Native review earlier failed START inventory preflight without creating authority; fresh candidate requires inspect and exact continuation only.

## Writer evidence and blockers
Writer completed7files443changedlines including3new. Missing-helper compile RED explicitly not behavioral assertion RED, then3helper tests GREEN, full45tests preserves42domain. APK and diffcheck pass. Android test compile unavailable at AAR metadata due uncached junit1.1.5/espresso3.5.1/Compose test1.7.0; no tests compiled/executed. No device/install/config/DB changes. Independent verifier running fresh45suite/APK and source/test review. Native inspect selected3newsource/testfiles excludes tracking/codegraph; START failed preflight inventorychanged, mutationnone/notstarted; fresh inspect confirms no authority/candidates. Stop retries; native review unavailable, not approved. Cold launch remains undiagnosed.

## Independent verification and correction
Fresh full45tests zero failures/errors/skips17s; separate APK6s ZIPvalid17,430,508bytes; diffcheck pass. No confirmed severe production defect. Found duplicate range-text selector in GenerateWeekTest; parent restricted matcher to dialog ancestor (test-only), independent structural follow-up confirmed dialog matcher appropriate and git diff --check passed. Android compilation/runtime still unavailable; unchanged production/unit/APK were not rerun after test-only correction. Android tests not compiled/executed, device and coldlaunch still pending. No new native review retry for unchanged production.

## Startup follow-up explicitly authorized
User reported persistent black startup and supplied log without FATAL/Room/ANR; Android drew frames. User approved minimal splash wiring test: MainActivity.installSplashScreen() before super.onCreate restored, no keep condition or DB changes. Verifier45tests0fail/errors/skips andAPK builds11s/5s, ZIP valid timestamp2026-10-08T20:12:31+01:00. Coldlaunch still unverified: no meaningful JVM RED for lifecycle. Native current candidate (weekly UI+startup) review-7be88509ec93a07b approved and exact acknowledgement consumed authority, no STATUS after burn. Nonblocking R3-observer-recovery at CalendarViewModel.kt:65–68 separate follow-up, no correction offered. Earlier untracked inventory blocker did not recur for this candidate; no recovery/reset/config actions. Git showed some preexisting staged files; assistant did not stage/commit or change their index.

## Routing and next step
W1 delegated multi-file writer, parent owns this document/mirror. Approximately400changed lines/task is advisory only, never minify or omit tests. Preserve old task documents and blockers. W2 follows ASSESS and native availability. No commits per user's standing constraint.
