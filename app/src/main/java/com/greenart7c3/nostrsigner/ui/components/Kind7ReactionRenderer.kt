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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenart7c3.nostrsigner.models.Account

@Composable
fun Kind7ReactionRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val reactionDisplay = remember(content) {
        when {
            content == "+" || content.isBlank() -> "\u2764\uFE0F"
            content == "-" -> "\uD83D\uDC4E"
            else -> content
        }
    }

    val reactedEventId = tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)
    val reactedAuthorPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)

    // Fetch the reacted-to event
    var fetchedEvent by remember { mutableStateOf<FetchedEvent?>(null) }
    var fetchFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (reactedEventId != null) {
        LaunchedEffect(reactedEventId) {
            isLoading = true
            try {
                val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchEvent(reactedEventId)
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

    // Fetch author profile
    var authorProfile by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    val authorToFetch = fetchedEvent?.authorPubkey ?: reactedAuthorPubkey

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
            text = "REACTION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Large reaction emoji display
        Text(
            text = reactionDisplay,
            fontSize = 48.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        // Reacted-to event context
        if (reactedEventId != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(
                            Icons.Default.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Reacting to",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    if (isLoading) {
                        LoadingRow(text = "Loading note...")
                    } else if (fetchedEvent != null) {
                        AuthorIdentityRow(
                            displayName = authorProfile?.first,
                            npub = fetchedEvent!!.authorNpub,
                            pictureUrl = authorProfile?.second,
                            avatarSize = 24,
                        )
                        if (fetchedEvent!!.content.isNotBlank()) {
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = fetchedEvent!!.content.take(200) +
                                    if (fetchedEvent!!.content.length > 200) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Text(
                            text = "Note: ${reactedEventId.take(8)}...${reactedEventId.takeLast(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(7, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
