package com.greenart7c3.nostrsigner.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.greenart7c3.nostrsigner.Amber

@Composable
fun AppIcon(key: String, name: String, size: Dp = 40.dp) {
    // Try to load Android app icon using key as package name first,
    // then try name as package name (for bunker apps where key is hex but name might be a package)
    val icon: Drawable? = remember(key, name) {
        // Try key as package name
        val fromKey = runCatching {
            val appInfo = Amber.instance.packageManager.getApplicationInfo(key, 0)
            Amber.instance.packageManager.getApplicationIcon(appInfo)
        }.getOrNull()
        if (fromKey != null) return@remember fromKey

        // Try name as package name (some apps store package name in the name field)
        if (name.contains(".") && name != key) {
            runCatching {
                val appInfo = Amber.instance.packageManager.getApplicationInfo(name, 0)
                Amber.instance.packageManager.getApplicationIcon(appInfo)
            }.getOrNull()
        } else {
            null
        }
    }

    if (icon != null) {
        Image(
            bitmap = icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp)),
        )
        return
    }

    val displayName = name.ifBlank { key }
    val firstLetter = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = firstLetter,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
