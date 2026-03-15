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

private val HEX_REGEX = Regex("^[0-9a-fA-F]{64}$")

private fun String.isHexKey() = HEX_REGEX.matches(this)

@Composable
fun AppIcon(key: String, name: String, size: Dp = 40.dp) {
    if (!key.isHexKey()) {
        val appInfo = remember(key) {
            runCatching {
                Amber.instance.packageManager.getApplicationInfo(key, 0)
            }.getOrNull()
        }
        val icon: Drawable? = remember(appInfo) {
            appInfo?.let {
                runCatching { Amber.instance.packageManager.getApplicationIcon(it) }.getOrNull()
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
