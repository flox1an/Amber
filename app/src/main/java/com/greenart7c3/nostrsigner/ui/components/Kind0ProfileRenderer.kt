package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import org.json.JSONObject

data class ProfileField(val key: String, val label: String)

enum class FieldSeverity { NORMAL, HIGH, CRITICAL }

// Known fields with friendly labels, in display order
val KNOWN_PROFILE_FIELDS = mapOf(
    "name" to "Name",
    "display_name" to "Display Name",
    "about" to "About",
    "picture" to "Picture",
    "banner" to "Banner",
    "website" to "Website",
    "nip05" to "NIP-05",
    "lud16" to "Lightning",
    "lud06" to "Lightning (LNURL)",
)

// Fields that are critical when changed (payment redirection)
val CRITICAL_FIELDS = setOf("lud16", "lud06")

// Fields that are high risk when changed (identity)
val HIGH_RISK_FIELDS = setOf("name", "display_name", "nip05")

fun fieldSeverity(key: String): FieldSeverity = when {
    key in CRITICAL_FIELDS -> FieldSeverity.CRITICAL
    key in HIGH_RISK_FIELDS -> FieldSeverity.HIGH
    else -> FieldSeverity.NORMAL
}

// Build the full field list from both current and proposed, preserving order
fun buildProfileFields(currentJson: JSONObject?, proposedJson: JSONObject?): List<ProfileField> {
    val seen = mutableSetOf<String>()
    val fields = mutableListOf<ProfileField>()

    // Add known fields first, in order
    for ((key, label) in KNOWN_PROFILE_FIELDS) {
        val inCurrent = currentJson?.has(key) == true
        val inProposed = proposedJson?.has(key) == true
        if (inCurrent || inProposed) {
            fields.add(ProfileField(key, label))
            seen.add(key)
        }
    }

    // Add any unknown fields from proposed
    proposedJson?.keys()?.forEach { key ->
        if (key !in seen) {
            fields.add(ProfileField(key, key))
            seen.add(key)
        }
    }

    // Add any unknown fields from current that aren't in proposed
    currentJson?.keys()?.forEach { key ->
        if (key !in seen) {
            fields.add(ProfileField(key, key))
            seen.add(key)
        }
    }

    return fields
}

@Composable
fun Kind0ProfileRenderer(
    content: String,
    account: Account,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var currentValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var fetchFailed by remember { mutableStateOf(false) }

    // Always fetch latest profile from relays for accurate diff
    LaunchedEffect(account.npub) {
        // Show stored content immediately while fetching
        val storedContent = LocalPreferences.getProfileContent(context, account.npub)
        if (storedContent.isNotBlank()) {
            currentValues = parseProfileJson(storedContent, account)
        }

        // Always fetch fresh from relays
        isLoading = currentValues.isEmpty()
        try {
            val fetched = fetchCurrentProfile(account)
            if (fetched != null) {
                LocalPreferences.setProfileContent(context, account.npub, fetched)
                currentValues = parseProfileJson(fetched, account)
            } else if (currentValues.isEmpty()) {
                currentValues = mapOf(
                    "name" to account.name.value,
                    "picture" to account.picture.value,
                )
                fetchFailed = true
            }
        } catch (_: Exception) {
            if (currentValues.isEmpty()) {
                currentValues = mapOf(
                    "name" to account.name.value,
                    "picture" to account.picture.value,
                )
                fetchFailed = true
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoadingRow(text = "Loading current profile...", spinnerSize = 24)
        }
        return
    }

    val proposedJson = try {
        JSONObject(content)
    } catch (_: Exception) {
        null
    }
    if (proposedJson == null) return

    // Parse ALL proposed values from JSON (not just known fields)
    val proposedValues = mutableMapOf<String, String>()
    proposedJson.keys().forEach { key ->
        proposedValues[key] = proposedJson.optString(key, "")
    }

    // Build current JSON for field discovery
    val storedContent = LocalPreferences.getProfileContent(context, account.npub)
    val currentJson = if (storedContent.isNotBlank()) {
        try {
            JSONObject(storedContent)
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }

    // Build the full field list dynamically from both current and proposed
    val allFields = buildProfileFields(currentJson, proposedJson)

    val hasCleared = allFields.any { field ->
        val current = currentValues[field.key]
        val proposed = proposedValues[field.key]
        !current.isNullOrBlank() && (proposed == null || proposed.isBlank())
    }

    // Only flag lightning changed if we KNOW the current value
    val lightningChanged = run {
        val proposed = proposedValues["lud16"]
        val current = currentValues["lud16"]
        current != null && proposed != null && proposed != current
    }

    // Only show fields that are actually changing (where we have current to compare)
    // Fields we don't have current values for are NOT shown as changes
    val changedFields = allFields.filter { field ->
        val proposed = proposedValues[field.key]
        val current = currentValues[field.key]
        when {
            // Field in current but missing from proposed = being cleared
            current != null && !current.isBlank() && proposed == null -> true
            // Field not in proposed at all and no current = skip
            proposed == null -> false
            // New field not in current = show it
            current == null && proposed.isNotBlank() -> true
            // Both exist, show if different
            current != null && proposed != current -> true
            else -> false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (changedFields.isEmpty()) "PROFILE UPDATE" else "PROFILE CHANGES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (changedFields.isEmpty()) {
            // No stored profile or no changes — show all proposed fields
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    val allProposed = allFields.filter { proposedValues.containsKey(it.key) }
                    allProposed.forEachIndexed { index, field ->
                        val value = proposedValues[field.key] ?: ""
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = field.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(
                                text = value.ifBlank { "(not set)" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (index < allProposed.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    val visibleFields = allFields.filter { field ->
                        if (!proposedValues.containsKey(field.key)) return@filter false
                        val proposed = proposedValues[field.key] ?: ""
                        val current = currentValues[field.key]
                        // Only show fields that are changing or being cleared
                        current == null || proposed != current
                    }
                    visibleFields.forEachIndexed { index, field ->
                        val proposed = proposedValues[field.key] ?: ""
                        val current = currentValues[field.key]
                        val isChanged = current != null && proposed != current
                        val isCleared = !current.isNullOrBlank() && (proposed.isBlank() || !proposedValues.containsKey(field.key))
                        val severity = fieldSeverity(field.key)

                        val borderColor = when {
                            isCleared -> AmberColors.error()
                            severity == FieldSeverity.CRITICAL && isChanged -> AmberColors.error()
                            severity == FieldSeverity.HIGH && isChanged -> AmberColors.warning()
                            isChanged -> MaterialTheme.colorScheme.primary
                            else -> null
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (borderColor != null) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(if (field.key == "picture" && proposed.isNotBlank() && !BuildFlavorChecker.isOfflineFlavor()) 72.dp else 56.dp)
                                        .background(
                                            color = borderColor,
                                            shape = RoundedCornerShape(topStart = if (index == 0) 16.dp else 0.dp, bottomStart = 0.dp),
                                        ),
                                )
                            } else {
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = field.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.size(2.dp))

                                if (field.key == "picture" && proposed.isNotBlank() && !BuildFlavorChecker.isOfflineFlavor()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SubcomposeAsyncImage(
                                            model = proposed,
                                            contentDescription = "Profile picture",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape),
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(
                                            text = proposed,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = when {
                                                severity == FieldSeverity.CRITICAL && isChanged -> AmberColors.error()
                                                severity == FieldSeverity.HIGH && isChanged -> AmberColors.warning()
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            maxLines = 1,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = proposed.ifBlank { "(cleared)" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = when {
                                            isCleared -> AmberColors.error()
                                            severity == FieldSeverity.CRITICAL && isChanged -> AmberColors.error()
                                            severity == FieldSeverity.HIGH && isChanged -> AmberColors.warning()
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }

                                if (isChanged && !isCleared && !current.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.size(2.dp))
                                    Text(
                                        text = "was: $current",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = TextDecoration.LineThrough,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else if (isCleared && !current.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.size(2.dp))
                                    Text(
                                        text = "was: $current",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = TextDecoration.LineThrough,
                                        ),
                                        color = AmberColors.warning(),
                                    )
                                }
                            }
                        }

                        if (index < visibleFields.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        } // end else (changedFields not empty)

        if (hasCleared) {
            Spacer(modifier = Modifier.size(8.dp))
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
                        text = "This update clears one or more of your existing profile fields.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (lightningChanged) {
            Spacer(modifier = Modifier.size(8.dp))
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
                        text = "Your Lightning address is changing. Verify this is intentional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Collapsible raw JSON
        val prettyJson = remember(content) {
            try {
                JSONObject(content).toString(2)
            } catch (_: Exception) {
                content
            }
        }
        CollapsibleRawJson(rawJson = prettyJson)
    }
}

private fun parseProfileJson(jsonContent: String, account: Account): Map<String, String> {
    val values = mutableMapOf<String, String>()
    try {
        val json = JSONObject(jsonContent)
        json.keys().forEach { key ->
            values[key] = json.optString(key, "")
        }
    } catch (_: Exception) {
        // ignore
    }
    if (!values.containsKey("name")) values["name"] = account.name.value
    if (!values.containsKey("picture")) values["picture"] = account.picture.value
    return values.toMap()
}

private suspend fun fetchCurrentProfile(account: Account): String? {
    return kotlinx.coroutines.withTimeoutOrNull(5000L) {
        val relays = com.greenart7c3.nostrsigner.LocalPreferences
            .loadSettingsFromEncryptedStorage().defaultProfileRelays
        if (relays.isEmpty()) return@withTimeoutOrNull null

        val result = kotlinx.coroutines.CompletableDeferred<String?>()
        val client = com.greenart7c3.nostrsigner.Amber.instance.client
        val subId = java.util.UUID.randomUUID().toString()

        val listener = object : com.vitorpamplona.quartz.nip01Core.relay.client.listeners.IRelayClientListener {
            override fun onIncomingMessage(
                relay: com.vitorpamplona.quartz.nip01Core.relay.client.single.IRelayClient,
                msgStr: String,
                msg: com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.Message,
            ) {
                if (msg is com.vitorpamplona.quartz.nip01Core.relay.commands.toClient.EventMessage && msg.subId == subId) {
                    if (msg.event.kind == com.vitorpamplona.quartz.nip01Core.metadata.MetadataEvent.KIND) {
                        result.complete(msg.event.content)
                        client.close(subId)
                        client.unsubscribe(this)
                    }
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
            kinds = listOf(com.vitorpamplona.quartz.nip01Core.metadata.MetadataEvent.KIND),
            authors = listOf(account.hexKey),
            limit = 1,
        )
        val filterMap = relays.associateWith { listOf(filter) }
        client.openReqSubscription(subId, filterMap)

        result.await()
    }
}
