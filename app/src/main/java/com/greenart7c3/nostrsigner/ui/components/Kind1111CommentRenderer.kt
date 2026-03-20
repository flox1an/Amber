package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
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

@Composable
fun Kind1111CommentRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    // Root event (uppercase E tag) or reply target (lowercase e tag)
    val rootEventId = tags.firstOrNull { it.size >= 2 && it[0] == "E" }?.get(1)
    val replyEventId = tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)
    val externalUrl = tags.firstOrNull { it.size >= 2 && it[0] == "I" }?.get(1)
    val parentAuthorPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)

    // Fetch parent event for context
    val parentEventId = replyEventId ?: rootEventId
    var parentEvent by remember { mutableStateOf<FetchedEvent?>(null) }
    var parentFetchFailed by remember { mutableStateOf(false) }
    var isLoadingParent by remember { mutableStateOf(false) }

    if (parentEventId != null) {
        LaunchedEffect(parentEventId) {
            isLoadingParent = true
            try {
                val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchEvent(parentEventId)
                }
                if (fetched != null) {
                    parentEvent = fetched
                } else {
                    parentFetchFailed = true
                }
            } catch (_: Exception) {
                parentFetchFailed = true
            }
            isLoadingParent = false
        }
    }

    val authorToFetch = parentAuthorPubkey ?: parentEvent?.authorPubkey
    val parentAuthorProfile = rememberProfile(authorToFetch)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "COMMENT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Parent context — either a Nostr event or an external URL
        if (parentEventId != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Commenting on",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Spacer(modifier = Modifier.size(6.dp))

                    if (isLoadingParent) {
                        LoadingRow(text = "Loading note...")
                    } else if (parentEvent != null) {
                        AuthorIdentityRow(
                            displayName = parentAuthorProfile?.first,
                            npub = parentEvent!!.authorNpub,
                            pictureUrl = parentAuthorProfile?.second,
                            avatarSize = 24,
                        )
                        if (parentEvent!!.content.isNotBlank()) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = parentEvent!!.content.take(200) +
                                    if (parentEvent!!.content.length > 200) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Text(
                            text = "Note: ${parentEventId.take(8)}...${parentEventId.takeLast(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else if (externalUrl != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Commenting on",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83C\uDF10", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = externalUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // Comment content
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(1111, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
