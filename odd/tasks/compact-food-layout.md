# Compact food layout

## Objective
Honor user correction: remove unnecessary home copy outside navigation buttons, remove weekly date range and 'Tu semana...' heading, restore one-row previous-icon / generate-week button / next-icon controls, and remove decorative card icons.

## Scope
HomeScreen.kt, CalendarScreen.kt weekly header/cards, MealsScreen.kt cards and relevant UI tests. Keep texts inside home buttons, warm theme, member identity markers, functional back/add/delete icons, all callback/enablement/save logic. Calendar dialog dates and content unchanged. No commits unless explicitly requested.

## Tasks
- [x] C1 (verified; no commit requested): Compact home, weekly controls and meal/day cards; adapt UI tests to accessible icon selectors and absence of unnecessary copy.
- [x] C2 (build/static/native review verified; device checks skipped): Verify unit/build/lint/Android-test compilation and review as enabled. Device visual checks only if device available.

## Checks
Three home destinations retained with button-associated text only. Weekly header has no date range or marketing heading; previous/generate/next share one Row with accessible descriptions and 48dp touch targets. Decorative food icons absent from cards. Do not remove member colors or functional controls. Preserve large-text reachability via wrapping central button, not stacked controls. Run bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest.

## Progress
C1 implemented across three screens and two tests: home only titled/subtitled navigation cards, weekly one-row 48dp icon controls around weighted generate button without heading/date range, meal/day card decorations removed. Identity badges and dialog content preserved. Tests updated before source; runtime RED/GREEN not observed. Structural diff/readback passed; independent unit/build/lint/Android compile passed exit 0; 48 unit tests zero failures, lint 0 errors/60 warnings/2 informational. adb reports zero devices; instrumentation/runtime rendering not executed.

## Next step
Native review review-ac78396d443a8d38 approved and exact acknowledgement completed (authority burned). ASSESS unavailable due explicit-untracked declaration requirement; conservative independent verifier already ran. Next: inspect compact/2x rendering and execute UI tests when device available. No commits made/requested.
