package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import com.vitorpamplona.quartz.nip19Bech32.toNpub

private val IMAGE_URL_REGEX =
    Regex("""https?://\S+\.(jpg|jpeg|png|gif|webp)(\?\S*)?""", RegexOption.IGNORE_CASE)
private val NPUB_MENTION_REGEX =
    Regex("""nostr:npub1[a-z0-9]{58}""")

private const val CONTENT_TRUNCATE_LENGTH = 500

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Kind1NoteRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val parentEventId = tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)
    val hashtags = tags.filter { it.size >= 2 && it[0] == "t" }.map { it[1] }
    val isReply = parentEventId != null

    var parentEvent by remember { mutableStateOf<FetchedEvent?>(null) }
    var parentFetchFailed by remember { mutableStateOf(false) }
    var isLoadingParent by remember { mutableStateOf(false) }

    if (isReply && parentEventId != null) {
        LaunchedEffect(parentEventId) {
            isLoadingParent = true
            try {
                val fetched = fetchEvent(parentEventId)
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SHORT TEXT NOTE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Reply context
        if (isReply && parentEventId != null) {
            if (isLoadingParent) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "Loading parent note...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else if (parentEvent != null) {
                val pe = parentEvent!!
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                                text = "Replying to ${shortenNpub(pe.authorNpub)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = pe.content.take(100) + if (pe.content.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else if (parentFetchFailed) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                            text = "Reply to: ${parentEventId.take(8)}...${parentEventId.takeLast(8)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Note content card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                NoteContent(content = content)

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

        // Collapsible raw JSON
        Spacer(modifier = Modifier.size(12.dp))
        var showRaw by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showRaw = !showRaw }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (showRaw) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = if (showRaw) "Hide raw event" else "Show raw event",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = showRaw) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(scrollState),
                )
            }
        }
    }
}

@Composable
private fun NoteContent(content: String) {
    var expanded by remember { mutableStateOf(false) }
    val isLong = content.length > CONTENT_TRUNCATE_LENGTH
    val displayText = if (isLong && !expanded) content.take(CONTENT_TRUNCATE_LENGTH) + "..." else content

    val annotated = buildAnnotatedString {
        var lastIndex = 0
        val matches = (NPUB_MENTION_REGEX.findAll(displayText) + IMAGE_URL_REGEX.findAll(displayText))
            .sortedBy { it.range.first }

        for (match in matches) {
            if (match.range.first < lastIndex) continue
            append(displayText.substring(lastIndex, match.range.first))

            when {
                match.value.startsWith("nostr:npub") -> {
                    val npub = match.value.removePrefix("nostr:")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) {
                        append("@${shortenNpub(npub)}")
                    }
                }
                else -> {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        append(match.value)
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < displayText.length) {
            append(displayText.substring(lastIndex))
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )

    if (isLong) {
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = if (expanded) "Show less" else "Show more",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { expanded = !expanded },
        )
    }
}

private fun shortenNpub(npub: String): String {
    if (npub.length < 16) return npub
    return "${npub.take(8)}:${npub.takeLast(4)}"
}

data class FetchedEvent(
    val authorNpub: String,
    val content: String,
)

private suspend fun fetchEvent(eventId: String): FetchedEvent? {
    return kotlinx.coroutines.withTimeoutOrNull(5000L) {
        val relays = com.greenart7c3.nostrsigner.LocalPreferences
            .loadSettingsFromEncryptedStorage().defaultProfileRelays
        if (relays.isEmpty()) return@withTimeoutOrNull null

        val result = kotlinx.coroutines.CompletableDeferred<FetchedEvent?>()
        val client = com.greenart7c3.nostrsigner.Amber.instance.client
        val subId = java.util.UUID.randomUUID().toString()

        val listener = object : com.vitorpamplona.quartz.nip01Core.relay.client.listeners.IRelayClientListener {
            override fun onIncomingMessage(
                relay: com.vitorpamplona.quartz.nip01Core.relay.client.single.IRelayClient,
                msgStr: String,
                msg: com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.Message,
            ) {
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EventMessage && msg.subId == subId) {
                    val authorHex = msg.event.pubKey
                    val authorNpub = try {
                        com.vitorpamplona.quartz.utils.Hex
                            .decode(authorHex).toNpub()
                    } catch (_: Exception) {
                        authorHex.take(8) + "..." + authorHex.takeLast(8)
                    }
                    result.complete(FetchedEvent(authorNpub = authorNpub, content = msg.event.content))
                    client.close(subId)
                    client.unsubscribe(this)
                }
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EoseMessage && msg.subId == subId) {
                    if (!result.isCompleted) {
                        result.complete(null)
                        client.close(subId)
                        client.unsubscribe(this)
                    }
                }
            }
        }

        client.subscribe(listener)
        val filter = com.vitorpamplona.quartz.nip01Core.relay.filters.Filter(
            ids = listOf(eventId),
            limit = 1,
        )
        val filterMap = relays.associateWith { listOf(filter) }
        client.openReqSubscription(subId, filterMap)

        result.await()
    }
}
