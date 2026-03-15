package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
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

val PROFILE_FIELDS = listOf(
    ProfileField("name", "Name"),
    ProfileField("display_name", "Display Name"),
    ProfileField("about", "About"),
    ProfileField("picture", "Picture"),
    ProfileField("nip05", "NIP-05"),
    ProfileField("lud16", "Lightning"),
)

@Composable
fun Kind0ProfileRenderer(
    content: String,
    account: Account,
) {
    val context = LocalContext.current

    // Load full current profile from stored Kind 0 content
    val currentValues = remember(account.npub) {
        val storedContent = LocalPreferences.getProfileContent(context, account.npub)
        val values = mutableMapOf<String, String>()
        if (storedContent.isNotBlank()) {
            try {
                val json = JSONObject(storedContent)
                for (field in PROFILE_FIELDS) {
                    if (json.has(field.key)) {
                        values[field.key] = json.optString(field.key, "")
                    }
                }
            } catch (_: Exception) {
                // Fall back to what we have from Account
            }
        }
        // Fall back to Account fields if not in stored content
        if (!values.containsKey("name")) {
            values["name"] = account.name.value
        }
        if (!values.containsKey("picture")) {
            values["picture"] = account.picture.value
        }
        values.toMap()
    }

    val proposedValues = mutableMapOf<String, String>()
    try {
        val json = JSONObject(content)
        for (field in PROFILE_FIELDS) {
            if (json.has(field.key)) {
                proposedValues[field.key] = json.optString(field.key, "")
            }
        }
    } catch (_: Exception) {
        return
    }

    // Only flag as cleared if we KNOW the current value and proposed is empty
    val hasCleared = PROFILE_FIELDS.any { field ->
        val current = currentValues[field.key]
        val proposed = proposedValues[field.key]
        !current.isNullOrBlank() && proposed != null && proposed.isBlank()
    }

    // Only flag lightning changed if we KNOW the current value
    val lightningChanged = run {
        val proposed = proposedValues["lud16"]
        val current = currentValues["lud16"]
        current != null && proposed != null && proposed != current
    }

    // Only show fields that are actually changing (where we have current to compare)
    // Fields we don't have current values for are NOT shown as changes
    val changedFields = PROFILE_FIELDS.filter { field ->
        if (!proposedValues.containsKey(field.key)) return@filter false
        val proposed = proposedValues[field.key] ?: ""
        val current = currentValues[field.key]
        // Only show as changed if we have a current value AND it differs
        current != null && proposed != current
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
                    val allProposed = PROFILE_FIELDS.filter { proposedValues.containsKey(it.key) }
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
                    val visibleFields = PROFILE_FIELDS.filter { field ->
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
                        val isCleared = !current.isNullOrBlank() && proposed.isBlank()
                        val isLud16 = field.key == "lud16"

                        val borderColor = when {
                            isCleared -> AmberColors.warning()
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
                                            color = if (isLud16 && isChanged) AmberColors.warning() else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = proposed.ifBlank { "(cleared)" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = when {
                                            isLud16 && isChanged -> AmberColors.warning()
                                            isCleared -> AmberColors.warning()
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
            val prettyJson = try {
                JSONObject(content).toString(2)
            } catch (_: Exception) {
                content
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text = prettyJson,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(scrollState),
                )
            }
        }
    }
}
