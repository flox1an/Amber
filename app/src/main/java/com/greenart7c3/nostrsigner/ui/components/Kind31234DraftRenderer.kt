package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Kind31234DraftRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val targetKind = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "k" }?.get(1)?.toIntOrNull()
    }
    val title = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    }
    val draftId = remember(tags) {
        val d = tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1) ?: return@remember null
        if (d.length > 20) "${d.take(8)}...${d.takeLast(8)}" else d
    }
    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.split("\\s+".toRegex()).size
    }
    val preview = remember(content) {
        if (content.length <= 300) content else content.take(300) + "..."
    }
    val hashtags = remember(tags) {
        tags.filter { it.size >= 2 && it[0] == "t" }.map { it[1] }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DRAFT",
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
                // Target kind badge
                if (targetKind != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "Draft for kind $targetKind",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Title
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }

                // Draft ID
                if (draftId != null) {
                    Text(
                        text = "ID: $draftId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }

                // Word count
                if (wordCount > 0) {
                    Text(
                        text = "$wordCount words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }

                // Content preview
                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Hashtag pills
                if (hashtags.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        hashtags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(31234, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
