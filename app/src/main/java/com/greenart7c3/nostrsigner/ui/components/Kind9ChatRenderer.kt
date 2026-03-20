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
import androidx.compose.material.icons.filled.Forum
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import org.json.JSONObject

@Composable
fun Kind9ChatRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val roomEventId = tags.firstOrNull { it.size >= 3 && it[0] == "e" && it[2] == "root" }?.get(1)
        ?: tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)

    // Fetch channel name from the kind 40 event
    var channelName by remember { mutableStateOf<String?>(null) }
    var isLoadingChannel by remember { mutableStateOf(false) }

    if (roomEventId != null) {
        LaunchedEffect(roomEventId) {
            isLoadingChannel = true
            try {
                val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchEvent(roomEventId)
                }
                if (fetched != null) {
                    channelName = try {
                        val json = JSONObject(fetched.content)
                        json.optString("name", "").ifBlank { null }
                    } catch (_: Exception) {
                        null
                    }
                }
            } catch (_: Exception) {
            }
            isLoadingChannel = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CHAT MESSAGE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Room/channel reference
        if (roomEventId != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column {
                        Text(
                            text = "CHANNEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        if (isLoadingChannel) {
                            LoadingRow(text = "Loading channel...", spinnerSize = 12)
                        } else if (channelName != null) {
                            Text(
                                text = channelName!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${roomEventId.take(8)}...${roomEventId.takeLast(8)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = "${roomEventId.take(8)}...${roomEventId.takeLast(8)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Message content
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

        val rawJson = remember(content, tags) { buildRawEventJson(9, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
