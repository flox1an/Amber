package com.greenart7c3.nostrsigner.ui

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.database.ApplicationEntity
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.actions.onAddRelay
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import com.greenart7c3.nostrsigner.ui.components.TitleExplainer
import java.util.UUID
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NewNsecBunkerScreen(
    modifier: Modifier = Modifier,
    account: Account,
    navController: NavController,
) {
    val secret = remember { mutableStateOf(UUID.randomUUID().toString()) }
    var name by remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val relays = remember { mutableStateListOf(*Amber.instance.settings.defaultRelays.toTypedArray()) }
    val textFieldRelay = remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val deleteAfterItems = persistentListOf(
        TitleExplainer(stringResource(DeleteAfterType.NEVER.resourceId)),
        TitleExplainer(stringResource(DeleteAfterType.FIVE_MINUTES.resourceId)),
        TitleExplainer(stringResource(DeleteAfterType.TEN_MINUTES.resourceId)),
        TitleExplainer(stringResource(DeleteAfterType.ONE_HOUR.resourceId)),
        TitleExplainer(stringResource(DeleteAfterType.ONE_DAY.resourceId)),
        TitleExplainer(stringResource(DeleteAfterType.ONE_WEEK.resourceId)),
    )
    var deleteAfterIndex by remember { mutableIntStateOf(DeleteAfterType.NEVER.screenCode) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (isLoading.value) {
        CenterCircularProgressIndicator(
            modifier = Modifier,
            text = stringResource(R.string.testing_relay),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Description
            item {
                Text(
                    stringResource(R.string.create_nsecbunker_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Name field
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }

            // Delete after setting
            item {
                var showDialog by remember { mutableStateOf(false) }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(stringResource(R.string.delete_after)) },
                        text = {
                            Column {
                                deleteAfterItems.forEachIndexed { index, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                deleteAfterIndex = index
                                                showDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        RadioButton(
                                            selected = deleteAfterIndex == index,
                                            onClick = {
                                                deleteAfterIndex = index
                                                showDialog = false
                                            },
                                        )
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                    )
                }

                Text(
                    text = stringResource(R.string.delete_after).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDialog = true },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.delete_after),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = deleteAfterItems[deleteAfterIndex].title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Relays section
            item {
                Text(
                    text = stringResource(R.string.relays).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                if (relays.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Column {
                            relays.forEachIndexed { index, relay ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = relay.url,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(onClick = { relays.removeAt(index) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                if (index < relays.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(start = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // Add relay field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = textFieldRelay.value.text,
                        onValueChange = { textFieldRelay.value = TextFieldValue(it) },
                        label = { Text(stringResource(R.string.wss)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                scope.launch(Dispatchers.IO) {
                                    onAddRelay(textFieldRelay, isLoading, relays, scope, account, context, onDone = {})
                                }
                            },
                        ),
                    )
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                scope.launch(Dispatchers.IO) {
                                    onAddRelay(textFieldRelay, isLoading, relays, scope, account, context, onDone = {})
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }

            // Create button
            item {
                val title = stringResource(R.string.relays)
                val noRelaysMessage = stringResource(R.string.no_relays_added)
                val noNameMessage = stringResource(R.string.name_cannot_be_empty)

                AmberButton(
                    text = stringResource(R.string.create),
                    onClick = {
                        if (relays.isEmpty()) {
                            ToastManager.toast(title, noRelaysMessage)
                            return@AmberButton
                        }
                        if (name.text.isBlank()) {
                            ToastManager.toast(title, noNameMessage)
                            return@AmberButton
                        }
                        scope.launch(Dispatchers.IO) {
                            val deleteAfter = deleteAfterToSeconds(parseDeleteAfterType(deleteAfterIndex))
                            val application = ApplicationEntity(
                                key = secret.value,
                                name = name.text,
                                relays = relays,
                                url = "",
                                icon = "",
                                description = "",
                                pubKey = account.hexKey,
                                isConnected = false,
                                secret = secret.value,
                                useSecret = true,
                                signPolicy = account.signPolicy,
                                closeApplication = true,
                                deleteAfter = deleteAfter,
                                lastUsed = 0L,
                            )
                            Amber.instance.getDatabase(account.npub).dao().insertApplication(application)
                            scope.launch(Dispatchers.Main) {
                                navController.navigate("NewNsecBunkerCreated/${secret.value}")
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun NewNsecBunkerCreatedScreen(
    modifier: Modifier = Modifier,
    account: Account,
    key: String,
) {
    val isLoading = remember { mutableStateOf(false) }
    var application by remember { mutableStateOf(ApplicationEntity.empty()) }
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading.value = true
        launch(Dispatchers.IO) {
            application = Amber.instance.getDatabase(account.npub).dao().getByKey(key)?.application ?: ApplicationEntity.empty()
            isLoading.value = false
        }
    }

    if (isLoading.value) {
        CenterCircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        val relays = application.relays.joinToString(separator = "&") { "relay=${it.url}" }
        val localSecret = "&secret=${application.secret}"
        val bunkerUri = "bunker://${account.hexKey}?$relays$localSecret"

        LaunchedEffect(Unit) {
            Amber.instance.applicationIOScope.launch(Dispatchers.IO) {
                Amber.instance.checkForNewRelaysAndUpdateAllFilters(shouldReconnect = true)
            }
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.your_nsec_bunker_has_been_created),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                Text(
                    stringResource(R.string.use_this_url_in_your_app),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Text(
                        bunkerUri,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            item {
                QrCodeDrawer(bunkerUri)
            }

            item {
                AmberButton(
                    onClick = {
                        scope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText("", bunkerUri),
                                ),
                            )
                        }
                    },
                    text = stringResource(R.string.copy_to_clipboard),
                )
            }
        }
    }
}
