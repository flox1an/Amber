package com.greenart7c3.nostrsigner.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.defaultAppRelays
import com.greenart7c3.nostrsigner.service.TrustScoreService
import com.greenart7c3.nostrsigner.ui.actions.onAddRelay
import com.greenart7c3.nostrsigner.ui.components.TrustScoreBadge
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RelaysScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    account: Account,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Active relays state
    val activeRelays = remember { mutableStateListOf<NormalizedRelayUrl>() }
    val activeTrustScores = remember { mutableStateMapOf<String, Int?>() }
    val activeLoadingScores = remember { mutableStateMapOf<String, Boolean>() }

    // Default relays state
    val defaultRelays = remember { mutableStateListOf(*Amber.instance.settings.defaultRelays.toTypedArray()) }
    val defaultTrustScores = remember { mutableStateMapOf<String, Int?>() }
    val defaultLoadingScores = remember { mutableStateMapOf<String, Boolean>() }
    val defaultTextField = remember { mutableStateOf(TextFieldValue("")) }
    val defaultIsLoading = remember { mutableStateOf(false) }

    // Profile relays state
    val profileRelays = remember { mutableStateListOf(*Amber.instance.settings.defaultProfileRelays.toTypedArray()) }
    val profileTrustScores = remember { mutableStateMapOf<String, Int?>() }
    val profileLoadingScores = remember { mutableStateMapOf<String, Boolean>() }
    val profileTextField = remember { mutableStateOf(TextFieldValue("")) }
    val profileIsLoading = remember { mutableStateOf(false) }

    // Load active relays
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            activeRelays.addAll(Amber.instance.getSavedRelays(account) + Amber.instance.settings.defaultRelays)
        }
    }

    // Fetch trust scores
    LaunchedEffect(activeRelays.toList()) {
        if (!BuildFlavorChecker.isOfflineFlavor()) {
            activeRelays.forEach { relay ->
                if (!activeTrustScores.containsKey(relay.url)) {
                    activeLoadingScores[relay.url] = true
                    scope.launch(Dispatchers.IO) {
                        activeTrustScores[relay.url] = TrustScoreService.getScore(relay.url)
                        activeLoadingScores[relay.url] = false
                    }
                }
            }
        }
    }
    LaunchedEffect(defaultRelays.toList()) {
        if (!BuildFlavorChecker.isOfflineFlavor()) {
            defaultRelays.forEach { relay ->
                if (!defaultTrustScores.containsKey(relay.url)) {
                    defaultLoadingScores[relay.url] = true
                    scope.launch(Dispatchers.IO) {
                        defaultTrustScores[relay.url] = TrustScoreService.getScore(relay.url)
                        defaultLoadingScores[relay.url] = false
                    }
                }
            }
        }
    }
    LaunchedEffect(profileRelays.toList()) {
        if (!BuildFlavorChecker.isOfflineFlavor()) {
            profileRelays.forEach { relay ->
                if (!profileTrustScores.containsKey(relay.url)) {
                    profileLoadingScores[relay.url] = true
                    scope.launch(Dispatchers.IO) {
                        profileTrustScores[relay.url] = TrustScoreService.getScore(relay.url)
                        profileLoadingScores[relay.url] = false
                    }
                }
            }
        }
    }

    if (defaultIsLoading.value || profileIsLoading.value) {
        CenterCircularProgressIndicator(
            modifier,
            text = stringResource(R.string.testing_relay),
        )
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Active Relays Section
            item {
                RelaySectionHeader(stringResource(R.string.active_relays))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column {
                        activeRelays.forEachIndexed { index, relay ->
                            ActiveRelayRow(
                                relay = relay,
                                trustScore = activeTrustScores[relay.url],
                                isLoadingScore = activeLoadingScores[relay.url] == true,
                                onClick = {
                                    navController.navigate(
                                        "RelayLogScreen/${Base64.getEncoder().encodeToString(relay.url.toByteArray())}",
                                    )
                                },
                            )
                            if (index < activeRelays.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Default App Relays Section
            item {
                RelaySectionHeader(stringResource(R.string.manage_the_relays_used_for_communicating_with_external_applications))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column {
                        defaultRelays.forEachIndexed { index, relay ->
                            EditableRelayRow(
                                relayUrl = relay.url,
                                trustScore = defaultTrustScores[relay.url],
                                isLoadingScore = defaultLoadingScores[relay.url] == true,
                                onRemove = {
                                    defaultTrustScores.remove(relay.url)
                                    defaultLoadingScores.remove(relay.url)
                                    defaultRelays.removeAt(index)
                                    Amber.instance.settings = Amber.instance.settings.copy(defaultRelays = defaultRelays)
                                    LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                                    scope.launch(Dispatchers.IO) {
                                        if (!BuildFlavorChecker.isOfflineFlavor()) {
                                            Amber.instance.checkForNewRelaysAndUpdateAllFilters()
                                        }
                                    }
                                },
                            )
                            if (index < defaultRelays.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            }
                        }
                    }
                }

                AddRelayField(
                    textFieldValue = defaultTextField.value,
                    onValueChange = { defaultTextField.value = it },
                    onAdd = {
                        scope.launch(Dispatchers.IO) {
                            onAddRelay(
                                defaultTextField,
                                defaultIsLoading,
                                defaultRelays,
                                scope,
                                account,
                                context,
                                onDone = {
                                    Amber.instance.settings = Amber.instance.settings.copy(defaultRelays = defaultRelays)
                                    LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                                    scope.launch(Dispatchers.IO) {
                                        if (!BuildFlavorChecker.isOfflineFlavor()) {
                                            Amber.instance.checkForNewRelaysAndUpdateAllFilters()
                                        }
                                        defaultIsLoading.value = false
                                    }
                                },
                            )
                        }
                    },
                )

                TextButton(
                    onClick = {
                        defaultRelays.clear()
                        defaultRelays.addAll(defaultAppRelays)
                        Amber.instance.settings = Amber.instance.settings.copy(defaultRelays = defaultRelays)
                        LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                        scope.launch(Dispatchers.IO) {
                            if (!BuildFlavorChecker.isOfflineFlavor()) {
                                Amber.instance.checkForNewRelaysAndUpdateAllFilters()
                                delay(2000)
                                Amber.instance.client.reconnect()
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        stringResource(R.string.default_relay_text),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Default Profile Relays Section
            item {
                RelaySectionHeader(stringResource(R.string.manage_the_relays_used_to_fetch_your_profile_data))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column {
                        if (profileRelays.isEmpty()) {
                            Text(
                                stringResource(R.string.default_profile_relays),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        profileRelays.forEachIndexed { index, relay ->
                            EditableRelayRow(
                                relayUrl = relay.url,
                                trustScore = profileTrustScores[relay.url],
                                isLoadingScore = profileLoadingScores[relay.url] == true,
                                onRemove = {
                                    profileRelays.removeAt(index)
                                    Amber.instance.settings = Amber.instance.settings.copy(defaultProfileRelays = profileRelays)
                                    LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                                    scope.launch(Dispatchers.IO) {
                                        if (!BuildFlavorChecker.isOfflineFlavor()) {
                                            Amber.instance.checkForNewRelaysAndUpdateAllFilters()
                                        }
                                    }
                                },
                            )
                            if (index < profileRelays.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            }
                        }
                    }
                }

                AddRelayField(
                    textFieldValue = profileTextField.value,
                    onValueChange = { profileTextField.value = it },
                    onAdd = {
                        scope.launch(Dispatchers.IO) {
                            onAddRelay(
                                profileTextField,
                                profileIsLoading,
                                profileRelays,
                                scope,
                                account,
                                context,
                                shouldCheckForBunker = false,
                                onDone = {
                                    Amber.instance.settings = Amber.instance.settings.copy(defaultProfileRelays = profileRelays)
                                    LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                                    scope.launch(Dispatchers.IO) {
                                        if (!BuildFlavorChecker.isOfflineFlavor()) {
                                            Amber.instance.checkForNewRelaysAndUpdateAllFilters()
                                        }
                                        profileIsLoading.value = false
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RelaySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 0.dp, bottom = 8.dp),
    )
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
private fun ActiveRelayRow(
    relay: NormalizedRelayUrl,
    trustScore: Int?,
    isLoadingScore: Boolean,
    onClick: () -> Unit,
) {
    val isConnected by Amber.instance.client.connectedRelaysFlow().map { status ->
        relay in status
    }.collectAsStateWithLifecycle(relay in Amber.instance.client.connectedRelaysFlow().value)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isConnected) AmberColors.success() else AmberColors.error(),
                    shape = CircleShape,
                ),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = relay.url,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isConnected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = if (isConnected) {
                    "${Amber.instance.relayStats.get(relay).pingInMs}ms ping"
                } else {
                    "Unavailable"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TrustScoreBadge(
            score = trustScore,
            isLoading = isLoadingScore,
        )
    }
}

@Composable
private fun EditableRelayRow(
    relayUrl: String,
    trustScore: Int?,
    isLoadingScore: Boolean,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = relayUrl,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TrustScoreBadge(
            score = trustScore,
            isLoading = isLoadingScore,
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddRelayField(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = textFieldValue.text,
            onValueChange = { onValueChange(TextFieldValue(it)) },
            label = { Text("Relay") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
        )
        IconButton(
            onClick = onAdd,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                )
                .size(48.dp),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
