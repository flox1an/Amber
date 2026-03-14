# Amber UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the entire Amber app UI to a dark & premium aesthetic with refined amber (#D4AF55) accents, following Material 3 guidelines.

**Architecture:** Theme-first approach — update design tokens (colors, typography, shapes) in the theme layer so changes cascade to all Material 3 components automatically. Then update individual screens/components that use hardcoded values. No architectural changes needed.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, existing dependencies only.

**Spec:** `docs/superpowers/specs/2026-03-14-ui-redesign-design.md`

**Build/Test Commands:**
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export ANDROID_HOME="/opt/homebrew/share/android-commandlinetools"
./gradlew assembleDebug          # build
./gradlew test --no-daemon       # unit tests
./gradlew ktlintCheck            # lint
./gradlew ktlintFormat           # auto-fix lint
```

---

## Chunk 1: Theme Foundation

### Task 1: Update Color Tokens in Theme.kt

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/Theme.kt`

- [ ] **Step 1: Update color definitions and dark color scheme**

Replace the existing color vals and `DarkColorPalette` with the new design tokens. Keep `LightColorPalette` for step 2.

```kotlin
// New primary colors
val primaryColor = Color(0xFFD4AF55)
val primaryVariant = Color(0xFFB8922E)
val secondaryColor = Color(0xFFD4AF55)

// Semantic colors
val AmberSubtleDark = Color(0xFFD4AF55).copy(alpha = 0.08f)
val AmberMutedDark = Color(0xFFD4AF55).copy(alpha = 0.12f)
val AmberSubtleLight = Color(0xFFB8922E).copy(alpha = 0.06f)
val AmberMutedLight = Color(0xFFB8922E).copy(alpha = 0.10f)
val SuccessColor = Color(0xFF66BB6A)
val SuccessBgDark = Color(0xFF4CAF50).copy(alpha = 0.15f)
val SuccessColorLight = Color(0xFF2E7D32)
val SuccessBgLight = Color(0xFF2E7D32).copy(alpha = 0.10f)
val WarningColor = Color(0xFFFFB74D)
val WarningBgDark = Color(0xFFFFB74D).copy(alpha = 0.15f)
val WarningColorLight = Color(0xFFE65100)
val WarningBgLight = Color(0xFFE65100).copy(alpha = 0.10f)
val ErrorColor = Color(0xFFEF5350)
val ErrorBgDark = Color(0xFFEF5350).copy(alpha = 0.15f)
val ErrorColorLight = Color(0xFFC62828)
val ErrorBgLight = Color(0xFFC62828).copy(alpha = 0.10f)

private val DarkColorPalette =
    darkColorScheme(
        primary = primaryColor,
        onPrimary = Color(0xFF0F0F14),
        secondary = primaryVariant,
        tertiary = secondaryColor,
        primaryContainer = Color(0xFFD4AF55).copy(alpha = 0.15f),
        secondaryContainer = Color(0xFFD4AF55).copy(alpha = 0.15f),
        background = Color(0xFF0F0F14),
        surface = Color(0xFF16161D),
        surfaceContainer = Color(0xFF1A1A24),
        surfaceContainerHigh = Color(0xFF1A1A24),
        surfaceContainerHighest = Color(0xFF1A1A24),
        onSurface = Color(0xFFE8E4E0),
        onSurfaceVariant = Color(0xFF888888),
        outline = Color.White.copy(alpha = 0.06f),
        outlineVariant = Color.White.copy(alpha = 0.04f),
        error = ErrorColor,
    )
```

- [ ] **Step 2: Update light color scheme**

```kotlin
private val LightColorPalette =
    lightColorScheme(
        primary = Color(0xFFB8922E),
        onPrimary = Color.White,
        secondary = Color(0xFFB8922E),
        tertiary = Color(0xFFB8922E),
        primaryContainer = Color(0xFFB8922E).copy(alpha = 0.10f),
        secondaryContainer = Color(0xFFB8922E).copy(alpha = 0.10f),
        background = Color(0xFFFFFBF5),
        surface = Color(0xFFF5EFE6),
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color.White,
        onSurface = Color(0xFF1C1B1F),
        onSurfaceVariant = Color(0xFF666666),
        outline = Color.Black.copy(alpha = 0.08f),
        outlineVariant = Color.Black.copy(alpha = 0.04f),
        error = ErrorColorLight,
    )
```

- [ ] **Step 3: Update Shapes**

```kotlin
val Shapes =
    Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
    )

val ButtonBorder = RoundedCornerShape(12.dp)
```

- [ ] **Step 4: Remove the `orange` color val**

Remove `val orange = Color(0xFFFF6B00)` — search for usages first and replace with `primaryVariant` or `MaterialTheme.colorScheme.secondary`.

- [ ] **Step 5: Run build to verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (may have warnings about unused orange references — fix any compile errors)

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/Theme.kt
git commit -m "feat: update theme color palette, shapes for premium dark redesign"
```

### Task 2: Update Typography in Type.kt

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/Type.kt`

- [ ] **Step 1: Replace typography definitions**

Replace entire file content (after package/imports) with:

```kotlin
val Typography =
    Typography(
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            letterSpacing = (-0.3).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            letterSpacing = (-0.2).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.3.sp,
        ),
    )
```

- [ ] **Step 2: Remove the separate TypographyDark val**

The new typography has no hardcoded colors (colors come from the color scheme), so a single `Typography` val works for both themes. Update `Theme.kt` to use `Typography` for both branches instead of switching on `darkTheme`:

In `NostrSignerTheme`, change:
```kotlin
val typography = if (darkTheme) TypographyDark else Typography
```
to:
```kotlin
val typography = Typography
```

- [ ] **Step 3: Run build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/Type.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/Theme.kt
git commit -m "feat: update typography scale with new sizes and weights"
```

### Task 3: Add Theme Extension Properties

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/AmberTheme.kt`

- [ ] **Step 1: Create AmberTheme.kt with extension properties**

This provides convenient access to semantic colors that aren't in the standard M3 color scheme:

```kotlin
package com.greenart7c3.nostrsigner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AmberColors {
    @Composable
    fun amberSubtle(): Color = if (isSystemInDarkTheme()) AmberSubtleDark else AmberSubtleLight

    @Composable
    fun amberMuted(): Color = if (isSystemInDarkTheme()) AmberMutedDark else AmberMutedLight

    @Composable
    fun success(): Color = if (isSystemInDarkTheme()) SuccessColor else SuccessColorLight

    @Composable
    fun successBg(): Color = if (isSystemInDarkTheme()) SuccessBgDark else SuccessBgLight

    @Composable
    fun warning(): Color = if (isSystemInDarkTheme()) WarningColor else WarningColorLight

    @Composable
    fun warningBg(): Color = if (isSystemInDarkTheme()) WarningBgDark else WarningBgLight

    @Composable
    fun error(): Color = if (isSystemInDarkTheme()) ErrorColor else ErrorColorLight

    @Composable
    fun errorBg(): Color = if (isSystemInDarkTheme()) ErrorBgDark else ErrorBgLight
}
```

- [ ] **Step 2: Run build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/theme/AmberTheme.kt
git commit -m "feat: add AmberColors helper for semantic theme colors"
```

---

## Chunk 2: Core Components

### Task 4: Update AmberButton and AmberElevatedButton

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberButton.kt`

- [ ] **Step 1: Update AmberButton**

Change shape from `RoundedCornerShape(20)` to `RoundedCornerShape(12.dp)`, update default colors to use theme:

```kotlin
@Composable
fun AmberButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors().copy(
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ),
    textColor: Color = Color.Unspecified,
    text: String,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            onClick = onClick,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    LocalDensity.current.density,
                    1f,
                ),
            ) {
                Text(
                    text = text,
                    color = textColor,
                    textAlign = textAlign,
                    maxLines = maxLines,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
```

- [ ] **Step 2: Update AmberElevatedButton**

Same shape change to `RoundedCornerShape(12.dp)` and `FontWeight.SemiBold`.

- [ ] **Step 3: Add AmberDangerButton composable**

Add a new composable to the same file for danger/destructive actions (Revoke All, Delete, etc.):

```kotlin
@Composable
fun AmberDangerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberColors.errorBg(),
                contentColor = AmberColors.error(),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            border = BorderStroke(1.dp, AmberColors.error().copy(alpha = 0.2f)),
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
```

Add required imports: `import androidx.compose.foundation.BorderStroke`, `import com.greenart7c3.nostrsigner.ui.theme.AmberColors`, `import androidx.compose.material3.MaterialTheme`.

- [ ] **Step 4: Run build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberButton.kt
git commit -m "feat: update button shapes and colors for redesign"
```

### Task 5: Update AmberTopAppBar

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberTopAppBar.kt`

- [ ] **Step 1: Replace CenterAlignedTopAppBar with TopAppBar**

Change the import and usage from `CenterAlignedTopAppBar` to `TopAppBar` for left-aligned titles.

- [ ] **Step 2: Style the relay status chip**

Wrap the relay count in a Surface with `AmberColors.amberMuted()` background and pill shape:

```kotlin
Surface(
    shape = RoundedCornerShape(20.dp),
    color = AmberColors.amberMuted(),
) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(AmberColors.success(), CircleShape),
        )
        Text(
            "${relayStats.value.second.size}/${relayStats.value.first.size} relays",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
```

- [ ] **Step 3: Update proxy icon colors**

Replace `Color.Green` and `Color.Red` with `AmberColors.success()` and `AmberColors.error()`.

- [ ] **Step 4: Run build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberTopAppBar.kt
git commit -m "feat: update top app bar to left-aligned with styled relay chip"
```

### Task 6: Update AmberNavigationBar

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberNavigationBar.kt`

- [ ] **Step 1: Update icon tint colors**

Replace the hardcoded `Color.Black` / `Color.White` icon tints with theme-aware colors:

```kotlin
tint = if (selected) {
    MaterialTheme.colorScheme.primary
} else {
    Color(0xFF555555)
},
```

- [ ] **Step 2: Set tonalElevation to 0**

The existing code already has `tonalElevation = 0.dp` — verify this is present.

- [ ] **Step 3: Remove excessive horizontal padding**

Change `Modifier.padding(horizontal = 40.dp)` to `Modifier.padding(horizontal = 16.dp)` for better spacing.

- [ ] **Step 4: Run build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberNavigationBar.kt
git commit -m "feat: update navigation bar colors and spacing"
```

### Task 7: Update AmberWarningCard

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberWarningCard.kt`

- [ ] **Step 1: Update card styling**

Replace `MaterialTheme.colorScheme.primary` background with `AmberColors.amberMuted()`, update text color from `Color.Black` to `MaterialTheme.colorScheme.onSurface`, update shape to `RoundedCornerShape(16.dp)`.

- [ ] **Step 2: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberWarningCard.kt
git commit -m "feat: update warning card to new design tokens"
```

### Task 8: Update SimpleSearchBar

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SimpleSearchBar.kt`

- [ ] **Step 1: Update search bar styling**

Update shape to `RoundedCornerShape(28.dp)`, use `MaterialTheme.colorScheme.surfaceContainer` for background, `MaterialTheme.colorScheme.outline` for border.

- [ ] **Step 2: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SimpleSearchBar.kt
git commit -m "feat: update search bar with rounded pill shape"
```

### Task 9: Update PermissionCard and BunkerPermissionCard

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/PermissionCard.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerPermissionCard.kt`

- [ ] **Step 1: Update PermissionCard**

Change `RoundedCornerShape(4.dp)` to `RoundedCornerShape(16.dp)`, update padding to 16dp, add `AmberSubtle` border via `Modifier.border(1.dp, AmberColors.amberSubtle(), RoundedCornerShape(16.dp))`.

- [ ] **Step 2: Update BunkerPermissionCard**

Same shape and border updates.

- [ ] **Step 3: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/PermissionCard.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerPermissionCard.kt
git commit -m "feat: update permission cards to 16dp radius with amber border"
```

### Task 10: Update AcceptRejectButtons

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AcceptRejectButtons.kt`

- [ ] **Step 1: Update button shapes to 12dp radius**

- [ ] **Step 2: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AcceptRejectButtons.kt
git commit -m "feat: update accept/reject buttons to new shape"
```

### Task 11: Update event data and modal components

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ActiveMarker.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/TrustScoreBadge.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EventData.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EventDetailModal.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EncryptDecryptData.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EncryptDecryptDetailModal.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EncryptedTagArraySection.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RawJson.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RawJsonButton.kt`

- [ ] **Step 1: Update ActiveMarker.kt** — shape to 16dp
- [ ] **Step 2: Update TrustScoreBadge.kt** — shape to 20dp (pill badge), use `AmberColors.success()`/`warning()`/`error()` for color-coded scoring
- [ ] **Step 3: Update EventData.kt** — shape to 16dp, remove all `Color.Black` hardcoding, use `MaterialTheme.colorScheme.onSurface`
- [ ] **Step 4: Update EventDetailModal.kt** — shape to 16dp, `surfaceContainer` background
- [ ] **Step 5: Update EncryptDecryptData.kt** — shape to 16dp, update TextFieldDefaults to use `surfaceContainer` fill and `outline` border
- [ ] **Step 6: Update EncryptDecryptDetailModal.kt** — shape to 16dp
- [ ] **Step 7: Update EncryptedTagArraySection.kt** — 16dp card shape, theme colors
- [ ] **Step 8: Update RawJsonButton.kt** — style as outline button: transparent bg, Primary border/text, 12dp radius
- [ ] **Step 9: Update RawJson.kt** — use `bodyMedium` for JSON text, `surfaceContainer` background
- [ ] **Step 10: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ActiveMarker.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/TrustScoreBadge.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EventData.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EventDetailModal.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EncryptDecryptData.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EncryptDecryptDetailModal.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EncryptedTagArraySection.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RawJson.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RawJsonButton.kt
git commit -m "feat: update event data and modal components"
```

### Task 12A: Update input and utility components

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RandomPinInput.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SignMessage.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/TextSpinner.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RememberMyChoice.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/MultiEventHomeScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/CloseButton.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/CloseIcon.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/LocalAppIcon.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ToogleOption.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SignerConnectAppTab.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/CenterCircularProgressIndicator.kt`

- [ ] **Step 1: Update RandomPinInput.kt** — button shape to 12dp, `surfaceContainer` for button backgrounds, Primary highlight on press, PIN dots use Primary fill
- [ ] **Step 2: Update SignMessage.kt** — shape to 16dp, remove hardcoded colors, update TextField styling
- [ ] **Step 3: Update TextSpinner.kt** — remove hardcoded colors, use theme tokens
- [ ] **Step 4: Update RememberMyChoice.kt** — `Checkbox` colors to use Primary for checked state
- [ ] **Step 5: Update MultiEventHomeScreen.kt** — theme colors
- [ ] **Step 6: Update CloseButton.kt, CloseIcon.kt** — use `onSurfaceVariant` for icon tint
- [ ] **Step 7: Update LocalAppIcon.kt** — ensure 44dp size, 12dp radius for icons in lists
- [ ] **Step 8: Update ToogleOption.kt, SignerConnectAppTab.kt** — theme colors
- [ ] **Step 9: Update CenterCircularProgressIndicator.kt** — use `MaterialTheme.colorScheme.primary` for indicator color
- [ ] **Step 10: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RandomPinInput.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SignMessage.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/TextSpinner.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/RememberMyChoice.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/MultiEventHomeScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/CloseButton.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/CloseIcon.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/LocalAppIcon.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ToogleOption.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SignerConnectAppTab.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/CenterCircularProgressIndicator.kt
git commit -m "feat: update input and utility components"
```

---

## Chunk 3: Main Screens

### Task 12: Update LoginScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/LoginScreen.kt`

This is the largest screen (~1301 lines) with heavy hardcoded colors.

- [ ] **Step 1: Redesign the initial login landing page**

The login landing should have:
- Centered layout with 40dp top padding
- Logo: 80dp `Surface` with 24dp radius, amber gradient background (`Brush.linearGradient(listOf(primaryColor, primaryVariant))`), app icon centered, `tonalElevation = 8.dp`
- Title "Amber" at 24sp, W700
- Subtitle "Nostr Event Signer" in `bodyMedium`, OnSurfaceVariant
- "Create New Account" — full-width Primary filled `AmberButton`
- "Import Private Key" — full-width `AmberElevatedButton` with outline
- Divider with "or" text (OutlineVariant lines + `bodySmall` centered)
- "Scan QR Code" — outline button with QR icon

- [ ] **Step 2: Search and replace all hardcoded colors throughout the file**

Find all `Color.Black`, `Color.White`, `Color(0xFF...)` hex literals. Replace with theme-aware alternatives:
- `Color.Black` text → remove or `MaterialTheme.colorScheme.onSurface`
- `Color.White` text → remove or `MaterialTheme.colorScheme.onSurface`
- Accent colors → `MaterialTheme.colorScheme.primary`

- [ ] **Step 3: Update shapes**

Replace `RoundedCornerShape(20.dp)` → `RoundedCornerShape(12.dp)` for buttons, `RoundedCornerShape(16.dp)` for cards/containers.

- [ ] **Step 4: Update TextField styling**

Use `MaterialTheme.colorScheme.surfaceContainer` for container color, `MaterialTheme.colorScheme.outline` for unfocused border, `MaterialTheme.colorScheme.primary` for focused border.

- [ ] **Step 5: Update spacing**

Consistent 16dp horizontal padding, 12dp gaps between action buttons, 10dp card gaps.

- [ ] **Step 6: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/LoginScreen.kt
git commit -m "feat: redesign login screen with logo, new theme"
```

### Task 13: Update SettingsScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/SettingsScreen.kt`

- [ ] **Step 1: Implement grouped settings card pattern**

Replace flat list with grouped sections. Each section gets an uppercase label (`labelSmall`) and rows grouped in a continuous card (first row top-radius 16dp, last row bottom-radius 16dp).

- [ ] **Step 2: Add colored icon containers**

Each setting row gets a 32dp icon container with 8dp radius and section-specific tinted background.

- [ ] **Step 3: Update toggle switches**

Ensure Material 3 `Switch` with Primary thumb when checked.

- [ ] **Step 4: Remove hardcoded colors**

Replace any `Color.Black`/`Color.White` with theme tokens.

- [ ] **Step 5: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/SettingsScreen.kt
git commit -m "feat: redesign settings with grouped sections and colored icons"
```

### Task 14: Update ApplicationsScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/ApplicationsScreen.kt`

- [ ] **Step 1: Update app cards**

Apply 16dp radius cards with `AmberSubtle` border, 44dp app icons with 12dp radius, `titleMedium` for app names, `bodySmall` for subtitles.

- [ ] **Step 2: Add chevron indicators**

Add trailing chevron icon (`Icons.AutoMirrored.Filled.KeyboardArrowRight`) in OnSurfaceVariant.

- [ ] **Step 3: Remove hardcoded colors**

- [ ] **Step 4: Add empty state**

When the app list is empty, show a centered empty state:
- Muted icon (48dp, OnSurfaceVariant) — e.g., `Icons.Outlined.Apps` or the applications drawable
- Title in `titleMedium`: "No applications connected yet"
- Subtitle in `bodySmall`, OnSurfaceVariant: "Apps that request signing will appear here"

- [ ] **Step 5: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/ApplicationsScreen.kt
git commit -m "feat: redesign applications list with new card style and empty state"
```

> **Note on empty states:** All other list screens (ActivitiesScreen, RelaysScreen, permission lists) should also add empty states following the same pattern: muted 48dp icon in OnSurfaceVariant, `titleMedium` message, `bodySmall` subtitle. Apply this when updating each respective screen in Tasks 21, 28.

### Task 15: Update EditPermission

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/EditPermission.kt`

- [ ] **Step 1: Add app header card**

52dp icon, app name in `titleLarge`, package name in `bodySmall`, divider, action buttons row.

- [ ] **Step 2: Add status badges to permissions**

Color-coded pills: Success for accept, Warning for ask, Error for reject. Use `AmberColors` helpers.

- [ ] **Step 3: Update shapes and spacing**

16dp card radius, 16dp padding, 10dp gaps.

- [ ] **Step 4: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/EditPermission.kt
git commit -m "feat: redesign permission editor with status badges"
```

### Task 16: Update MainScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/MainScreen.kt`

- [ ] **Step 1: Update Scaffold theming**

Ensure `containerColor` uses `MaterialTheme.colorScheme.background`.

- [ ] **Step 2: Update any hardcoded RoundedCornerShape values**

Change to new radii (16dp for cards, 24dp for sheets).

- [ ] **Step 3: Remove hardcoded colors**

- [ ] **Step 4: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/MainScreen.kt
git commit -m "feat: update main screen scaffold theming"
```

---

## Chunk 4: Signing/Approval Screens

### Task 17: Update BunkerSingleEventHomeScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerSingleEventHomeScreen.kt`

- [ ] **Step 1: Update event preview card**

16dp radius, `surfaceContainer` background, `AmberSubtle` border. Kind badge in Primary (`labelMedium`).

- [ ] **Step 2: Remove all hardcoded Color.Black/Color.White**

Replace with theme-aware colors.

- [ ] **Step 3: Update button shapes**

12dp radius for action buttons.

- [ ] **Step 4: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerSingleEventHomeScreen.kt
git commit -m "feat: redesign bunker single event screen"
```

### Task 18: Update BunkerMultiEventHomeScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerMultiEventHomeScreen.kt`

- [ ] **Step 1: Same pattern as Task 17** — update cards, remove hardcoded colors, update shapes.

- [ ] **Step 2: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerMultiEventHomeScreen.kt
git commit -m "feat: redesign bunker multi event screen"
```

### Task 19: Update IntentSingleEventHomeScreen and IntentMultiEventHomeScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/IntentSingleEventHomeScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/IntentMultiEventHomeScreen.kt`

- [ ] **Step 1: Update IntentSingleEventHomeScreen** — same pattern: 16dp cards, theme colors, 12dp buttons.
- [ ] **Step 2: Update IntentMultiEventHomeScreen** — same pattern.
- [ ] **Step 3: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/IntentSingleEventHomeScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/IntentMultiEventHomeScreen.kt
git commit -m "feat: redesign intent event screens"
```

### Task 20: Update BunkerConnectRequestScreen and remaining bunker screens

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerConnectRequestScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerGetPubKeyScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerRelayAuthScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerPingScreen.kt`

- [ ] **Step 1: Update each file** — theme colors, 16dp card shapes, consistent spacing.
- [ ] **Step 2: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerConnectRequestScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerGetPubKeyScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerRelayAuthScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BunkerPingScreen.kt
git commit -m "feat: redesign bunker connect/auth screens"
```

---

## Chunk 5: Secondary Screens and Dialogs

### Task 21: Update ActivitiesScreen and ActivityScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/ActivitiesScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/ActivityScreen.kt`

- [ ] **Step 1: Update card shapes and colors**
- [ ] **Step 2: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/ActivitiesScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/ActivityScreen.kt
git commit -m "feat: redesign activity screens"
```

### Task 22: Update AccountBackupScreen and ExportAllAccountsScreen

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/AccountBackupScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/ExportAllAccountsScreen.kt`

- [ ] **Step 1: Update shapes (16dp cards, 12dp buttons)**
- [ ] **Step 2: Remove hardcoded colors**
- [ ] **Step 3: Update TextField styling**
- [ ] **Step 4: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/AccountBackupScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/ExportAllAccountsScreen.kt
git commit -m "feat: redesign backup and export screens"
```

### Task 23: Update AccountsBottomSheet

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/AccountsBottomSheet.kt`

- [ ] **Step 1: Update bottom sheet styling** — Surface background, 24dp top corners.
- [ ] **Step 2: Style account rows** — 44dp avatars, `titleMedium` names, Primary checkmark on active. Active account row gets `PrimaryContainer` background highlight.
- [ ] **Step 3: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/AccountsBottomSheet.kt
git commit -m "feat: redesign accounts bottom sheet"
```

### Task 24: Update EditRelaysDialog

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/EditRelaysDialog.kt`

- [ ] **Step 1: Update dialog shape** — 24dp radius
- [ ] **Step 2: Update TextField and button styling**
- [ ] **Step 3: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/EditRelaysDialog.kt
git commit -m "feat: redesign relay editor dialog"
```

### Task 25: Update dialogs

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/ConnectOrbotDialog.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/LogoutDialog.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/LogoutButton.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/RemoveAllPermissionsDialog.kt`

All dialogs: Surface background, 24dp radius shape, `titleLarge` for title, `bodyMedium` OnSurfaceVariant for body.

- [ ] **Step 1: Update ConnectOrbotDialog** — 24dp radius, `AmberColors.success()`/`error()` for connection status dot, Primary "Connect" button, outline "Configure" button, `bodyMedium` instructions text
- [ ] **Step 2: Update LogoutDialog** — 24dp radius, Error-styled "Confirm" button (use `AmberDangerButton` pattern), "Cancel" text button
- [ ] **Step 3: Update LogoutButton** — theme-aware colors
- [ ] **Step 4: Update RemoveAllPermissionsDialog** — same pattern as LogoutDialog
- [ ] **Step 5: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/actions/
git commit -m "feat: redesign dialogs with new theme"
```

### Task 26: Update Security and PIN screens

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/SecurityScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/SetupPinScreen.kt`

- [ ] **Step 1: Update SecurityScreen** — grouped-settings card pattern (same as SettingsScreen Task 13). Rows: Biometric Lock (toggle with Primary thumb), Biometric Frequency (chevron), PIN Lock (toggle). Section label in `labelSmall`, uppercase
- [ ] **Step 2: Update SetupPinScreen** — depends on RandomPinInput.kt (updated in Task 11). Ensure `surfaceContainer` backgrounds, Primary highlight, PIN dots with Primary fill
- [ ] **Step 3: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/SecurityScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/SetupPinScreen.kt
git commit -m "feat: redesign security and PIN screens"
```

### Task 27: Update SignPolicy and Language screens

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/SignPolicySettingsScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/LanguageScreen.kt`

- [ ] **Step 1: Update SignPolicySettingsScreen** — per-kind policy rows in grouped card. Policy options styled as colored chips: Auto-accept in `AmberColors.success()` on `successBg()`, Always ask in `AmberColors.warning()` on `warningBg()`, Auto-reject in `AmberColors.error()` on `errorBg()`. 20dp pill radius for chips
- [ ] **Step 2: Update LanguageScreen** — grouped-settings card with language rows, active language gets Primary checkmark icon, search field at top with 28dp radius
- [ ] **Step 3: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/SignPolicySettingsScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/LanguageScreen.kt
git commit -m "feat: redesign sign policy and language screens"
```

### Task 28: Update Relay screens

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/RelaysScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/DefaultProfileRelaysScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/EditConfigurationScreen.kt`

- [ ] **Step 1: Update RelaysScreen** — relay cards with 16dp radius, relay URL in `titleMedium`, connection status dot (`AmberColors.success()` green = connected, `AmberColors.error()` red = disconnected), read/write indicator chips (20dp radius), add-relay `OutlinedTextField` with `surfaceContainer` fill and Primary focus border, "Add" Primary button
- [ ] **Step 2: Update DefaultProfileRelaysScreen** — same card pattern as RelaysScreen
- [ ] **Step 3: Update EditConfigurationScreen** — 16dp card shapes, form fields with `surfaceContainer` fill, 12dp button shapes
- [ ] **Step 4: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/RelaysScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/DefaultProfileRelaysScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/EditConfigurationScreen.kt
git commit -m "feat: redesign relay management screens"
```

### Task 29: Update form and detail screens

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/NewApplicationScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/NewNsecBunkerScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/EditProfileScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/FeedbackScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/SeeDetailsScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/LogsScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/QrCodeDrawer.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/AccountScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/IncomingRequestScreen.kt`

All form screens: `OutlinedTextField` with `surfaceContainer` fill, `outline` border, Primary focus border, `titleMedium` labels. Full-width Primary `AmberButton` at bottom.

- [ ] **Step 1: Update form screens** (NewApplication, NewNsecBunker, EditProfile, Feedback) — `RoundedCornerShape(16.dp)` for cards, remove hardcoded colors, update TextFields. NsecBunkerCreated page: show connection string in `surfaceContainer` card with copy button (Primary tint), QR code below
- [ ] **Step 2: Update detail screens** (SeeDetails, LogsScreen) — `surfaceContainer` card for JSON content, `bodyMedium` monospace, `RawJsonButton` as outline style. LogsScreen: log entries as 16dp cards with timestamp in `bodySmall` and operation in `titleMedium`
- [ ] **Step 3: Update QrCodeDrawer** — QR in `surfaceContainer` card with 16dp padding, "Share" Primary button + "Copy" outline button
- [ ] **Step 4: Update AccountScreen, IncomingRequestScreen** — theme colors, 16dp shapes
- [ ] **Step 5: Run build and commit**

```bash
./gradlew assembleDebug
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/NewApplicationScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/NewNsecBunkerScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/EditProfileScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/FeedbackScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/SeeDetailsScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/LogsScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/QrCodeDrawer.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/AccountScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/IncomingRequestScreen.kt
git commit -m "feat: redesign form and detail screens"
```

---

## Chunk 6: Remaining Components and Cleanup

### Task 30: Update remaining component files

**Files:**
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberBottomBar.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberFloatingButton.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberToggles.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BackButtonBottomBar.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BiometricAuthScreen.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ChooseSignPolicy.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EnabledPermissions.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EventSection.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/IconRow.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/LoginWithPubKey.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/NewBunkerFloatingButton.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SeedWordsPage.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SigningAs.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/TagsSection.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/QrCodeScanner.kt`

- [ ] **Step 1: Update FAB components** (AmberFloatingButton, NewBunkerFloatingButton) — 16dp radius, Primary bg, OnPrimary icon, `elevation = 6.dp`
- [ ] **Step 2: Update BiometricAuthScreen** — dialog overlay Surface background, 24dp radius, 48dp fingerprint icon centered with Primary tint, `titleLarge` "Authentication Required", `bodyMedium` subtitle in OnSurfaceVariant, "Cancel" text button
- [ ] **Step 3: Update LoginWithPubKey** — `RoundedCornerShape(16.dp)` for cards, TextField with `surfaceContainer` fill, remove `Color.Black`/`Color.White`, 12dp button shapes
- [ ] **Step 4: Update SeedWordsPage** — seed word cells use `surfaceContainer` background, 16dp card padding, `titleMedium` for words, numbered grid layout
- [ ] **Step 5: Update SigningAs** — `bodySmall` OnSurfaceVariant for npub display, 16dp card shape
- [ ] **Step 6: Update layout components** (AmberBottomBar, BackButtonBottomBar, IconRow, EventSection, TagsSection) — theme colors, remove hardcoded values
- [ ] **Step 7: Update ChooseSignPolicy** — RadioButton/dropdown colors to Primary
- [ ] **Step 8: Update EnabledPermissions** — 16dp card shapes, theme colors
- [ ] **Step 9: Update AmberToggles** — Primary thumb when checked
- [ ] **Step 10: Update QrCodeScanner** — theme colors if any
- [ ] **Step 11: Run build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 12: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberBottomBar.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberFloatingButton.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/AmberToggles.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BackButtonBottomBar.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/BiometricAuthScreen.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ChooseSignPolicy.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EnabledPermissions.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/EventSection.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/IconRow.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/LoginWithPubKey.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/NewBunkerFloatingButton.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SeedWordsPage.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/SigningAs.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/TagsSection.kt
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/QrCodeScanner.kt
git commit -m "feat: redesign remaining components"
```

### Task 31: Run lint and fix issues

- [ ] **Step 1: Run ktlint check**

Run: `./gradlew ktlintCheck`

- [ ] **Step 2: Auto-fix lint issues**

Run: `./gradlew ktlintFormat`

- [ ] **Step 3: Fix any remaining manual lint issues**

- [ ] **Step 4: Run full test suite**

Run: `./gradlew test --no-daemon`
Expected: All tests pass

- [ ] **Step 5: Run full build for both flavors**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL for both free and offline

- [ ] **Step 6: Commit lint fixes**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/
git commit -m "chore: fix lint issues from redesign"
```

### Task 32: Visual verification

- [ ] **Step 1: Install on emulator**

```bash
adb install -r app/build/outputs/apk/free/debug/app-free-universal-debug.apk
adb shell am start -n com.greenart7c3.nostrsigner.debug/com.greenart7c3.nostrsigner.MainActivity
```

- [ ] **Step 2: Verify Login screen** — logo, buttons, "or" divider, overall dark premium feel

- [ ] **Step 3: Verify Applications screen** — card styling, search bar, FAB, nav bar

- [ ] **Step 4: Verify Settings screen** — grouped sections, colored icons, toggles

- [ ] **Step 5: Verify Accounts bottom sheet** — avatar, active highlight, add account

- [ ] **Step 6: Verify Security/PIN screens** — toggle rows, PIN input styling

- [ ] **Step 7: Verify Relay screens** — status dots, add relay, relay cards

- [ ] **Step 8: Test dark theme (should be default)**

- [ ] **Step 9: Test light theme** (toggle system theme in emulator settings)

- [ ] **Step 10: Fix any visual issues found**

- [ ] **Step 11: Final commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/
git commit -m "fix: visual polish from manual verification"
```
