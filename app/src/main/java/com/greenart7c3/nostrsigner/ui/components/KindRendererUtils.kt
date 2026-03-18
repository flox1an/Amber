package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.vitorpamplona.quartz.nip19Bech32.toNpub
import org.json.JSONArray
import org.json.JSONObject

internal data class FetchedEvent(
    val authorPubkey: String,
    val authorNpub: String,
    val content: String,
    val authorDisplayName: String? = null,
    val authorPictureUrl: String? = null,
)

@Composable
internal fun AuthorAvatar(pictureUrl: String?, size: Int = 24) {
    if (!pictureUrl.isNullOrBlank() && !BuildFlavorChecker.isOfflineFlavor()) {
        SubcomposeAsyncImage(
            model = pictureUrl,
            contentDescription = "Author avatar",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
            loading = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(size.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            error = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(size.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    } else {
        Icon(
            Icons.Outlined.Person,
            contentDescription = null,
            modifier = Modifier.size(size.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal suspend fun fetchAuthorProfile(authorPubkeyHex: String): Pair<String?, String?>? {
    return kotlinx.coroutines.withTimeoutOrNull(5000L) {
        val settings = com.greenart7c3.nostrsigner.LocalPreferences.loadSettingsFromEncryptedStorage()
        val relays = (settings.defaultRelays + settings.defaultProfileRelays).distinct()
        if (relays.isEmpty()) return@withTimeoutOrNull null

        val result = kotlinx.coroutines.CompletableDeferred<Pair<String?, String?>?>()
        val client = com.greenart7c3.nostrsigner.Amber.instance.client
        val subId = java.util.UUID.randomUUID().toString()
        val totalRelays = relays.size
        val eoseCount = java.util.concurrent.atomic.AtomicInteger(0)

        val listener = object : com.vitorpamplona.quartz.nip01Core.relay.client.listeners.IRelayClientListener {
            override fun onIncomingMessage(
                relay: com.vitorpamplona.quartz.nip01Core.relay.client.single.IRelayClient,
                msgStr: String,
                msg: com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.Message,
            ) {
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EventMessage && msg.subId == subId) {
                    if (msg.event.kind == com.vitorpamplona.quartz.nip01Core.metadata.MetadataEvent.KIND) {
                        try {
                            val json = JSONObject(msg.event.content)
                            val displayName = json.optString("display_name", "").ifBlank {
                                json.optString("name", "")
                            }
                            val picture = json.optString("picture", "")
                            if (!result.isCompleted) {
                                result.complete(Pair(displayName, picture))
                            }
                        } catch (_: Exception) {
                            if (!result.isCompleted) result.complete(null)
                        }
                        client.close(subId)
                        client.unsubscribe(this)
                    }
                }
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EoseMessage && msg.subId == subId) {
                    if (eoseCount.incrementAndGet() >= totalRelays && !result.isCompleted) {
                        result.complete(null)
                        client.close(subId)
                        client.unsubscribe(this)
                    }
                }
            }
        }

        client.subscribe(listener)
        val filter = com.vitorpamplona.quartz.nip01Core.relay.filters.Filter(
            kinds = listOf(com.vitorpamplona.quartz.nip01Core.metadata.MetadataEvent.KIND),
            authors = listOf(authorPubkeyHex),
            limit = 1,
        )
        val filterMap = relays.associateWith { listOf(filter) }
        client.openReqSubscription(subId, filterMap)

        result.await()
    }
}

internal suspend fun fetchEvent(eventId: String): FetchedEvent? {
    return kotlinx.coroutines.withTimeoutOrNull(5000L) {
        val settings = com.greenart7c3.nostrsigner.LocalPreferences.loadSettingsFromEncryptedStorage()
        val relays = (settings.defaultRelays + settings.defaultProfileRelays).distinct()
        if (relays.isEmpty()) return@withTimeoutOrNull null

        val result = kotlinx.coroutines.CompletableDeferred<FetchedEvent?>()
        val client = com.greenart7c3.nostrsigner.Amber.instance.client
        val subId = java.util.UUID.randomUUID().toString()
        val totalRelays = relays.size
        val eoseCount = java.util.concurrent.atomic.AtomicInteger(0)

        val listener = object : com.vitorpamplona.quartz.nip01Core.relay.client.listeners.IRelayClientListener {
            override fun onIncomingMessage(
                relay: com.vitorpamplona.quartz.nip01Core.relay.client.single.IRelayClient,
                msgStr: String,
                msg: com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.Message,
            ) {
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EventMessage && msg.subId == subId) {
                    val authorHex = msg.event.pubKey
                    val authorNpub = hexToNpub(authorHex)
                    if (!result.isCompleted) {
                        result.complete(FetchedEvent(authorPubkey = authorHex, authorNpub = authorNpub, content = msg.event.content))
                        client.close(subId)
                        client.unsubscribe(this)
                    }
                }
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EoseMessage && msg.subId == subId) {
                    if (eoseCount.incrementAndGet() >= totalRelays && !result.isCompleted) {
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

internal fun shortenNpub(npub: String): String {
    if (npub.length < 16) return npub
    return "${npub.take(8)}:${npub.takeLast(4)}"
}

internal fun hexToNpub(hex: String): String = try {
    com.vitorpamplona.quartz.utils.Hex.decode(hex).toNpub()
} catch (_: Exception) {
    hex.take(8) + "..." + hex.takeLast(8)
}

internal fun formatSats(millisats: Long): String {
    val sats = millisats / 1000
    return when {
        sats >= 1_000_000 -> String.format("%.2fM sats", sats / 1_000_000.0)
        sats >= 1_000 -> String.format("%.1fk sats", sats / 1_000.0)
        else -> "$sats sats"
    }
}

internal fun buildRawEventJson(kind: Int, content: String, tags: Array<Array<String>>): String = try {
    val json = JSONObject()
    json.put("kind", kind)
    json.put("content", content)
    val tagsArray = JSONArray()
    tags.forEach { tag ->
        val tagArray = JSONArray()
        tag.forEach { tagArray.put(it) }
        tagsArray.put(tagArray)
    }
    json.put("tags", tagsArray)
    json.toString(2)
} catch (_: Exception) {
    content
}

@Composable
internal fun CollapsibleRawJson(rawJson: String) {
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
                text = rawJson,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(12.dp)
                    .horizontalScroll(scrollState),
            )
        }
    }
}

@Composable
internal fun LoadingRow(text: String, spinnerSize: Int = 16) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(spinnerSize.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun AuthorIdentityRow(
    displayName: String?,
    npub: String,
    pictureUrl: String?,
    avatarSize: Int = 24,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AuthorAvatar(
            pictureUrl = pictureUrl,
            size = avatarSize,
        )
        Text(
            text = displayName?.ifBlank { null } ?: shortenNpub(npub),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
