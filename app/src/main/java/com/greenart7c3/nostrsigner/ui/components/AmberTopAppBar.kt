package com.greenart7c3.nostrsigner.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.AmberBunkerRequest
import com.greenart7c3.nostrsigner.models.IntentData
import com.greenart7c3.nostrsigner.models.TorMode
import com.greenart7c3.nostrsigner.service.TorManager
import com.greenart7c3.nostrsigner.service.toShortenHex
import com.greenart7c3.nostrsigner.ui.navigation.Route
import com.greenart7c3.nostrsigner.ui.navigation.routes
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import com.vitorpamplona.quartz.nip46RemoteSigner.BunkerRequestConnect
import java.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmberTopAppBar(
    destinationRoute: String,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    context: Context,
    navBackStackEntry: NavBackStackEntry?,
    account: Account,
    intents: List<IntentData>,
    bunkerRequests: List<AmberBunkerRequest>,
    packageName: String?,
    navController: androidx.navigation.NavHostController? = null,
) {
    val mainTabRoutes = listOf(
        Route.Applications.route,
        Route.IncomingRequest.route,
        Route.Settings.route,
        Route.Accounts.route,
    )
    val isSubScreen = destinationRoute !in mainTabRoutes &&
        destinationRoute != "login" &&
        destinationRoute != "create" &&
        destinationRoute != "loginPage"
    if (intents.isEmpty() || packageName == null || destinationRoute != Route.IncomingRequest.route) {
        if (destinationRoute != "login" && destinationRoute != "create" && destinationRoute != "loginPage") {
            TopAppBar(
                navigationIcon = {
                    if (isSubScreen && navController != null) {
                        IconButton(
                            onClick = {
                                if (destinationRoute.startsWith("NewNsecBunkerCreated/")) {
                                    navController.navigate(Route.Applications.route) {
                                        popUpTo(0)
                                    }
                                } else {
                                    navController.navigateUp()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.back),
                                contentDescription = context.getString(R.string.go_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                actions = {
                    if (intents.isEmpty() || packageName == null || destinationRoute != Route.IncomingRequest.route) {
                        val relayStats = Amber.instance.stats.relayStatus.collectAsStateWithLifecycle(Pair(emptySet(), emptySet()))
                        if (relayStats.value.first.isNotEmpty() || relayStats.value.second.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(end = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                    tooltip = {
                                        PlainTooltip {
                                            Text(text = context.getString(R.string.reconnect))
                                        }
                                    },
                                    state = rememberTooltipState(),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = AmberColors.amberMuted(),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .clickable(
                                                onClick = {
                                                    Amber.instance.applicationIOScope.launch {
                                                        Amber.instance.reconnect()
                                                    }
                                                },
                                            ),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(6.dp)
                                                    .background(AmberColors.success(), CircleShape),
                                            )
                                            Text(
                                                "${relayStats.value.second.size}/${relayStats.value.first.size} relays",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                }

                                if (Amber.instance.settings.useProxy) {
                                    val torIsRunning by TorManager.isRunning.collectAsStateWithLifecycle()
                                    var orbotProxyEnabled by remember { mutableStateOf(false) }
                                    DisposableEffect(lifecycleOwner) {
                                        val observer = LifecycleEventObserver { _, event ->
                                            when (event) {
                                                Lifecycle.Event.ON_START,
                                                Lifecycle.Event.ON_RESUME,
                                                -> {
                                                    if (Amber.instance.settings.torMode != TorMode.BUILTIN) {
                                                        Amber.instance.applicationIOScope.launch {
                                                            orbotProxyEnabled = Amber.instance.isSocksProxyAlive("127.0.0.1", Amber.instance.settings.proxyPort)
                                                        }
                                                    }
                                                }

                                                else -> {}
                                            }
                                        }
                                        lifecycleOwner.lifecycle.addObserver(observer)

                                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                                    }
                                    val isProxyEnabled = if (Amber.instance.settings.torMode == TorMode.BUILTIN) torIsRunning else orbotProxyEnabled

                                    TooltipBox(
                                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                        tooltip = {
                                            PlainTooltip {
                                                Text(text = if (isProxyEnabled) context.getString(R.string.proxy_is_connected) else context.getString(R.string.proxy_is_not_working))
                                            }
                                        },
                                        state = rememberTooltipState(),
                                    ) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    if (isProxyEnabled) {
                                                        Toast.makeText(context, context.getString(R.string.proxy_is_connected), Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, context.getString(R.string.proxy_is_not_working), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            content = {
                                                Icon(
                                                    Icons.Outlined.Shield,
                                                    context.getString(R.string.proxy),
                                                    tint = if (isProxyEnabled) AmberColors.success() else AmberColors.error(),
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                title = {
                    var title by remember { mutableStateOf(routes.find { it.route.startsWith(destinationRoute) }?.let { context.getString(it.titleRes) } ?: "") }
                    LaunchedEffect(destinationRoute) {
                        if (destinationRoute.startsWith("Permission/") || destinationRoute.startsWith("Activity/") || destinationRoute.startsWith("RelayLogScreen/") || destinationRoute.startsWith("qrcode/")) {
                            launch(Dispatchers.IO) {
                                navBackStackEntry?.arguments?.getString("content")?.let {
                                    title = context.getString(Route.QrCode.titleRes)
                                }
                                navBackStackEntry?.arguments?.getString("packageName")?.let { packageName ->
                                    val application = Amber.instance.getDatabase(account.npub).dao().getByKey(packageName)?.application
                                    title = if (destinationRoute.startsWith("Activity/")) {
                                        "${application?.name?.ifBlank { application.key.toShortenHex() } ?: packageName} - ${routes.find { it.route.startsWith(destinationRoute) }?.let { context.getString(it.titleRes) }}"
                                    } else {
                                        application?.name?.ifBlank { application.key.toShortenHex() } ?: packageName
                                    }
                                }
                                navBackStackEntry?.arguments?.getString("key")?.let { packageName ->
                                    val application = Amber.instance.getDatabase(account.npub).dao().getByKey(packageName)?.application
                                    title = if (destinationRoute.startsWith("Activity/")) {
                                        "${application?.name?.ifBlank { application.key.toShortenHex() } ?: packageName} - ${routes.find { it.route.startsWith(destinationRoute) }?.let { context.getString(it.titleRes) }}"
                                    } else {
                                        application?.name?.ifBlank { application.key.toShortenHex() } ?: packageName
                                    }
                                }
                                navBackStackEntry?.arguments?.getString("url")?.let { url ->
                                    val localUrl = Base64.getDecoder().decode(url).toString(Charsets.UTF_8)
                                    title = localUrl
                                }
                            }
                        } else {
                            launch(Dispatchers.IO) {
                                if (destinationRoute == Route.IncomingRequest.route && (bunkerRequests.isNotEmpty())) {
                                    val request = bunkerRequests.first()
                                    val key = bunkerRequests.first().localKey

                                    var application = Amber.instance.getDatabase(account.npub).dao().getByKey(key)?.application
                                    if (application == null && request.request is BunkerRequestConnect) {
                                        val secret = request.request.secret
                                        if (secret != null) {
                                            application = Amber.instance.getDatabase(account.npub).dao().getByKey(secret)?.application
                                        }
                                    }
                                    val titleTemp = application?.name?.ifBlank { request.name.ifBlank { application.key.toShortenHex() } } ?: request.name
                                    title = titleTemp.ifBlank {
                                        routes.find { it.route == destinationRoute }?.let { context.getString(it.titleRes) } ?: ""
                                    }
                                } else {
                                    title = routes.find { it.route == destinationRoute }?.let { context.getString(it.titleRes) } ?: ""
                                }
                            }
                        }
                    }

                    Text(title)
                },
            )
        }
    }
}

@Composable
fun rememberAppDisplayInfo(packageName: String): AppDisplayInfo {
    val context = LocalContext.current
    return remember(packageName) {
        val appInfo = runCatching {
            context.packageManager.getApplicationInfo(packageName, 0)
        }.getOrNull()

        if (appInfo != null) {
            AppDisplayInfo(
                name = context.packageManager.getApplicationLabel(appInfo).toString(),
                icon = context.packageManager.getApplicationIcon(appInfo),
            )
        } else {
            AppDisplayInfo(name = packageName, icon = null)
        }
    }
}

data class AppDisplayInfo(
    val name: String,
    val icon: Drawable? = null,
)
