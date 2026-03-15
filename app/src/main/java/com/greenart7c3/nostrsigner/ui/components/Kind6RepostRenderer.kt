package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import org.json.JSONObject

@Composable
fun Kind6RepostRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    // Try to parse embedded event from content
    val embeddedEvent = remember(content) {
        if (content.isBlank()) return@remember null
        try {
            val json = JSONObject(content)
            val authorHex = json.optString("pubkey", "")
            val noteContent = json.optString("content", "")
            val authorNpub = hexToNpub(authorHex)
            FetchedEvent(
                authorPubkey = authorHex,
                authorNpub = authorNpub,
                content = noteContent,
            )
        } catch (_: Exception) {
            null
        }
    }

    // For bare reposts (empty content), get event ID from "e" tag
    val repostedEventId = tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)
    val repostedAuthorPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)

    // Fetch original event if content is empty (bare repost)
    var fetchedEvent by remember { mutableStateOf<FetchedEvent?>(null) }
    var fetchFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (embeddedEvent == null && repostedEventId != null) {
        LaunchedEffect(repostedEventId) {
            isLoading = true
            try {
                val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchEvent(repostedEventId)
                }
                if (fetched != null) {
                    fetchedEvent = fetched
                } else {
                    fetchFailed = true
                }
            } catch (_: Exception) {
                fetchFailed = true
            }
            isLoading = false
        }
    }

    // The event to display (either embedded or fetched)
    val displayEvent = embeddedEvent ?: fetchedEvent

    // Fetch author profile for display event
    var authorProfile by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    val authorToFetch = displayEvent?.authorPubkey ?: repostedAuthorPubkey

    LaunchedEffect(authorToFetch) {
        if (authorToFetch == null) return@LaunchedEffect
        try {
            val profile = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fetchAuthorProfile(authorToFetch)
            }
            if (profile != null) {
                authorProfile = profile
            }
        } catch (_: Exception) {
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "REPOST",
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
                // Repost icon + label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Reposting",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (isLoading) {
                    LoadingRow(text = "Loading original note...")
                } else if (displayEvent != null) {
                    // Original note card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Author identity
                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                AuthorIdentityRow(
                                    displayName = authorProfile?.first,
                                    npub = displayEvent.authorNpub,
                                    pictureUrl = authorProfile?.second,
                                    avatarSize = 28,
                                )
                            }

                            // Note content
                            if (displayEvent.content.isNotBlank()) {
                                val displayContent = if (displayEvent.content.length > 300) {
                                    displayEvent.content.take(300) + "..."
                                } else {
                                    displayEvent.content
                                }
                                Text(
                                    text = displayContent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 6,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                } else if (fetchFailed && repostedEventId != null) {
                    // Fallback: show event ID
                    Text(
                        text = "Event: ${repostedEventId.take(8)}...${repostedEventId.takeLast(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (repostedEventId != null) {
                    Text(
                        text = "Event: ${repostedEventId.take(8)}...${repostedEventId.takeLast(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(6, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
