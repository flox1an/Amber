# Amber UI Redesign — Dark & Premium Material 3

**Date:** 2026-03-14
**Status:** Approved

## Summary

Redesign the entire Amber app UI to feel professional, polished, and premium. Dark theme with refined amber accents, following Android Material 3 guidelines. Spacious layouts with all functionality preserved — content is shortened, hidden behind expandable sections, or refined rather than removed.

## Design Direction

- **Aesthetic:** Dark & premium, inspired by Signal/Telegram dark mode
- **Framework:** Material 3 (Jetpack Compose) — no library changes needed
- **Color shift:** Primary amber refined from `#FFCA62` to `#D4AF55` (warmer, more premium against dark surfaces)
- **Layout:** Spacious & focused — generous padding, clear visual hierarchy, information layered

## Design Tokens

### Color Palette (Dark Theme)

All alpha-blended colors expressed in Compose notation: `Color(hex).copy(alpha = f)`.

| Token | Compose Value | Usage |
|-------|---------------|-------|
| Background | `Color(0xFF0F0F14)` | Root background |
| Surface | `Color(0xFF16161D)` | Top bar, bottom nav, elevated containers |
| SurfaceContainer | `Color(0xFF1A1A24)` | Cards, list items, input fields |
| Primary (Amber) | `Color(0xFFD4AF55)` | CTAs, active nav, accent text, FAB |
| PrimaryContainer | `Color(0xFFD4AF55).copy(alpha = 0.15f)` | Pill highlights, active tab backgrounds |
| OnPrimary | `Color(0xFF0F0F14)` | Text on amber buttons/FAB |
| OnSurface | `Color(0xFFE8E4E0)` | Primary text |
| OnSurfaceVariant | `Color(0xFF888888)` | Secondary text, subtitles |
| Outline | `Color.White.copy(alpha = 0.06f)` | Borders, dividers |
| OutlineVariant | `Color.White.copy(alpha = 0.04f)` | Subtle dividers (nav border) |
| AmberSubtle | `Color(0xFFD4AF55).copy(alpha = 0.08f)` | Card borders, subtle highlights |
| AmberMuted | `Color(0xFFD4AF55).copy(alpha = 0.12f)` | Chip backgrounds, icon tints |
| Success | `Color(0xFF66BB6A)` / bg: `Color(0xFF4CAF50).copy(alpha = 0.15f)` | Auto-accept badges, relay connected |
| Warning | `Color(0xFFFFB74D)` / bg: `Color(0xFFFFB74D).copy(alpha = 0.15f)` | Always-ask badges |
| Error | `Color(0xFFEF5350)` / bg: `Color(0xFFEF5350).copy(alpha = 0.15f)` | Reject badges, danger buttons |

### Color Palette (Light Theme)

| Token | Compose Value | Usage |
|-------|---------------|-------|
| Background | `Color(0xFFFFFBF5)` | Root background |
| Surface | `Color(0xFFF5EFE6)` | Top bar, bottom nav |
| SurfaceContainer | `Color(0xFFFFFFFF)` | Cards, list items |
| Primary (Amber) | `Color(0xFFB8922E)` | Darker amber for light bg contrast |
| PrimaryContainer | `Color(0xFFB8922E).copy(alpha = 0.10f)` | Active tab backgrounds |
| OnPrimary | `Color(0xFFFFFFFF)` | Text on amber buttons |
| OnSurface | `Color(0xFF1C1B1F)` | Primary text |
| OnSurfaceVariant | `Color(0xFF666666)` | Secondary text |
| Outline | `Color.Black.copy(alpha = 0.08f)` | Borders |
| OutlineVariant | `Color.Black.copy(alpha = 0.04f)` | Subtle dividers |
| AmberSubtle | `Color(0xFFB8922E).copy(alpha = 0.06f)` | Card borders |
| AmberMuted | `Color(0xFFB8922E).copy(alpha = 0.10f)` | Chip backgrounds |
| Success | `Color(0xFF2E7D32)` / bg: `Color(0xFF2E7D32).copy(alpha = 0.10f)` | Accept badges |
| Warning | `Color(0xFFE65100)` / bg: `Color(0xFFE65100).copy(alpha = 0.10f)` | Ask badges |
| Error | `Color(0xFFC62828)` / bg: `Color(0xFFC62828).copy(alpha = 0.10f)` | Reject badges |

### Typography

All styles map to `MaterialTheme.typography` slots.

| M3 Slot | Size | Weight | Tracking | Usage |
|---------|------|--------|----------|-------|
| `titleLarge` | 18sp | SemiBold (W600) | -0.3sp | Screen titles in top bar |
| `titleMedium` | 14sp | SemiBold (W600) | -0.2sp | Card titles, app names, setting titles |
| `bodyMedium` | 13sp | Normal (W400) | 0 | Descriptions, event content |
| `bodySmall` | 12sp | Normal (W400) | 0 | Subtitles, timestamps, current values |
| `labelSmall` | 11sp | SemiBold (W600) | 1sp | Section headers (uppercase) |
| `labelMedium` | 10sp | Medium (W500) | 0.3sp | Badges, nav labels, chip text |

Font family: `FontFamily.Default`.

### Shapes

| Element | Radius |
|---------|--------|
| Cards, list items, grouped settings | 16dp |
| App icons | 12dp |
| Buttons (primary/outline/danger) | 12dp |
| FAB | 16dp |
| Pills, chips, badges | 20dp |
| Search bar | 28dp |
| Bottom sheet handle | 2dp |
| Bottom sheet top corners | 24dp |
| Setting icon containers | 8dp |

### Spacing

| Context | Value |
|---------|-------|
| Screen horizontal padding | 16dp |
| Card internal padding | 16dp |
| Gap between cards in lists | 10dp |
| Top bar padding | 16dp vertical, 20dp horizontal |
| Bottom nav padding | 12dp top, 20dp bottom |
| App icon size (list) | 44dp |
| App icon size (header/detail) | 52dp |
| Mini app icon (activity list) | 24dp with 6dp radius |
| Icon-to-text gap | 14dp |
| Section label padding | 8dp top, 4dp bottom |

## Screen-by-Screen Design

### 1. Applications Screen (Main Tab)

- **Top bar:** `TopAppBar` (not CenterAligned — title is left-aligned). Title "Applications" in `titleLarge`. Right side: relay status chip (green dot + "2/3 relays") in a pill with AmberMuted background
- **Search bar:** 28dp radius, SurfaceContainer background, Outline border, search icon + placeholder in OnSurfaceVariant
- **App list:** `LazyColumn` of cards. Each card:
  - App icon (44dp, 12dp radius) — PackageManager icon when available, fallback to first-letter on gradient
  - App name (`titleMedium`)
  - Subtitle: permission count + last-used time (`bodySmall`, OnSurfaceVariant)
  - Chevron on right (OnSurfaceVariant)
- **FAB:** Primary amber, 16dp radius, bottom-end position, `+` icon in OnPrimary. Elevation: 6dp
- **Empty state:** Centered icon (muted amber), "No applications connected yet" text, subtitle with instructions
- **Bottom nav:** Surface background, `tonalElevation = 0.dp`. 4 items (same order as current): Applications, IncomingRequest, Settings, Accounts. Active: M3 indicator pill behind icon (PrimaryContainer) + Primary icon/label. Inactive: `Color(0xFF555555)`

### 2. Settings Screen

- **Top bar:** Same as Applications — `TopAppBar`, left-aligned title
- **Grouped sections** with uppercase section labels (`labelSmall`, OnSurfaceVariant)
- **Setting rows** grouped into continuous cards: first item top-radius 16dp, last item bottom-radius 16dp, OutlineVariant dividers between rows
- Each row:
  - Colored icon container (32dp, 8dp radius, icon-specific tinted background per section color)
  - Title (`titleMedium`) + optional subtitle showing current value (`bodySmall`, OnSurfaceVariant)
  - Right side: chevron for navigation, Material 3 `Switch` for booleans (amber thumb when checked)
- **Sections:** General (Language, Sign Policy), Security (Biometric Lock, PIN Lock), Network (Relays, Tor Proxy), Data (Backup, Export All, Logs), About (Feedback)

### 3. Permission Editor Screen

- **Top bar:** `TopAppBar` with amber-tinted back arrow as navigation icon, app name as title
- **App header card:** 52dp icon, app name (`titleLarge`), package name (`bodySmall`, OnSurfaceVariant), OutlineVariant divider, then row of two buttons: "View Logs" (outline) + "Revoke All" (danger — Error color border/text, Error bg)
- **Permission list:** Cards with:
  - Permission name (`titleMedium`)
  - Kind description (`bodySmall`, OnSurfaceVariant)
  - Status badge (20dp radius pill):
    - Auto-accept: Success text on Success bg
    - Always ask: Warning text on Warning bg
    - Auto-reject: Error text on Error bg
- Each card tappable to edit permission details

### 4. Signing / Approval Screens (IncomingRequest tab content)

All signing approval UIs render inside the IncomingRequest tab. These include: `BunkerSingleEventHomeScreen`, `BunkerMultiEventHomeScreen`, `IntentSingleEventHomeScreen`, `IntentMultiEventHomeScreen`, `BunkerConnectRequestScreen`, `BunkerGetPubKeyScreen`, `BunkerRelayAuthScreen`, `BunkerPingScreen`. They all follow the same visual pattern:

- **Layout:** Full-screen content within the tab (not a bottom sheet — the existing behavior is preserved)
- **App identity header:** Centered app icon (52dp), app name (`titleLarge`), request description (`bodySmall`, OnSurfaceVariant)
- **Event preview card:** SurfaceContainer background, kind badge in Primary (`labelMedium`), timestamp in OnSurfaceVariant, content preview in `bodyMedium`. For multi-event: scrollable list of event cards
- **Connect requests** (`BunkerConnectRequestScreen`): Show requested permissions list using the same permission card pattern as section 3
- **"Remember my choice"** — Material 3 `Checkbox` with Primary color + label in `bodyMedium`
- **Action buttons:** "Reject" (outline) + "Accept/Sign" (filled Primary). Full width, stacked or side-by-side depending on existing layout
- **Signing as indicator:** `bodySmall`, OnSurfaceVariant, showing current account npub

### 5. Login Screen

- **Centered layout**, 40dp top padding
- **Logo:** 80dp, 24dp radius, amber gradient (`Primary` to darker amber), app icon centered, `tonalElevation = 8.dp` (M3 tonal, not shadow)
- **Title:** "Amber" (24sp, W700), subtitle "Nostr Event Signer" (`bodyMedium`, OnSurfaceVariant)
- **Actions (vertical stack, 12dp gap):**
  - "Create New Account" — filled Primary button
  - "Import Private Key" — outline button
  - "or" divider (OutlineVariant lines + `bodySmall` text)
  - "Scan QR Code" — outline button with QR icon

### 6. Activities Screen (Route.Activities)

History/log of past signing events. Accessible from Settings > Logs or per-app Activity.

- **Search bar** same pattern as Applications
- **Filter chips row:** Horizontal scrollable `LazyRow`. Active: PrimaryContainer bg with Primary border and text. Inactive: SurfaceContainer bg with Outline border, OnSurfaceVariant text
- **Event list:** Cards with:
  - Mini app icon (24dp, 6dp radius) + app name (`titleMedium`) + timestamp (`bodySmall`)
  - Event description (`bodyMedium`, OnSurfaceVariant) + status badge (pill)
- **Per-app Activity screen** (`Route.Activity`): Same layout but filtered to single app, with app name in top bar title

### 7. Accounts Bottom Sheet

- Bottom sheet with Surface background, 24dp top corners
- Account list: cards with avatar/icon (44dp, round), display name (`titleMedium`), npub truncated (`bodySmall`), checkmark on active account
- "Add Account" row at bottom with `+` icon in Primary
- Active account has subtle PrimaryContainer highlight

### 8. Security Screen

- Same grouped-settings pattern as main Settings
- Rows: Biometric Lock (toggle), Biometric Frequency (chevron to dropdown), PIN Lock (toggle)
- PIN setup/confirm: `SetupPinScreen` with randomized grid, SurfaceContainer button backgrounds, Primary highlight on press, PIN dots displayed with Primary fill

### 9. Relay Screens (Active, Default, Profile, Config)

- **Relay list:** Cards with relay URL (`titleMedium`), connection status dot (Success green = connected, Error red = disconnected), read/write indicators as chips
- **Add relay:** SurfaceContainer input field with 16dp radius, Primary focus border, "Add" Primary button
- **Edit/Delete:** Swipe-to-delete or icon buttons in OnSurfaceVariant, delete icon in Error
- **RelayLogScreen:** Same card pattern, log entries with timestamps

### 10. Form Screens (EditProfile, NewApplication, NewBunker, NSecBunkerCreated, Feedback, EditConfiguration)

All form screens follow a consistent pattern:
- **Top bar:** `TopAppBar` with back arrow, screen title
- **Form fields:** `OutlinedTextField` with SurfaceContainer fill, Outline border, Primary border on focus, `titleMedium` labels above
- **Action button:** Full-width Primary filled button at bottom
- **Secondary actions:** Outline buttons
- **NSecBunkerCreated:** Confirmation screen after bunker creation — shows connection string in a SurfaceContainer card with copy button (Primary tint), QR code below

### 11. Detail/Inspection Screens (SeeDetails, ActivityDetail, RelayLog, LogsScreen)

- **Top bar** with back arrow, contextual title
- **Content card** with event JSON in monospace (`bodyMedium`), SurfaceContainer background
- **Raw JSON toggle:** `RawJsonButton` uses outline style
- **Copy button** with Primary tint
- **LogsScreen** (`Route.Logs`): List of operation log entries as cards, each with timestamp (`bodySmall`), operation type (`titleMedium`), and details (`bodyMedium`, OnSurfaceVariant)

### 12. Tor / Orbot Dialog (`ConnectOrbotDialog`)

This is a dialog (not a full screen) — keep as dialog:
- Surface background, 24dp radius
- Connection status with Success/Error dot
- "Connect" Primary button, "Configure" outline button
- Instructions text in `bodyMedium`, OnSurfaceVariant
- Dismiss via "Close" text button

### 13. Language Screen

- List of language options as setting rows in a grouped card
- Active language has Primary checkmark
- Search/filter at top

### 14. Sign Policy Screen (`SignPolicySettingsScreen`)

- Same grouped-settings card pattern
- Per-kind policy rows with kind name (`titleMedium`), current policy as dropdown/spinner
- Policy options styled as chips: Auto-accept (Success), Always ask (Warning), Auto-reject (Error)

### 15. QR Code Screen (`Route.QrCode`)

- **Top bar** with back arrow
- QR code centered in a SurfaceContainer card with 16dp padding
- Content string below QR in `bodySmall`, OnSurfaceVariant, truncated
- "Share" Primary filled button + "Copy" outline button below

### 16. Account Backup / Export Screens

- **AccountBackupScreen:** Warning card (AmberMuted bg, Primary border) explaining backup importance. Seed words displayed in a numbered grid (SurfaceContainer cells, `titleMedium`). "Copy" and "Share" buttons below
- **ExportAllAccountsScreen:** List of accounts as cards with checkboxes. "Export Selected" Primary button. Password input field for encryption

### 17. Biometric Auth Screen

- Dialog overlay with Surface background, 24dp radius
- Fingerprint icon centered (48dp, Primary tint)
- Title: `titleLarge` "Authentication Required"
- Subtitle: `bodyMedium`, OnSurfaceVariant
- "Cancel" text button
- Delegates to system BiometricPrompt — this just styles the pre-prompt

### 18. Dialogs (all)

Consistent dialog styling:
- Surface background, 24dp radius
- Title: `titleLarge`
- Body: `bodyMedium`, OnSurfaceVariant
- **Confirm dialogs** (LogoutDialog, RemoveAllPermissionsDialog): Error-styled "Confirm" button + "Cancel" text button
- **Info dialogs:** Primary "OK" button
- **Edit dialogs** (EditRelaysDialog): `OutlinedTextField` with Primary focus, "Save" Primary button + "Cancel" text button
- **ConnectOrbotDialog:** See section 12

### 19. Empty States

All list screens show centered empty state:
- Muted icon (OnSurfaceVariant, 48dp)
- Title: `titleMedium`, OnSurface
- Subtitle: `bodySmall`, OnSurfaceVariant
- Optional action button (Primary outline)

### 20. Loading States

- `CircularProgressIndicator` with Primary color, centered
- Skeleton loading not needed (keep existing approach)

## Component Changes

### AmberButton
- Background: Primary (`#D4AF55`)
- Text: OnPrimary (`#0F0F14`), `titleMedium` weight
- Shape: 12dp rounded corners
- Full width by default

### AmberElevatedButton / Outline variant
- Background: transparent
- Border: 1dp solid Primary
- Text: Primary, `titleMedium` weight
- Shape: 12dp rounded corners

### Danger Button
- Background: `Error.copy(alpha = 0.12f)`
- Border: 1dp solid `Error.copy(alpha = 0.2f)`
- Text: Error color
- Shape: 12dp rounded corners

### AmberTopAppBar
- Use `TopAppBar` (not CenterAligned) for left-aligned titles
- Background: Surface
- Title: `titleLarge` style, OnSurface color
- Relay chip in actions: AmberMuted pill bg, Success dot, relay count in Primary
- No scroll collapse behavior (pinned)

### AmberNavigationBar
- Background: Surface, `tonalElevation = 0.dp`
- Active item: M3 default indicator pill behind icon (PrimaryContainer), Primary-tinted icon and label
- Inactive icon/label: `Color(0xFF555555)`
- Uses standard `NavigationBar` + `NavigationBarItem` — no custom top border (rely on M3 tonal separation)

### Cards (all types)
- Background: SurfaceContainer
- Border: 1dp AmberSubtle
- Radius: 16dp
- Padding: 16dp internal
- Gap: 10dp between cards

### Status Badges
- Rounded pill, 20dp radius
- Accept: Success text on Success bg
- Ask: Warning text on Warning bg
- Reject: Error text on Error bg
- Font: `labelMedium`

### Grouped Settings Card
- First row: top corners 16dp
- Last row: bottom corners 16dp
- Middle rows: 0dp radius
- Dividers: 1dp OutlineVariant between rows
- Same SurfaceContainer background

## Implementation Strategy

All changes are in the theme layer and individual composables — no architectural changes needed:

1. Update `Theme.kt` — new `darkColorScheme()` and `lightColorScheme()` with all tokens above
2. Add color extension vals (AmberSubtle, AmberMuted, Success, Warning, Error) as top-level or theme extension properties
3. Update `Type.kt` — new typography scale per table above
4. Update `Shapes` — 16dp medium, 12dp small, 24dp large
5. Update each composable screen to use new tokens, spacing, and patterns
6. Test dark theme (primary) then light theme

## What Stays the Same

- All functionality preserved — nothing removed
- Navigation structure (same 4 tabs, same routes, same route names)
- Material 3 component library (no new dependencies)
- Data flow, state management, ViewModels
- All existing features accessible
