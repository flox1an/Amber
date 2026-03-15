package com.greenart7c3.nostrsigner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.ClearTextEncryptedDataKind
import com.greenart7c3.nostrsigner.models.EventEncryptedDataKind
import com.greenart7c3.nostrsigner.models.Permission
import com.greenart7c3.nostrsigner.models.SignerType
import com.greenart7c3.nostrsigner.models.TagArrayEncryptedDataKind
import com.greenart7c3.nostrsigner.service.BunkerRequestUtils
import com.greenart7c3.nostrsigner.service.MultiEventScreenIntents
import com.greenart7c3.nostrsigner.service.model.AmberEvent
import com.greenart7c3.nostrsigner.ui.components.RememberMyChoice
import com.vitorpamplona.quartz.nip46RemoteSigner.BunkerRequestSign

@Composable
fun SeeDetailsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    var rememberType by remember { mutableStateOf(MultiEventScreenIntents.intents.firstOrNull()?.rememberType?.value ?: MultiEventScreenIntents.bunkerRequests.first().rememberType.value) }
    val type = if (MultiEventScreenIntents.intents.isNotEmpty()) {
        MultiEventScreenIntents.intents.first().type
    } else {
        BunkerRequestUtils.getTypeFromBunker(MultiEventScreenIntents.bunkerRequests.first().request)
    }
    val permission = if (type == SignerType.SIGN_EVENT) {
        val event = if (MultiEventScreenIntents.intents.isNotEmpty()) {
            MultiEventScreenIntents.intents.first().event!!
        } else {
            MultiEventScreenIntents.bunkerRequests.first().signedEvent!!
        }
        Permission("sign_event", event.kind)
    } else {
        Permission(type.toString().toLowerCase(Locale.current), null)
    }

    val message = if (type == SignerType.CONNECT) {
        stringResource(R.string.connect)
    } else {
        permission.toLocalizedString(context)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.is_requiring_to_sign_these_events_related_to_permission, MultiEventScreenIntents.appName, message),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        item {
            Row {
                RememberMyChoice(
                    alwaysShow = true,
                    shouldRunAcceptOrReject = null,
                    onAccept = {},
                    onReject = {},
                    onChanged = {
                        rememberType = it
                        MultiEventScreenIntents.intents.forEach { intent ->
                            intent.rememberType.value = rememberType
                        }
                    },
                    packageName = null,
                )
            }
        }

        items(MultiEventScreenIntents.intents.size) { idx ->
            val intent = MultiEventScreenIntents.intents[idx]
            val data = if (intent.type == SignerType.SIGN_EVENT) {
                val event = intent.event!!
                if (event.kind == 22242) AmberEvent.relay(event) ?: event.content else event.content
            } else {
                if (type.name.contains("ENCRYPT") && intent.encryptedData is ClearTextEncryptedDataKind) {
                    intent.encryptedData.text
                } else if (intent.encryptedData is EventEncryptedDataKind) {
                    if (intent.encryptedData.sealEncryptedDataKind != null) {
                        if (intent.encryptedData.sealEncryptedDataKind is EventEncryptedDataKind) {
                            intent.encryptedData.sealEncryptedDataKind.event.content
                        } else {
                            intent.encryptedData.sealEncryptedDataKind.result
                        }
                    } else {
                        intent.encryptedData.event.content
                    }
                } else {
                    if (intent.encryptedData is TagArrayEncryptedDataKind) {
                        intent.encryptedData.tagArray.joinToString(separator = ", ") {
                            "[${it.joinToString(separator = ", ") { tag -> "\"${tag}\"" }}]"
                        }
                    } else {
                        intent.encryptedData?.result ?: ""
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            intent.checked.value = !intent.checked.value
                        },
                ) {
                    Checkbox(
                        checked = intent.checked.value,
                        onCheckedChange = { _ ->
                            intent.checked.value = !intent.checked.value
                        },
                        colors = CheckboxDefaults.colors().copy(
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        text = data.ifBlank { message },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (intent.checked.value) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(MultiEventScreenIntents.bunkerRequests.size) { idx ->
            val bunkerRequest = MultiEventScreenIntents.bunkerRequests[idx]
            val data = if (bunkerRequest.request is BunkerRequestSign) {
                val event = bunkerRequest.signedEvent!!
                if (event.kind == 22242) AmberEvent.relay(event) ?: event.content else event.content
            } else {
                if (type.name.contains("ENCRYPT") && bunkerRequest.encryptedData is ClearTextEncryptedDataKind) {
                    bunkerRequest.encryptedData.text
                } else if (bunkerRequest.encryptedData is EventEncryptedDataKind) {
                    if (bunkerRequest.encryptedData.sealEncryptedDataKind != null) {
                        if (bunkerRequest.encryptedData.sealEncryptedDataKind is EventEncryptedDataKind) {
                            bunkerRequest.encryptedData.sealEncryptedDataKind.event.content
                        } else {
                            bunkerRequest.encryptedData.sealEncryptedDataKind.result
                        }
                    } else {
                        bunkerRequest.encryptedData.event.content
                    }
                } else {
                    if (bunkerRequest.encryptedData is TagArrayEncryptedDataKind) {
                        bunkerRequest.encryptedData.tagArray.joinToString(separator = ", ") {
                            "[${it.joinToString(separator = ", ") { tag -> "\"${tag}\"" }}]"
                        }
                    } else {
                        bunkerRequest.encryptedData?.result ?: BunkerRequestUtils.getDataFromBunker(bunkerRequest.request)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            bunkerRequest.checked.value = !bunkerRequest.checked.value
                        },
                ) {
                    Checkbox(
                        checked = bunkerRequest.checked.value,
                        onCheckedChange = { _ ->
                            bunkerRequest.checked.value = !bunkerRequest.checked.value
                        },
                        colors = CheckboxDefaults.colors().copy(
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        text = data.ifBlank { message },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (bunkerRequest.checked.value) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
