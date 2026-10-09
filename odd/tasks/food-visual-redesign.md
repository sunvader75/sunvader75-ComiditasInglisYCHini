# Food app visual redesign

## Objective and rationale
Create a polished, coherent family-food-planner interface instead of template styling. User approved full redesign in staged work units (estimated 880–1490 authored diff lines overall).

## Design direction
Light-only cream background, warm-white surfaces, terracotta primary, muted olive accents and cocoa text. Complete typography hierarchy, rounded cards/dialogs, subtle borders, consistent spacing and meaningful food icons. Preserve stored member colors as identity, with names always visible. No new photos/dependencies or navigation/product features.

## Scope and constraints
Presentation only: theme/native launch colors, shared primitives, home, meals, members, weekly overview and day dialogs. Preserve Spanish UI and tested semantics, unified meals/likes, IDs, saving/cancel behavior, generation/replacement logic and member limits. No whole-file formatting, persistence changes, version catalog edits, deletion UX or dark-mode expansion. Working tree clean except pre-existing .codegraph. No commits without explicit request. Historical instrumentation import/device blocker must be rechecked, not assumed resolved.

## Tasks
- [x] V1 (verified; no commit requested): Build theme foundation and small shared visual primitives, native color alignment and themed previews.
- [ ] V2 (implemented; device validation blocked): Redesign home hierarchy/navigation cards with scrollable compact layout.
- [ ] V3 (implemented; device validation pending): Redesign unified meal cards/editor without changing selection/save/cancel contracts.
- [ ] V4 (implemented; device validation pending): Redesign member cards/editor while preserving identity colors and limits.
- [ ] V5 (implemented; device validation pending): Redesign weekly overview with clearer date/navigation/meal-recipient hierarchy.
- [ ] V6 (implemented; rendered checks pending): Restyle day dialogs without changing assignment/replacement/save behavior.
- [ ] V7 (in progress): Final build/lint/regression, device/screenshot checks when available, native review as enabled.

## Acceptance and verification
Consistent theme across Compose and launch UI; readable text/contrast, 48dp touch targets, no fixed heights clipping large text, compact screens scroll where needed. All existing actions and tested labels remain. Unit tests/build/lint per stage; instrumentation compilation/device checks as available. Preview validation is not screenshot/device evidence. Styling has no meaningful deterministic visual RED; use structural/functional checks and test-first only where behavior actually changes. Record failed/skipped/unavailable checks explicitly.

## Evidence and progress
V1 implemented by bounded writer: full warm light semantic palette, 15 typography roles, rounded shapes/spacing tokens, headers/decorative icons/member badges/empty states and standard/2x previews; native cream system/launch surfaces aligned. Stored member palette unchanged. Structural diff/XML/readback checks passed. Eleven semantic contrast pairs >=4.5:1 (minimum 5.63:1). No deterministic styling RED applicable. Independent checks: 48 unit tests passed and assembleDebug passed. lintDebug failed with 2 NewApi errors for windowLightNavigationBar (API27 vs min26) at values/themes.xml:9,17; correction applied with values-v27/themes.xml overrides. Base API26 navigation bar uses black for contrast with light icons; API27+ retains cream/dark icons. Independent recheck passed: exit 0, assembleDebug and lintDebug successful (0 errors, 59 warnings); unit task UP-TO-DATE with retained 48-test passing reports. adb devices reported no devices; no screenshots or device checks. 59 lint warnings reported. Previews are not screenshot evidence.

## V2 evidence
Home redesigned with welcoming header, prominent weekly planning, differentiated cards, shared theme primitives and scrollable compact/2x-font previews. All three destinations and Spanish labels preserved. Added HomeScreenTest callback and compact accessibility coverage. Structural diff/readback passed; Unit/build/lint checks passed exit 0 (48 tests without failures). Android test compilation failed exit 1: pre-existing invalid onNode import in GenerateWeekTest.kt:7 (verified in HEAD, not changed). Two new HomeScreen instrumentation tests remain unexecuted. Previews not screenshot evidence.

## V3 evidence
Meal cards/editor restyled with shared icon/identity badges, refined loading/empty states and add action; editor scrolls, with preserved labels/callbacks/preselection/cancel/saving/error/blank guards. Added 2x-font last-member reachability test and previews. Structural checks passed; independent unit/build/lint passed exit 0, 48 tests without failures; lint 0 errors/59 warnings/2 informational. Device tests unexecuted. Added 2x-font test does not constrain viewport; compact-layout coverage gap must be addressed in V7 before claiming proof.

## V4 evidence
Members styled with shared warm cards/header/empty state/add action; scrolling editor and wrapping 48dp color selection. Identity colors, <6 add limit, recommended-three label, blank validation, untrimmed callback payload, immediate delete and dialog-reset semantics preserved by structural inspection. Added four MemberDialog tests and standard/320x480 2x previews; not executed/rendered. Independent build/unit/lint passed (48 tests zero failures, lint 62 findings no errors). New contentModifier naming warning at MembersScreen.kt:210 and deprecated ArrowBack at :122 are cleanup follow-ups. Four device tests unexecuted; no runtime proof.

## V5 evidence
Weekly overview restyled with warm date-range header, separately stacked navigation/generation controls, grouped dish/recipient cards and scrolling header. Added constrained compact/2x reachability/callback test and previews. Diff 174 additions/37 deletions; structural checks confirmed dialogs byte-equivalent to HEAD and date/state strings/data-binding/callbacks preserved. Independent build/unit/lint passed exit 0: 48 tests zero failures, lint 0 errors/60 warnings/2 informational. No confirmed source functional defects. Compact test uses empty days/bypasses production guards; no populated-card/dialog runtime proof. No rendered evidence.

## V6 evidence
Day/manual dialogs restyled with warm section hierarchy, member badges, distinct errors, scrollable main actions and 48dp controls. Changed ChangeMealTest uses app theme and scrolls to action controls. Structural checks preserve state initialization/callbacks/guards/replacement confirmation/tested strings. Four compact/2x previews unrendered; no explicit compact dialog viewport tests added. Independent build/unit/lint passed: 48 tests zero failures, debug assembly/lint successful. No confirmed V6 regression from source inspection; rendered reachability pending.

## Next step
V7: user explicitly approved prior invalid onNode import fix; parent removed only that import from GenerateWeekTest.kt. assembleDebugAndroidTest passed and test APK packaged. adb reports no devices; instrumentation execution/screenshots/rendered compact/font checks remain pending. Native review START twice stopped with consent-binding-expired, lineage_created=false; no review verdict/authority created. Do not replay expired bindings; a fresh consent/start is required if resumed. Source visual implementation complete; runtime validation pending. Minor naming/deprecation and viewport coverage improvements remain follow-ups. No commits made. V2 remains unchecked due pre-existing instrumentation blocker; resolving that unrelated import requires authorization before V7 device tests. Rendered/device checks remain V7 pending. No commits without explicit user request.
