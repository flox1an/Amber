package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account

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
    val parentAuthorPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)
    val hashtags = tags.filter { it.size >= 2 && it[0] == "t" }.map { it[1] }
    val isReply = parentEventId != null

    var parentEvent by remember { mutableStateOf<FetchedEvent?>(null) }
    var parentFetchFailed by remember { mutableStateOf(false) }
    var isLoadingParent by remember { mutableStateOf(false) }

    if (isReply && parentEventId != null) {
        // Fetch parent event
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

        // Fetch author profile separately (using p tag or event author)
        val authorToFetch = parentAuthorPubkey ?: parentEvent?.authorPubkey
        val authorProfile = rememberProfile(authorToFetch)

        // Apply profile to event when both are available
        if (authorProfile != null && parentEvent != null && parentEvent?.authorPictureUrl == null) {
            parentEvent = parentEvent!!.copy(
                authorDisplayName = authorProfile.first,
                authorPictureUrl = authorProfile.second,
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isReply) "REPLY" else "NOTE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Reply context
        if (isReply && parentEventId != null) {
            if (isLoadingParent) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        LoadingRow(text = "Loading original note...")
                    }
                }
            } else if (parentEvent != null) {
                val pe = parentEvent!!
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Default.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AuthorIdentityRow(
                                displayName = pe.authorDisplayName,
                                npub = pe.authorNpub,
                                pictureUrl = pe.authorPictureUrl,
                                avatarSize = 24,
                            )
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = pe.content.take(200) + if (pe.content.length > 200) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else if (parentFetchFailed) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
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
                NoteContent(content = content, tags = tags)

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
        val rawJson = remember(content, tags) { buildRawEventJson(1, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}

@Composable
private fun NoteContent(content: String, tags: Array<Array<String>> = emptyArray()) {
    var expanded by remember { mutableStateOf(false) }
    val isLong = content.length > CONTENT_TRUNCATE_LENGTH
    val displayText = if (isLong && !expanded) content.take(CONTENT_TRUNCATE_LENGTH) + "..." else content

    // Resolve mention names from p-tags via relay fetch
    val pTagHexKeys = tags.filter { it.size >= 2 && it[0] == "p" }.map { it[1] }
    val profiles = rememberProfiles(pTagHexKeys)

    // Build a lookup: npub -> display name
    val npubToName = remember(profiles.size) {
        val map = mutableMapOf<String, String>()
        pTagHexKeys.forEach { hex ->
            val npub = hexToNpub(hex)
            val profile = profiles[hex]
            val name = profile?.first?.ifBlank { null } ?: shortenNpub(npub)
            map[npub] = name
        }
        map
    }

    val chipBg = MaterialTheme.colorScheme.primaryContainer
    val chipText = MaterialTheme.colorScheme.primary

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
                    val name = npubToName[npub] ?: shortenNpub(npub)
                    withStyle(
                        SpanStyle(
                            color = chipText,
                            fontWeight = FontWeight.SemiBold,
                            background = chipBg,
                        ),
                    ) {
                        append(" @$name ")
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
