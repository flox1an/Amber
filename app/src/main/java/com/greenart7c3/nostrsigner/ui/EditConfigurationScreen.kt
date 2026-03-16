package com.greenart7c3.nostrsigner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.database.ApplicationWithPermissions
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.service.TrustScoreService
import com.greenart7c3.nostrsigner.service.toShortenHex
import com.greenart7c3.nostrsigner.ui.actions.onAddRelay
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import com.greenart7c3.nostrsigner.ui.components.AmberDangerButton
import com.greenart7c3.nostrsigner.ui.components.AppIcon
import com.greenart7c3.nostrsigner.ui.navigation.Route
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import kotlin.collections.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EditConfigurationScreen(
    modifier: Modifier = Modifier,
    key: String,
    account: Account,
    navController: NavController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var application by remember { mutableStateOf<ApplicationWithPermissions?>(null) }
    var name by remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }
    val relays = remember { mutableStateListOf<NormalizedRelayUrl>() }
    val textFieldRelay = remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }
    val isLoading = remember { mutableStateOf(true) }
    var closeApp by remember { mutableStateOf(false) }
    val trustScores = remember { mutableStateMapOf<String, Int?>() }
    val loadingScores = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            application = Amber.instance.getDatabase(account.npub).dao().getByKey(key)
            name = TextFieldValue(AnnotatedString(application?.application?.name?.ifBlank { application?.application?.key?.toShortenHex() } ?: ""))
            closeApp = application?.application?.closeApplication != false

            application?.application?.relays?.forEach {
                relays.add(
                    it.copy(),
                )
            }

            if (!BuildFlavorChecker.isOfflineFlavor()) {
                relays.forEach { relay ->
                    val url = relay.url
                    if (!trustScores.containsKey(url)) {
                        loadingScores[url] = true
                        scope.launch(Dispatchers.IO) {
                            val score = TrustScoreService.getScore(url)
                            trustScores[url] = score
                            loadingScores[url] = false
                        }
                    }
                }
            }

            isLoading.value = false
        }
    }

    if (isLoading.value) {
        CenterCircularProgressIndicator(
            modifier,
            if (textFieldRelay.value.text.isNotBlank()) {
                stringResource(R.string.testing_relay)
            } else {
                null
            },
        )
    } else {
        Column(
            modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // App header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppIcon(key = key, name = name.text, size = 40.dp)
                Text(
                    text = name.text.ifBlank { key },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
            }

            Text(
                stringResource(R.string.edit_configuration_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Name field
            OutlinedTextField(
                value = name,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next,
                ),
                onValueChange = {
                    name = it
                },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )

            // Close application toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { closeApp = !closeApp }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.close_application),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = closeApp,
                        onCheckedChange = {
                            closeApp = it
                        },
                    )
                }
            }

            // Relay section
            Text(
                text = stringResource(R.string.relays),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (relays.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column {
                        relays.forEachIndexed { index, relay ->
                            EditableRelayRow(
                                relayUrl = relay.url,
                                trustScore = trustScores[relay.url],
                                isLoadingScore = loadingScores[relay.url] == true,
                                onRemove = {
                                    relays.removeAt(index)
                                },
                            )
                            if (index < relays.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            }
                        }
                    }
                }
            }

            AddRelayField(
                textFieldValue = textFieldRelay.value,
                onValueChange = { textFieldRelay.value = it },
                onAdd = {
                    scope.launch(Dispatchers.IO) {
                        onAddRelay(
                            textFieldRelay,
                            isLoading,
                            relays,
                            scope,
                            account,
                            context,
                            onDone = {},
                        )
                    }
                },
            )

            // Action buttons
            AmberButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        application?.let {
                            val localApplicationData =
                                it.application.copy(
                                    name = name.text,
                                    relays = relays,
                                    closeApplication = closeApp,
                                )
                            Amber.instance.getDatabase(account.npub).dao().delete(it.application)
                            Amber.instance.getDatabase(account.npub).dao().insertApplicationWithPermissions(
                                ApplicationWithPermissions(
                                    localApplicationData,
                                    it.permissions,
                                ),
                            )
                            Amber.instance.checkForNewRelaysAndUpdateAllFilters()

                            scope.launch(Dispatchers.Main) {
                                navController.navigate(Route.Applications.route) {
                                    popUpTo(0)
                                }
                            }
                        }
                    }
                },
                text = stringResource(R.string.update),
            )

            AmberDangerButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    application?.let {
                        scope.launch(Dispatchers.IO) {
                            Amber.instance.getDatabase(account.npub).dao().delete(it.application)
                            Amber.instance.getHistoryDatabase(account.npub).dao().deleteHistory(it.application.key)

                            scope.launch(Dispatchers.Main) {
                                navController.navigate(Route.Applications.route) {
                                    popUpTo(0)
                                }
                            }

                            Amber.instance.checkForNewRelaysAndUpdateAllFilters()
                        }
                    }
                },
                text = stringResource(R.string.delete_application),
            )
        }
    }
}
