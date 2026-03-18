package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Kind1063FileRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val url = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "url" }?.get(1)
    }
    val mimeType = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "m" }?.get(1)
    }
    val hash = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "x" }?.get(1)
    }
    val filename = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "filename" }?.get(1)
    }
    val sizeRaw = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "size" }?.get(1)
    }
    val sizeLabel = remember(sizeRaw) {
        val bytes = sizeRaw?.toLongOrNull() ?: return@remember null
        when {
            bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
    val hashShort = remember(hash) {
        if (hash == null || hash.length < 16) {
            hash
        } else {
            "${hash.take(8)}...${hash.takeLast(8)}"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "FILE METADATA",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Filename
                if (filename != null) {
                    Text(
                        text = filename,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // URL
                if (url != null) {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }

                // MIME type
                if (mimeType != null) {
                    MetaRow(label = "Type", value = mimeType)
                }

                // Size
                if (sizeLabel != null) {
                    MetaRow(label = "Size", value = sizeLabel)
                }

                // Hash (truncated)
                if (hashShort != null) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Hash",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = hashShort,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Description from content
                if (content.isNotBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(1063, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp),
    )
}
