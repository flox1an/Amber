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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import kotlinx.coroutines.async

private const val MAX_PROFILE_FETCHES = 50

@Composable
fun Kind3FollowListRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    // Extract proposed follows from tags
    val proposedFollows = remember(tags) {
        tags.filter { it.size >= 2 && it[0] == "p" }
            .map { it[1] }
            .distinct()
    }

    // Fetch current follow list from relay
    var currentFollows by remember { mutableStateOf<Set<String>?>(null) }
    var isLoadingCurrent by remember { mutableStateOf(true) }
    var fetchFailed by remember { mutableStateOf(false) }

    LaunchedEffect(account.hexKey) {
        isLoadingCurrent = true
        try {
            val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fetchCurrentFollowList(account.hexKey)
            }
            currentFollows = fetched
        } catch (_: Exception) {
            fetchFailed = true
        }
        isLoadingCurrent = false
    }

    // Compute diff
    val proposedSet = remember(proposedFollows) { proposedFollows.toSet() }
    val added = if (currentFollows != null) proposedSet - currentFollows!! else emptySet()
    val removed = if (currentFollows != null) currentFollows!! - proposedSet else emptySet()
    val hasDiff = currentFollows != null && (added.isNotEmpty() || removed.isNotEmpty())

    // Fetch profiles for added/removed pubkeys (cap at MAX_PROFILE_FETCHES)
    val profiles = remember { mutableStateMapOf<String, Pair<String?, String?>>() }
    val pubkeysToFetch = remember(added, removed) {
        (added + removed).take(MAX_PROFILE_FETCHES)
    }

    LaunchedEffect(pubkeysToFetch) {
        if (pubkeysToFetch.isEmpty()) return@LaunchedEffect
        pubkeysToFetch.map { pubkey ->
            async(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val profile = fetchAuthorProfile(pubkey)
                    if (profile != null) {
                        profiles[pubkey] = profile
                    }
                } catch (_: Exception) {
                }
            }
        }.forEach { it.await() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "FOLLOW LIST UPDATE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Warning banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberColors.warningBg(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AmberColors.warning(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Apps sometimes replace your entire follow list. Review carefully.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Summary card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Proposed list: ${proposedFollows.size} follows",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (isLoadingCurrent) {
                    Spacer(modifier = Modifier.size(8.dp))
                    LoadingRow(text = "Loading current follow list...", spinnerSize = 14)
                } else if (currentFollows != null) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Current list: ${currentFollows!!.size} follows",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (hasDiff) {
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "+${added.size} adding / -${removed.size} removing",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (removed.isNotEmpty()) AmberColors.warning() else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else if (fetchFailed) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Could not fetch current follow list for comparison.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Adding section
        if (added.isNotEmpty()) {
            Spacer(modifier = Modifier.size(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AmberColors.success(),
                )
                Text(
                    text = "Adding (${added.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberColors.success(),
                    fontWeight = FontWeight.Medium,
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberColors.successBg(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val addedList = added.toList()
                    addedList.forEachIndexed { index, pubkey ->
                        FollowIdentityCard(
                            pubkey = pubkey,
                            profile = profiles[pubkey],
                        )
                        if (index < addedList.lastIndex) {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }
        }

        // Removing section
        if (removed.isNotEmpty()) {
            Spacer(modifier = Modifier.size(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AmberColors.error(),
                )
                Text(
                    text = "Removing (${removed.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberColors.error(),
                    fontWeight = FontWeight.Medium,
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberColors.errorBg(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val removedList = removed.toList()
                    removedList.forEachIndexed { index, pubkey ->
                        FollowIdentityCard(
                            pubkey = pubkey,
                            profile = profiles[pubkey],
                        )
                        if (index < removedList.lastIndex) {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(3, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}

@Composable
private fun FollowIdentityCard(
    pubkey: String,
    profile: Pair<String?, String?>?,
) {
    val npub = remember(pubkey) { hexToNpub(pubkey) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AuthorAvatar(
            pictureUrl = profile?.second,
            size = 28,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile?.first?.ifBlank { null } ?: shortenNpub(npub),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (profile?.first != null && profile.first!!.isNotBlank()) {
                Text(
                    text = shortenNpub(npub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private suspend fun fetchCurrentFollowList(accountHexKey: String): Set<String>? {
    return kotlinx.coroutines.withTimeoutOrNull(8000L) {
        val settings = com.greenart7c3.nostrsigner.LocalPreferences.loadSettingsFromEncryptedStorage()
        val relays = (settings.defaultRelays + settings.defaultProfileRelays).distinct()
        if (relays.isEmpty()) return@withTimeoutOrNull null

        val result = kotlinx.coroutines.CompletableDeferred<Set<String>?>()
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
                    if (msg.event.kind == 3) {
                        val follows = msg.event.tags
                            .filter { it.size >= 2 && it[0] == "p" }
                            .map { it[1] }
                            .toSet()
                        if (!result.isCompleted) {
                            result.complete(follows)
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
            kinds = listOf(3),
            authors = listOf(accountHexKey),
            limit = 1,
        )
        val filterMap = relays.associateWith { listOf(filter) }
        client.openReqSubscription(subId, filterMap)

        result.await()
    }
}
