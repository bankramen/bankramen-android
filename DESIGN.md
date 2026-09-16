# Balog Mobile Design System

## 0. Reference Log

- Source: user-provided mobile concept board, 2026-09-02.
- Source: Readdy `notifications` reference, 2026-09-15.
- Source: [Balog Readdy prototype](https://readdy.cc/preview/3714b737-3bee-489d-aba3-06a012227d35/13935540/), inspected 2026-09-16. The reference phone viewport is 390 × 844 and includes 14 routes.
- Extracted grammar: warm off-white canvas, compact white cards, blue financial-summary panels, ink-colored amounts, quiet dividers, and a fixed low-elevation bottom navigation.
- Scope: Home, report, and recurring-payment behavior remains unchanged. The alert tab is a dedicated notification inbox using the existing push-notification API.

## 1. Direction

Calm personal-finance dashboard. The signature material is a pale blue monthly-summary surface against a warm white canvas; bright blue is reserved for the primary action and selected state. Monetary information is dense but never visually noisy.

## 2. Color Tokens

| Token | Value | Usage |
| --- | --- | --- |
| `Canvas` | `#F8F7F2` | Screen background |
| `Surface` | `#FFFFFF` | Cards, sheets, navigation |
| `Ink` | `#182235` | Primary text and amounts |
| `Muted` | `#7B8493` | Supporting copy and inactive navigation |
| `BrandBlue` | `#3867F4` | CTA, selected state, links |
| `BrandBlueSoft` | `#E8EFFF` | Monthly summary and selected containers |
| `SubtleSurface` | `#F1F3F7` | Icon wells and quiet controls |
| `Outline` | `#E2E6EC` | Dividers and disabled surfaces |
| `Expense` | `#DE6268` | Expense/destructive status |
| `Income` | `#2DA66B` | Income status |
| `Warning` | `#F3B540` | Attention-required state |

## 3. Typography

- Family: Android system sans-serif.
- Page title: 22sp / bold.
- Amount: 26–30sp / bold.
- Card title and row amount: 14–15sp / semibold.
- Supporting copy: 12sp / medium.
- Minimum visible copy size: 12sp.

## 4. Spacing and Layout

- Base unit: 4dp.
- Page inset: 16dp.
- Standard card inset: 20dp; feature card inset: 24dp.
- Card gaps: 12dp within a group, 16dp between groups.
- Card radius: 20dp; sheets and large feature cards: 24dp.
- The bottom navigation is fixed; each page owns its own vertical scroll.

## 5. Primitives

### Summary Card

- Pale-blue feature surface, amount-first hierarchy, full-width primary action.
- States: loading skeleton, populated, no-data.

### Content Card

- White surface with a 1dp-equivalent soft shadow; no heavy border.
- States: default, loading skeleton, empty, error.

### Primary Button

- Solid `BrandBlue`, white label, 54dp height, 16dp radius.
- States: enabled, pressed, disabled.

### Bottom Navigation

- White fixed surface, active item in `BrandBlue`, inactive item in `Muted`.
- Minimum target size: 44dp.

### Transaction Row

- Quiet icon well, merchant and metadata on the left, amount on the right.
- Expense uses `Expense`; income uses `Income`.

### Notification Inbox

- Header pairs the page title with an unread-count pill and a 40dp settings affordance.
- The inbox starts with a compact summary, then a three-way segmented filter: 전체, 파싱 성공, 인식 실패.
- Notifications are grouped by relative date and presented in 16dp-radius outlined list sections; each row has a type-tinted 36dp icon well, optional unread dot, title, body, and timestamp.
- `파싱 성공` maps only to `PAYMENT_RECORDED`; the current API has no failed-parsing status, so `인식 실패` intentionally shows its empty state until the backend contract grows.
- A pale-blue privacy notice explains the retained data boundary below the list.
- States: loading, error, filtered empty, populated. Every state retains the header and filter controls for orientation.

## 6. Interaction

- Tap feedback uses Compose defaults; no decorative animation is added.
- Screen content changes must not alter scroll ownership.
- Every tap target remains at least 44dp where the existing layout permits it.
- Filter segments retain their selected surface and label contrast; status is never conveyed by color alone.

## 7. Depth

Mixed tonal-shift and subtle shadow strategy. White cards are separated from `Canvas` with low elevation only; feature hierarchy comes from the `BrandBlueSoft` tonal shift rather than deeper shadows.

## 8. Accessibility and Accepted Debt

- Text colors target WCAG AA contrast on their declared surfaces.
- Color is paired with text/sign semantics for income, expense, and warnings.
- Existing emoji category glyphs remain as accepted debt because replacing them requires a dedicated icon asset set; this redesign does not change feature assets.
- Notification settings and read-state mutations remain accepted debt: the current OpenAPI contract supplies no mutation endpoint, so this version does not pretend to persist either action.

## 9. Screen Inventory and Folder Plan

| Flow | Reference screens | Compose folder |
| --- | --- | --- |
| Entry | 3-step onboarding, login | `feature/onboarding`, existing `feature/login` |
| Permissions | notification request, collection access, denied, setup complete | `feature/permissions` |
| Home | monthly spending, collection status, recent transactions, recurring, investment summary | existing `feature/home` |
| Transactions | month list, detail/category edit, manual add | `feature/transactions` |
| Assets | total valuation, treemap, accounts/classes/holdings | `feature/assets` |
| Reports | spending and asset analysis | `feature/reports` |
| Notifications | collection state, parsed and failed notifications | `feature/notifications` |

Keep the existing `core/auth`, `core/api`, `core/local`, and `core/notification` repositories. Each new feature folder owns a screen Composable and a ViewModel; local mock data fills prototype-only surfaces that the current APIs do not supply.

## 10. Home Screen Fidelity Contract (Readdy runtime, 2026-09-16)

The current delivery targets only `/home`. These values were read from the rendered Readdy phone at 390 × 844 with `getComputedStyle` and DOM bounds; they supersede the general tokens above for this screen.

| Home token | Runtime value | Native use |
| --- | --- | --- |
| Canvas | `#F7F4EE` (`oklch(0.968 0.009 88)`) | Phone background |
| Card | `#FDFBF7` (`oklch(0.988 0.006 88)`) | Status and transaction cards |
| Navy | `#05162F` (`oklch(0.202 0.056 258)`) | Monthly spending card |
| Ink | `#121A22` (`oklch(0.212 0.02 250)`) | Merchant and section headings |
| Muted | `#636A73` (`oklch(0.522 0.016 250)`) | Metadata |
| Border | `#E3DED4` (`oklch(0.902 0.015 88)`) | Card outlines |
| Cerulean | `#0099D5` (`oklch(0.64 0.146 232)`) | Budget progress |
| CTA | `#224C8A` (`oklch(0.422 0.112 258)`) | Fixed add button |
| Green | `#309957` (`oklch(0.608 0.138 152)`) | Lower spending |
| Attention | `#D68E14` (`oklch(0.702 0.146 72)`) | Pending category |

- Font: IBM Plex Sans KR for headings/labels, Noto Sans KR for supporting text; numerical text uses tabular figures. Native font assets come from Google Fonts and icon glyphs from Remix Icon 4.5.0.
- Header: 20px horizontal inset; phone content starts at 50px below the top; title 22px/22px bold, month 12px/18px medium. Header height is 108px.
- Scroll viewport: 20px side inset, from y=108 to y=693 (585px tall). Top summary is 350 × 202px, navy with 8px radius and 16px inset. Following gap: 12px.
- Collection card: 350 × 63px, ivory with 1px border, 8px radius, 14px horizontal and 12px vertical inset. Following gap: 20px.
- Recent card: 350 × 378px, ivory with 1px border and 8px radius. Header is 44px high; each transaction is about 65px high with a 36px tinted icon well, 13.5px merchant/amount, and 11px metadata.
- Fixed add button occupies y=693–761 with 20px side inset; button itself is 350 × 48px, CTA blue and 8px radius. Five-item nav occupies y=761–844 with ivory background, 1px top border, 21px icons, and 10.5px labels.
- Repeat the same 8px radius and quiet 1px outline for lower content cards. Preserve the real HomeViewModel data path. Readdy sample values are visible only when `allowMockData` is explicitly enabled for a preview; production defaults to live or empty data.
