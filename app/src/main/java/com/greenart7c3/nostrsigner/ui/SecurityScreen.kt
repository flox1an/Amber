package com.greenart7c3.nostrsigner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import com.greenart7c3.nostrsigner.ui.components.TitleExplainer
import com.greenart7c3.nostrsigner.ui.navigation.Route
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SecurityScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val biometricItems =
        persistentListOf(
            TitleExplainer(stringResource(BiometricsTimeType.EVERY_TIME.resourceId)),
            TitleExplainer(stringResource(BiometricsTimeType.ONE_MINUTE.resourceId)),
            TitleExplainer(stringResource(BiometricsTimeType.FIVE_MINUTES.resourceId)),
            TitleExplainer(stringResource(BiometricsTimeType.TEN_MINUTES.resourceId)),
        )
    var enableBiometrics by remember { mutableStateOf(Amber.instance.settings.useAuth) }
    val setupPin by remember { mutableStateOf(Amber.instance.settings.usePin) }
    var biometricsIndex by remember {
        mutableIntStateOf(Amber.instance.settings.biometricsTimeType.screenCode)
    }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                enableBiometrics = !enableBiometrics
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.enable_biometrics),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(
                            checked = enableBiometrics,
                            onCheckedChange = {
                                enableBiometrics = !enableBiometrics
                            },
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (setupPin) {
                                    scope.launch(Dispatchers.IO) {
                                        val pin = LocalPreferences.loadPinFromEncryptedStorage()
                                        scope.launch(Dispatchers.Main) {
                                            navController.navigate("${Route.ConfirmPin.route.split("/")[0]}/$pin")
                                        }
                                    }
                                } else {
                                    navController.navigate(Route.SetupPin.route)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.setup_pin),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(
                            checked = setupPin,
                            onCheckedChange = {
                                if (setupPin) {
                                    scope.launch(Dispatchers.IO) {
                                        val pin = LocalPreferences.loadPinFromEncryptedStorage()
                                        scope.launch(Dispatchers.Main) {
                                            navController.navigate("${Route.ConfirmPin.route.split("/")[0]}/$pin")
                                        }
                                    }
                                } else {
                                    navController.navigate(Route.SetupPin.route)
                                }
                            },
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        SettingsRow(
                            R.string.when_to_ask,
                            R.string.when_to_ask,
                            biometricItems,
                            biometricsIndex,
                        ) {
                            biometricsIndex = it
                        }
                    }
                }
            }
        }

        item {
            AmberButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        Amber.instance.settings = Amber.instance.settings.copy(
                            useAuth = enableBiometrics,
                            biometricsTimeType = parseBiometricsTimeType(biometricsIndex),
                        )
                        LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                        scope.launch(Dispatchers.Main) {
                            navController.navigateUp()
                        }
                    }
                },
                text = stringResource(R.string.save),
            )
        }
    }
}
