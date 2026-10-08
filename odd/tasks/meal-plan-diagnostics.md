# Meal plan diagnostics

## Goal and scope
User authorized step 4: explain impossible meal generation/replacement with actionable Spanish messages rather than generic failure. Baseline HEAD69a9573 includes human-committed step3; tracked worktree clean except existing .codegraph/. Preserve optimizer, weekly generator, replacement enumeration, shared validator and atomic persistence unchanged. No variety, member management, automatic repair or preference-change revalidation. Prior Room/UI checks remain pending.

## Acceptance
- Diagnose no members, unsupported/duplicate members, empty catalog, named people with no liked available dishes, and true incompatibility within two dishes.
- Eligible likes intersect actual catalog; stale-only preference IDs must not imply feasibility.
- Deterministic ID ordering and messages using operation snapshot; no mutable last-failure state.
- Explain missing compatible shared pair for three people without assigning individual blame; suggest configuring shared tastes/comidas.
- Replacement validates current complete draft and old-dish presence first; diagnoses catalog excluding old dish, distinguishes no alternatives from impossible daily plan. Never promise an invalid draft is valid.
- Explained failures preserve persisted day/week and unsaved drafts. Keep storage/preparation technical errors distinct; cancellation still propagated.
- Reuse existing user-message channels, no broad UI redesign/signature churn.

## Tasks
- [ ] D1 (implemented; device checks pending) Implement pure diagnostics and failure messaging integration with deterministic regression tests.
- [ ] D2 (unit/build/review verified; device checks pending) Verify fresh unit suite/APK and native review; retain unavailable Room/UI checks.

## Routing and checks
D1 one delegated writer due multiple non-trivial files/preparation reads. D2 independent verifier as assessment requires and native review when enabled.
Test-first meaningful behavioral RED then GREEN: precedence, stale likes, three exclusive likes, compatible pair, reordered snapshots, invalid draft vs valid daily/no replacement, diagnostic agreement with optimizer for exhaustive small preference matrices. Baseline29 unit tests preserved/adapted only where explained errors intentionally alter expectations; no behavior search policy change. Author targeted existing Room/Compose cases for preservation/messages but no fabricated device execution.
Use ANDROID_HOME=/Users/sunva/Library/Android/sdk sh ./gradlew --offline -Pandroid.builder.sdkDownload=false --no-daemon --console=plain; full tests and assembleDebug separately. No config/SDK/dependency edits or installs. Instrumentation remains unavailable offline/no device.

## Delivery
No commits/staging/push/PR authorized. Strategy ask-on-risk; expected 250–450 authored diff lines incl tests, advisory not a cap. Preserve tests/readability. Parent owns task document and mirror.

## Evidence
Writer added241/removed4 lines (245total), typed snapshot diagnostics and explained failure integration. Behavioral RED four failures; GREEN6 diagnostic tests incl584 preference matrices agreeing with optimizer. Full35 tests retains baseline29; APK and diff --check pass. Search/validator/repository algorithms unchanged. Room/Compose assertions authored not compiled/executed (offline deps/no device). ASSESS unassessable until untracked declaration; independent verifier running fresh unit suite and APK.

Native medium/reliability review approved241-line candidate, lineage review-cf557e810b50c7cb. Exact acknowledgement returned authority burned. No further STATUS/source mutations. Independent verifier freshly executed35 tests in16s (zero failures/errors/skips), baseline29 unchanged; APK build5s and ZIP CRC inspection passed,17,427,020 bytes. diff --check passed. No confirmed candidate defects or fixture/assertion mismatches; instrumentation not compiled/executed. Room/Compose/cancellation races and unsaved-draft runtime preservation remain unverified.

## Blocker
- [ ] D3 Compile/execute instrumentation and UI checks after dependencies/device become available. D1/D2 not fully closed while applicable runtime checks remain unavailable.

## Next step
Report step4 implemented and35-test/APK/review evidence, with pending device verification. No automatic step5, commits/config/publishing.
