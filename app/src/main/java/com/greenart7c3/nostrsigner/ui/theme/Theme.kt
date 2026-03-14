package com.greenart7c3.nostrsigner.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import com.greenart7c3.nostrsigner.Amber
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.resolveDefaults

val Shapes =
    Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
    )

val Size35dp = 35.dp

val ButtonBorder = RoundedCornerShape(12.dp)
val Size20Modifier = Modifier.size(20.dp)

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

val RichTextDefaults = RichTextStyle().resolveDefaults()

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

@Suppress("DEPRECATION")
@Composable
fun NostrSignerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val typography = Typography

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = Shapes,
        content = content,
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if (darkTheme) {
                    window.statusBarColor = colors.background.toArgb()
                } else {
                    window.statusBarColor = colors.surface.toArgb()
                }
                window.navigationBarColor = colors.surface.toArgb()
            }
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

fun Color.light(factor: Float = 0.5f) = this.copy(alpha = this.alpha * factor)

fun Color.Companion.fromHex(colorString: String) = try {
    Color("#$colorString".toColorInt())
} catch (e: Exception) {
    Log.e(Amber.TAG, "Failed to parse color: $colorString", e)
    Unspecified
}
