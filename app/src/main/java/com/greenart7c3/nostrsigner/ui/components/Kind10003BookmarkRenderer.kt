package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Kind 10003 — Bookmark List (NIP-51)
 *
 * Shows total bookmark count with a breakdown by type (notes, articles, hashtags),
 * then lists the first 5 entries.
 */
@Composable
fun Kind10003BookmarkRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val eTags = remember(tags) { tags.filter { it.isNotEmpty() && it[0] == "e" } }
    val aTags = remember(tags) { tags.filter { it.isNotEmpty() && it[0] == "a" } }
    val tTags = remember(tags) { tags.filter { it.isNotEmpty() && it[0] == "t" } }

    val totalCount = eTags.size + aTags.size + tTags.size

    val breakdownParts = remember(eTags, aTags, tTags) {
        buildList {
            if (eTags.isNotEmpty()) add("${eTags.size} ${if (eTags.size == 1) "note" else "notes"}")
            if (aTags.isNotEmpty()) add("${aTags.size} ${if (aTags.size == 1) "article" else "articles"}")
            if (tTags.isNotEmpty()) add("${tTags.size} ${if (tTags.size == 1) "hashtag" else "hashtags"}")
        }.joinToString(", ")
    }

    // Collect first 5 entries across all types in tag order
    val entriesToShow = remember(tags) {
        tags.filter { it.isNotEmpty() && (it[0] == "e" || it[0] == "a" || it[0] == "t") }
            .take(5)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "BOOKMARKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(8.dp))

                // Total count row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(72.dp),
                    )
                    Text(
                        text = "$totalCount ${if (totalCount == 1) "bookmark" else "bookmarks"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Breakdown
                if (breakdownParts.isNotEmpty() && totalCount > 0) {
                    Spacer(modifier = Modifier.size(2.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.width(72.dp))
                        Text(
                            text = breakdownParts,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // First 5 entries
                if (entriesToShow.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(10.dp))
                    entriesToShow.forEach { tag ->
                        if (tag.size >= 2) {
                            val displayText = when (tag[0]) {
                                "t" -> "#${tag[1]}"
                                else -> shortenBookmarkId(tag[1])
                            }
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = if (tag[0] != "t") FontFamily.Monospace else null,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                    val remaining = totalCount - entriesToShow.size
                    if (remaining > 0) {
                        Text(
                            text = "... and $remaining more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(10003, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}

private fun shortenBookmarkId(id: String): String {
    if (id.length <= 20) return id
    return "${id.take(8)}...${id.takeLast(8)}"
}
