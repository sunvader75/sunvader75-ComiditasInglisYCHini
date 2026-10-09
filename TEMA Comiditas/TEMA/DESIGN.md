---
name: Domestic Harmony & Meal Planning
colors:
  surface: '#fef7ff'
  surface-dim: '#dfd7e4'
  surface-bright: '#fef7ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f8f1fe'
  surface-container: '#f3ebf8'
  surface-container-high: '#ede5f2'
  surface-container-highest: '#e7e0ec'
  on-surface: '#1d1a23'
  on-surface-variant: '#494551'
  inverse-surface: '#322f38'
  inverse-on-surface: '#f6eefb'
  outline: '#7a7582'
  outline-variant: '#cac4d2'
  surface-tint: '#6650a4'
  primary: '#4e378a'
  on-primary: '#ffffff'
  primary-container: '#6650a4'
  on-primary-container: '#dfd2ff'
  inverse-primary: '#cfbdff'
  secondary: '#625b71'
  on-secondary: '#ffffff'
  secondary-container: '#e8def9'
  on-secondary-container: '#686177'
  tertiary: '#633b48'
  on-tertiary: '#ffffff'
  tertiary-container: '#7d5260'
  on-tertiary-container: '#ffcbda'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e8ddff'
  primary-fixed-dim: '#cfbdff'
  on-primary-fixed: '#21005d'
  on-primary-fixed-variant: '#4e388a'
  secondary-fixed: '#e8def9'
  secondary-fixed-dim: '#ccc2dc'
  on-secondary-fixed: '#1e192b'
  on-secondary-fixed-variant: '#4a4358'
  tertiary-fixed: '#ffd9e3'
  tertiary-fixed-dim: '#eeb8c8'
  on-tertiary-fixed: '#31111d'
  on-tertiary-fixed-variant: '#633b48'
  background: '#fef7ff'
  on-background: '#1d1a23'
  surface-variant: '#e7e0ec'
typography:
  display-lg:
    fontFamily: Roboto Flex
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  display-lg-mobile:
    fontFamily: Roboto Flex
    fontSize: 36px
    fontWeight: '400'
    lineHeight: 44px
    letterSpacing: -0.25px
  headline-md:
    fontFamily: Roboto Flex
    fontSize: 28px
    fontWeight: '400'
    lineHeight: 36px
    letterSpacing: 0px
  title-lg:
    fontFamily: Roboto Flex
    fontSize: 22px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: 0px
  title-md:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-sm:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  body-lg:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: Roboto Flex
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Roboto Flex
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Roboto Flex
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system blends **Corporate / Modern Material Design 3** discipline with a warm, domestic culinary atmosphere. Built to manage household meal schedules, dietary preferences, and family food logistics, the system resolves daily coordination friction into calm, predictable clarity. 

The aesthetic is characterized by:
- A light-drenched, immaculate white foundation that evokes kitchen cleanliness and organization.
- Royal Iris Violet anchor accents that offer decisive, reassuring interactive affordances without visual noise.
- A cheerful, semantic multi-color taxonomy used strictly for human identity: color-coding household members to make weekly rosters legible at a glance.
- Tactile, accessible targets with comfortable thumb reaches, generous card separation, and clear information density suited for multitasking domestic environments.

## Colors

The color palette centers on functional clarity and familial warmth. Surfaces remain bright and neutral to let meal imagery, meal titles, and member tokens drive visual focus.

### Core Hierarchy
- **Primary (`#6650a4`)**: Royal Iris Violet. The signature brand and primary action color. Applied to prominent call-to-action buttons, key state indicators, and floating controls.
- **Secondary (`#625b71`)**: Slate Lavender. Used for secondary badge fills, sub-navigation indicators, and complementary filters.
- **Tertiary (`#7d5260`)**: Dusty Rose. Applied sparingly to specialty highlights, category distinctions, and culinary accents.
- **Neutral (`#49454f`)**: Neutral Slate. Governs secondary descriptions, outline borders, empty state messaging, and subtle iconography.
- **Background & Canvas (`#ffffff`)**: Root canvas delivering high contrast against typography and color markers.

### Domain Identity Palette (Household Member Markers)
To represent individual family members across menus, preference matrices, and schedules, use the dedicated semantic rainbow tokens:
- Coral Red: `#e57373`
- Sage Green: `#81c784`
- Sky Blue: `#64b5f6`
- Soft Amber: `#ffb74d`
- Muted Orchid: `#ba68c8`
- Seafoam Teal: `#4db6ac`

Never use member identity colors for generic system states (such as success or warning); reserve them strictly for user assignment dots, avatar chips, and personal dietary indicators.

## Typography

The type system prioritizes mobile readability, clear hierarchical rhythm, and neutral presentation so food names and meal calendars read without ambiguity.

- **Primary Typeface**: `Roboto Flex` provides reliable cross-platform legibility with consistent tabular and proportional numerals for calendars and ingredient quantities.
- **Display & Headings**: Reserved for major view transitions and top navigation app bars. Keep day headings (`title-md`) and dish labels (`title-sm`) compact enough to prevent wrapping when displayed side-by-side with preference tags.
- **Labels & Microcopy**: Form labels and buttons strictly utilize `label-lg` with medium weight (500) to ensure clear tap boundaries. Member name tags and chips use `label-md` for balanced scale against 12px color indicators.

## Layout & Spacing

The layout model is mobile-first, adhering to an intentional 4px base increment across all internal padding, margins, and gaps.

### Responsive Structure
- **Mobile (<600px)**: Single-column vertical flow with `margin: 1rem` (16px) outer edge boundary. Cards stack linearly with `space-sm` (8px) vertical separation. Menu schedules render sequentially through the 7-day week (Monday to Sunday).
- **Tablet (600px–1024px)**: 8-column layout with `gutter: 1rem` and `margin: 1.5rem`. Daily menu grids may reflow into a 2-column card arrangement.
- **Desktop (>1024px)**: Fixed-width centered canvas (max 1200px) or 12-column layout with `gutter-desktop: 1.5rem` and `margin-desktop: 2rem`. Weekly schedule displays as a structured 7-column calendar board.

### Spacing Cadence
- Use `space-xs` (4px) strictly for micro element distances (e.g., between an assignment color dot and recipient name).
- Use `space-sm` (8px) for title-to-subtitle relationships and vertical card stack gaps.
- Use `space-md` (12px) for form control vertical spacing and dialog action arrangements.
- Use `space-lg` (16px) for interior card padding and mobile outer viewport margins.
- Maintain a minimum 48px by 48px touch bounding box for every interactive trigger.

## Elevation & Depth

Visual hierarchy relies on **Tonal Layers** paired with restrained ambient drop shadows. Rather than aggressive z-index stacking, depth reflects physical kitchen recipe cards resting on a clean surface.

- **Level 0 (Canvas Base)**: Pure White (`#ffffff`). Flat background for master screens and views.
- **Level 1 (Resting Cards & Lists)**: Surface Container Low (`#f7f2fa`) or White (`#ffffff`) with subtle ambient shadow: `0 1px 3px 0 rgba(0, 0, 0, 0.08), 0 1px 2px -1px rgba(0, 0, 0, 0.05)`. Used for daily menu cards and meal list rows.
- **Level 2 (Active & Navigational Elements)**: Surface Container (`#f3edf7`) with elevated shadow: `0 3px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.06)`. Applied to active day cards, search bars, and floating toolbar groupings.
- **Level 3 (Modals & Overlays)**: Surface Container High (`#ece6f0`) with diffuse shadow: `0 10px 15px -3px rgba(0, 0, 0, 0.12), 0 4px 6px -4px rgba(0, 0, 0, 0.08)`. Applied to add/edit meal dialogs and bottom sheets.
- **Floating Action Button**: Primary Container (`#eaddff`) or Iris Violet (`#6650a4`) at 6dp ambient elevation, dropping to 12dp on active tap.

## Shapes

The design language uses roundedness level `2` (0.5rem base radius) to establish an approachable, friendly kitchen aesthetic while retaining crisp structural alignment.

- **Base Radius (`0.5rem` / 8px)**: Applied to input text boxes, compact containers, and contextual popovers.
- **Large Radius (`1rem` / 16px)**: Applied to weekly menu cards, modal dialogs, and recipe overview sheets.
- **Full Radius / Pill (`9999px`)**: Reserved for primary action buttons, filter preference chips, status badges, and circular member color swatches.

## Components

### Buttons
- **Primary Action**: Pill-shaped (`rounded-full`), solid Royal Iris background (`#6650a4`), text in white `label-lg`. Padding: 10px vertical, 24px horizontal. Minimum height 48px.
- **Secondary / Text Button**: Transparent background, text in Royal Iris (`#6650a4`). Used for inline triggers like "Editar", "Cambiar", and "Cancelar".
- **Floating Action Button (FAB)**: Pinned to bottom-right corner with 16px margin. 56px x 56px dimension, rounded-2xl or rounded-full, with an icon centered. Enforces roster rules (e.g., hidden when family capacity reaches maximum).

### Chips & Badges
- **Filter Chips**: Pill-shaped controls used in dialogs to associate family members with dishes ("¿A quién le gusta?"). Unselected state uses an outline border (`#79747e`); selected state fills with Primary Container (`#eaddff`) and displays a check icon.
- **Member Assignment Dot**: 12px x 12px circle filled with the member's assigned color token. Positioned adjacent to recipe titles or meal descriptions.
- **Color Picker Swatches**: 40px circular buttons displaying available palette colors. Selected swatch displays a 3px outer contrast ring and a centered white checkmark.

### Cards
- **Weekly Day Card**: Elevated surface with 16px internal padding and 16px corner radius. Features weekday name in `title-md`, date caption in `body-sm`, and nested dish rows. If empty, displays "Sin comidas asignadas" in muted `body-md`.
- **Meal Directory Item**: Full-width card with 16px padding. Displays dish name in `title-md`, trailing edit/delete icon buttons, and recipient tag list in `body-md`.

### Form Inputs
- **Outlined Text Field**: 8px corner radius. Idle border: 1px `#79747e`. Focused border: 2px Royal Iris (`#6650a4`). Includes floating label typography in `body-sm` and a minimum 48px height.

### Dialogs & Modals
- **Alert & Edit Modals**: 16px rounded corners on elevated surface containers. Structured with 16px internal padding, title in `title-lg`, vertical scrollable stack for inputs with 12px gaps, and bottom right-aligned action buttons ("Cancelar" and "Guardar").