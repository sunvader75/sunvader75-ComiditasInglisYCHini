# Weekly meal generation

## Goal
User explicitly selected adding Generate week as step 2. Generate Monday–Sunday containing the selected date, preserving daily full liked coverage and <=2 meals for 1–3 members.

## Scope
Reuse daily validator and optimizer. Add pure weekly generator, atomic weekly persistence, calendar button and replacement confirmation. No variety policy, schema/config/dependency changes, membership limit UI changes or unrelated fixes. Repetition is allowed. Preserve existing uncommitted daily-validity work and its pending Room/UI checks.

## Acceptance
- Show exact selected-date week range including cross-month/year boundaries.
- Generate and validate seven days before writing; impossible generation leaves all old rows intact.
- Replace exactly the inclusive target week in a single Room transaction, preserving neighboring dates.
- Confirm replacement when any assignment exists across the full week, not just visible month.
- Transactionally reject an unconfirmed overwrite if assignments appeared after initial emptiness check.
- Freeze confirmation range; cancellation writes nothing; no duplicate submissions/dismissal while saving; errors retain retry context.
- Do not overlap day editing and weekly confirmation or retain stale daily drafts after weekly replacement.

## Tasks
- [ ] W1 (implemented; device checks pending) Implement weekly generation, transactional persistence and confirmation UI with focused regression/integration tests.
- [ ] W2 (unit/build/native review verified; device checks pending) Verify unit suite and APK independently; review new candidate and record device-test limitations.
- [ ] W3 (blocked) Compile and execute Room/Compose integration checks after dependency/device availability is resolved.

## Routing
W1 delegated writer: multiple non-trivial files and preparation reads.
W2 delegated verifier as dictated by native assessment, plus native review when enabled.

## Verification
Test-first deterministic weekly generator: seven complete valid days, Monday/Sunday, month/year/leap boundaries, fixed seeds, impossible preferences and unsupported counts. Observe RED before implementation and GREEN after; compilation failure alone is not behavioral RED.
Commands use ANDROID_HOME=/Users/sunva/Library/Android/sdk, sh ./gradlew, --offline -Pandroid.builder.sdkDownload=false --no-daemon --console=plain. Run unit tests and assembleDebug separately. Instrumentation compilation is blocked by uncached dependencies and execution by absent device; no downloads/emulators authorized. Author relevant integration cases without inventing runtime evidence.

## Delivery and progress
No commits/staging/push/PR authorized; leave working changes. Strategy ask-on-risk; expected weekly delta ~300–500 diff lines including tests; advisory only. Existing daily candidate was 483 lines and already reviewed/acknowledged. Current Git HEAD 4bba1b5 contains the daily changes, committed outside this parent workflow; weekly review is a 369-line delta from that baseline. Do not delete tests or minify to reduce size. Writer reported weekly delta 369 authored diff lines; cumulative historical estimate 852 (not independently reconstructed). Meaningful RED: five tests ran, four failed against minimal skeleton. GREEN: five weekly tests pass; full suite 22 tests preserves baseline17. assembleDebug and git diff --check passed. Added five Room cases and cross-month Compose confirmation/cancel coverage, not compiled/executed due known offline/device limits. ASSESS unassessable due untracked selection; independent verifier confirmed XML22 tests zero failures/errors/skips and APK17,408,169 bytes. Its separate unit/build commands succeeded UP-TO-DATE (cached validation, not fresh test execution); writer had freshly observed test results. Static review found no candidate-caused issue; git diff --check passed.
Native review medium/reliability approved lineage review-7efc5c8a375d45f1, original369 lines; exact acknowledge-approved returned authority burned. No STATUS after acknowledgement. No source edits after review.
Instrumentation not compiled/executed. Actual Room rollback and interactive loading/errors/retry remain unverified; do not close W1/W2 as fully done.

## Next step
Report implementation and verified unit/build/review evidence; retain W3 blocked on offline dependencies and absent device. No automatic continuation to step3, commits or publishing.
