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
