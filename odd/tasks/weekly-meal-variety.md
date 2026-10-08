# Weekly meal variety

## Goal and authorized policy
Implement step 5. User chose variety-first: prefer two fresh dishes over a previously used shared dish. Never fail solely because repetition is unavoidable. Keep <=2 dishes, full liked coverage, 1–3 members, existing week atomic replacement and diagnostics. Daily generation without week history retains one-shared-dish preference.

## Baseline/scope
HEAD001ab29 includes human-committed diagnostics. Tracked worktree clean except existing .codegraph/. Optimizer already shuffles feasible splits when no common dish exists: no fixed-pair bug to claim. Add week-local used-dish preference only; replacement proposal ordering intentionally stays deterministic. No UI/config/schema/persistence/preference-revalidation changes or global history.

## Acceptance
- Rank feasible daily plans by number of distinct already-used dish IDs ascending; then number of dishes ascending. Therefore two unused dishes beat one repeated dish, but equally fresh shared dish wins.
- Choose randomly among equally ranked feasible groupings and dishes using injected Random; preserve all valid pairs and max2 coverage. Uniform grouping selection, not a promise of uniform complete assignments.
- Week generator tracks dishes already chosen earlier in that generated week, passes soft avoidance; repetitions allowed if needed.
- No greedy first-group pick that incorrectly misses lower-repeat alternatives; no same-ID group overwrite/drop of recipients when common dishes overlap splits.
- Stable seeded behavior, same feasible snapshots remain feasible, allseven days prepared before persistence. No global optimum over entire week promised: greedy day-by-day preference.

## Tasks
- [ ] V1 (implemented; functional domain checks verified) Implement soft repetition scoring and weekly history with deterministic regression tests.
- [ ] V2 (unit/build/independent checks verified; native review blocked) Verify fresh domain suite/APK and native review; record unchanged device-test blockers.

## Routing/checks
V1 delegated writer, multiple non-trivial files and preparation reads. V2 independent verifier according to native assessment and enabled native review.
Test-first meaningful RED:7 universally liked dishes produce no weekly repeats; fresh split beats repeated shared; best-repeat score across partitions; fallback unavoidable repeated shared; overlapping likes never lose recipients; seeded determinism and invariant seeds; same allowed snapshot feasibility as diagnostics. Preserve35-test baseline and replacement deterministic tests. Pair-variety currently passes, not a RED bug.
Use ANDROID_HOME=/Users/sunva/Library/Android/sdk sh ./gradlew --offline -Pandroid.builder.sdkDownload=false --no-daemon --console=plain; focused unit, full unit and assembleDebug separately. No downloads. Existing Room/Compose/device checks still pending; no additional UI surfaces.

## Delivery
No commits/staging/push/PR authorized. Strategy ask-on-risk; forecast200–350diff lines including tests, advisory. Keep code/readability/tests, no budget minification. Parent owns document/mirror.

## Routing correction
Initial writer stopped before edits because parent omitted com/ in three exact allowed paths. Human explicitly approved corrected four-file list: main/java/com/comiditas/familia/domain/optimizer/{MealAssignmentOptimizer,WeekMealPlanGenerator}.kt and test/java/com/comiditas/familia/domain/optimizer/{MealAssignmentOptimizerTest,WeekMealPlanGeneratorTest}.kt (under app/src). Original verification/design unchanged. Corrected writer is now running; no source/tests edited before approval.

## Evidence
Writer changed only4authorized files,117add/11delete. RED focused11tests failed no-repeat fixture against baseline; GREEN17focused, full42tests preserves baseline35/diagnostics/replacement. APK/diffcheck pass. usedMealIds trailing empty-default keeps positionalRandom compatibility; shared candidates complete, splitdishIDs distinct. Existing Room/device checks remain pending. ASSESS unassessable untracked selection; independent verifier fresh42suite/APK running.

## Native review blocker
Two START attempts after fresh inspect failed preflight invalid_request: untracked inventory changed; mutation_outcome not_started/none, no lineage created. Stop blind retries. Parent Git inventory shows only .codegraph/.gitignore and odd/tasks/weekly-meal-variety.md untracked; no unexpected visible source mutations. Root cause not established. Native review unavailable, not approved. Explicit ASSESS unavailable returns unassessable/high and requires independent verifier (already running). No review mode/maintenance changes authorized.
- [ ] V3 Resolve native preflight inventory blocker before native review can run.

## Independent final evidence
Verifier freshly executed42tests in17s, zero failures/errors/skips; APK build5s, ZIP integrity valid (manifest+16DEX),17,427,020 bytes; git diff --check pass. No confirmed candidate defects and no source/config/install/device mutations. Source confirmed allpartitions compete by repeateddistinct IDs then dishcount, distinctsplit IDs preserve coverage, weekhistory local. Baseline35 preserved. Prior Room/UI runtime verification remains pending. Native review remains unavailable, no approval.

## Next step
Report step5 implemented with42 fresh-test/APK/independent review evidence, plus native-review blocker and device checks. No commits or automatic further work.
