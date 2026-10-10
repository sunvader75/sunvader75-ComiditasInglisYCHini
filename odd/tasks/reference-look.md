# Reference-inspired Comiditas look

## Objective
Replace the improvised warm style with a cohesive new visual identity inspired by user-provided TEMA Comiditas design/colors/screens, not a 1:1 clone.

## Direction and rationale
Deep iris violet primary (#4e378a), near-white lilac canvas (#fef7ff), white resting cards, lavender inset surfaces, restrained borders/shadows, 16dp card corners, pill actions/chips, clear scalable typography. Resolve inconsistent reference tokens coherently rather than reproducing contradictions. Keep system fonts/no dependency; translate Roboto Flex hierarchy. MemberColors already match reference rainbow identities: preserve all persisted values.

## Scope and constraints
Presentation only. Preserve prior explicit compact requirements: home only three navigation buttons and associated title/subtitle, no marketing/dashboard copy or decorative card icons; weekly previous-icon/generate/next-icon single Row, no range/hero header. Preserve live meal likes/name editing, member limits/colors, weekly/daily generation/replacement/save/cancel guards and tested labels. No reference-only photos, timings/categories/ingredients/search/filters/allergies/roles/stats/bottom navigation or fake success. Reference folder read-only; preserve staged/uncommitted earlier source/reference work and .idea anomaly. No cleanup/reset, dependency/catalog/domain/data/navigation edits or commits without explicit request.

## Tasks
- [x] R1 (build/static verified; device checks R7; no commit requested): Establish violet/lilac theme, typography/shapes/shared identity primitives and native API26/27 resource consistency.
- [ ] R2 (in progress): Restyle compact home navigation tiles using reference identity without adding copy/icons.
- [ ] R3 (pending): Restyle meals/likes cards and editor with compact lavender inset/chips.
- [ ] R4 (pending): Restyle members/cards/editor with preserved identity colors and actions.
- [ ] R5 (pending): Restyle weekly overview while preserving single-row controls and grouped recipients.
- [ ] R6 (pending): Restyle day/manual/replacement dialogs with intact workflow guards.
- [ ] R7 (pending): Final functional/structural verification and native review as enabled; actual device/rendered compact checks when available.

## Acceptance and checks
Visual identity recognizably inspired by references but tailored to current feature set. Contrast >=4.5:1 normal text; 48dp interactive targets; cards/dialogs wrap/scroll at compact 320x480 and 2x fonts; week controls remain horizontal. Preserve all callbacks/labels and state behavior. Per-unit bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug; compile Android tests as touched/final. No meaningful deterministic styling RED; use applicable test-first only for executable behavior/structure assertions. Previews are not rendered evidence. Record warnings/failures/skips truthfully.

## Evidence
Scout inspected DESIGN.md, four HTMLs and four PNGs; references have contradictory tokens and demonstration-only handlers. Estimated six focused UI slices around 700–1200 authored diff lines overall, consistent with requested total redesign. Current staged references/prior redesign are preserved. R1 source styling implemented across theme/shared/native files; existing APIs and MemberColors preserved. Structural diff/XML/baseline/contrast checks passed: white/iris 9.39:1, container pairs >=13.22:1, error 6.46:1, all surface text >=4.61:1. Native API26 dark nav fallback and API27+ lilac/dark-icon settings retained. Styling RED not applicable; independent build/unit/lint passed exit 0: 48 tests/7 suites without failures, lint 0 errors/62 warnings/2 informational. Twelve principal contrast pairs independently calculate 6.45–17.15:1. Rendering/device acceptance remains pending R7. Writer observed external staging changes but performed no Git mutations; preserve current index.

## Next step
Implement R2 compact home after R1 checks passed. No rendered proof yet; no source writes outside derived allowlists.
