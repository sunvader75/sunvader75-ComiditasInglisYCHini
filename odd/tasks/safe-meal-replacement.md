# Safe meal replacement

## Goal and authorized decision
Implement step 3: dish-level Change with only valid complete-day alternatives and explicit recipient preview before confirmation. User explicitly selected excluding the changed dish from the entire day; alternatives may reorganize the other dish when clearly presented. Never silently retain the unavailable dish.

## Scope and baseline
HEAD 3625f0d87b6ef006eb3f41deb32fdbd056cd1cfb contains daily validation and weekly generation (human commits). Tracked worktree clean, only pre-existing .codegraph/ untracked. Existing day editor remains available for incomplete drafts. Reuse shared validator and saveDay atomic persistence; leave weekly behavior, schema, config, catalog revalidation and variety untouched.

## Acceptance
- Group a complete current draft by dish with recipients and dish-level Change.
- Alternatives exclude selected old dish everywhere, cover every member with liked existing meals, and use <=2 meals.
- Prefer direct swaps preserving the other dish/assignments; also show explicitly labelled full-day reorganization options.
- Canonical deduplication and deterministic ordering, including reordered inputs; no randomized retries.
- Read-only proposal loading; selecting/canceling writes nothing. Preview every resulting dish and recipient before explicit confirmation.
- Confirmation uses existing saveDay, revalidating fresh preferences; invalid/storage failures preserve saved rows and selected proposal.
- Differentiate preparation vs saving errors; guard duplicates/loading, stale date/draft responses and overlapping dialogs.

## Tasks
- [ ] R1 (implemented; device checks pending) Implement planner, read-only proposal API and dish-level preview/confirmation with focused tests.
- [ ] R2 (unit/build/native review verified; device checks pending) Verify unit suite/APK and native review; record unavailable device checks.

## Routing and verification
R1 delegated writer (multiple non-trivial files and write-preparation reads). R2 delegated verifier according to assessment plus enabled native review.
Test-first pure planner: direct swaps, combinations requiring both dishes changed, exclusion, complete liked coverage, <=2 dishes, no alternatives, shared/one-member, deduplication, reordered-input stability and input immutability. Observe behavioral RED then GREEN, not missing-symbol-only failure.
Use process-local ANDROID_HOME=/Users/sunva/Library/Android/sdk; sh ./gradlew with --offline -Pandroid.builder.sdkDownload=false --no-daemon --console=plain. Focused tests then full suite (baseline22) and assembleDebug separately. Instrumentation deps uncached/no device; author relevant Room/Compose cases but mark compilation/execution pending. No installs or config edits.

## Delivery/progress
No commits/staging/push/PR authorized. Strategy ask-on-risk; estimate 300–550 diff lines with tests, advisory not a cap. Parent owns document/mirror; do not remove tests/minify for size. Prior Room/UI checks still pending independently.
Writer reported398 diff lines (380add/18delete). RED seven tests/four behavioral failures against compiled skeleton; GREEN seven planner tests, full29tests baseline22 preserved; assembleDebug and diff --check passed. Room/Compose cases authored not compiled/executed. Independent verifier now reruns unit tasks fresh and separately builds APK. ASSESS returned unassessable pending intended-untracked declaration; independent verifier required.

## Follow-up evidence
Independent verifier freshly reran29 tests (zero failures/errors/skips) in16s; APK build succeeded5s,17,426,893 bytes. Found confirmed fixture FK defect in ChangeMealTest: preferences reference meal2 before it is inserted. One writer now fixes only meal-before-preference setup ordering; unavailable instrumentation means structural fallback, no claimed UI RED/GREEN.
Initial398-line native review approved/acknowledged authority burned, lineage review-224285abd3d47777; non-blocking informational advisory R3-001 at MealReplacementPlanner.kt:40-42 has no correction route. Initial relay failed WebSocket closed1000 with no mutation; fresh STATUS reoffered slot, subsequent run succeeded. Fixture edit will create a different candidate requiring fresh inspect.

Fixture follow-up completed: only two added lines separate parent insertion from preferences; structural FK inspection and git diff --check passed. No production/unit changes after independently executed29-test suite and APK build. Final400-line candidate reviewed and approved, lineage review-8db2718e71a5f476; exact acknowledgement returned authority burned. Informational R3-001 planner advisory remains separate later work; no correction route. No STATUS after burn.

## Blocker
- [ ] R3 Compile/run Room/Compose on available device after dependency availability is authorized. Current offline missing dependencies/no device prevent full closure of R1/R2; prior step checks also remain pending.

## Next step
Report implemented step3 and29-test/APK evidence; actual UI/storage/error lifecycle checks remain pending. No commits, config changes or next-step expansion.
