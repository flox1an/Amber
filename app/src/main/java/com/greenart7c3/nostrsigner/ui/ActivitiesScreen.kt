package com.greenart7c3.nostrsigner.ui

import android.util.Log
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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.database.HistoryEntity
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.TimeUtils
import com.greenart7c3.nostrsigner.models.supportedKindNumbers
import com.greenart7c3.nostrsigner.service.ApplicationNameCache
import com.greenart7c3.nostrsigner.service.model.AmberEvent
import com.greenart7c3.nostrsigner.service.toShortenHex
import com.greenart7c3.nostrsigner.ui.components.AppIcon
import com.greenart7c3.nostrsigner.ui.components.SimpleSearchBar
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    modifier: Modifier,
    paddingValues: PaddingValues,
    topPadding: Dp,
    account: Account,
) {
    val database = Amber.instance.getHistoryDatabase(account.npub)
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val pager = remember(searchQuery) {
        Pager(
            PagingConfig(pageSize = 20, enablePlaceholders = false),
        ) {
            if (searchQuery.isEmpty()) {
                database.dao().getAllHistoryPaging()
            } else {
                database.dao().searchAllHistoryPaging(searchQuery.lowercase())
            }
        }
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    val textFieldState by remember { mutableStateOf(TextFieldState(initialText = searchQuery)) }

    Column(modifier = modifier.padding(top = topPadding)) {
        SimpleSearchBar(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            textFieldState = textFieldState,
            onSearch = { searchQuery = it },
            searchResults = supportedKindNumbers.map { it.toLocalizedString(context, true) },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (lazyPagingItems.itemCount == 0) {
                item {
                    Text(
                        stringResource(R.string.no_activities_found),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(lazyPagingItems.itemCount) { index ->
                val activity = lazyPagingItems[index]
                if (activity != null) {
                    ActivityRow(activity = activity, account = account)
                }
            }

            lazyPagingItems.apply {
                when (loadState.refresh) {
                    is LoadState.Loading -> item {
                        Log.d("ActivitiesScreen", "Loading...")
                        CenterCircularProgressIndicator(Modifier.padding(16.dp))
                    }
                    is LoadState.Error -> item {
                        Text("Error loading data", Modifier.padding(16.dp))
                    }
                    is LoadState.NotLoading -> {}
                }
            }
        }
    }
}

@Composable
fun ActivityRow(activity: HistoryEntity, account: Account) {
    val parsedEvent = remember(activity.content) {
        if (activity.content.isBlank()) {
            null
        } else {
            runCatching { AmberEvent.fromJson(activity.content).toEvent() }.getOrNull()
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // App icon
            ApplicationIconWithName(
                key = activity.pkKey,
                account = account,
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // App name + time row
                ApplicationNameRow(
                    key = activity.pkKey,
                    account = account,
                    time = activity.time,
                )

                // Permission type
                Text(
                    text = activity.translatedPermission,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Event content preview (if available)
                if (parsedEvent != null && parsedEvent.content.isNotBlank()) {
                    Text(
                        text = parsedEvent.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else if (activity.content.isNotBlank() && parsedEvent == null) {
                    Text(
                        text = activity.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Status icon
            Icon(
                if (activity.accepted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (activity.accepted) AmberColors.success() else AmberColors.error(),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ApplicationIconWithName(key: String, account: Account) {
    var name by remember { mutableStateOf("") }

    LaunchedEffect(key) {
        launch(Dispatchers.IO) {
            val cacheKey = "${account.npub.toShortenHex()}-$key"
            val cached = ApplicationNameCache.names[cacheKey]
            if (cached != null) {
                name = cached
            } else {
                val app = Amber.instance.getDatabase(account.npub).dao().getByKey(key)
                app?.let {
                    name = it.application.name
                    ApplicationNameCache.names[cacheKey] = it.application.name
                }
            }
        }
    }

    AppIcon(key = key, name = name, size = 36.dp)
}

@Composable
private fun ApplicationNameRow(key: String, account: Account, time: Long) {
    var name by remember { mutableStateOf("") }

    LaunchedEffect(key) {
        launch(Dispatchers.IO) {
            val cacheKey = "${account.npub.toShortenHex()}-$key"
            val cached = ApplicationNameCache.names[cacheKey]
            if (cached != null) {
                name = cached
            } else {
                val app = Amber.instance.getDatabase(account.npub).dao().getByKey(key)
                app?.let {
                    name = it.application.name
                    ApplicationNameCache.names[cacheKey] = it.application.name
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name.ifBlank { key.toShortenHex() },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = TimeUtils.formatLongToCustomDateTimeWithSeconds(time * 1000),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ApplicationName(
    key: String,
    accepted: Boolean,
    account: Account,
) {
    var name by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            if (ApplicationNameCache.names["${account.npub.toShortenHex()}-$key"] == null) {
                val app = Amber.instance.getDatabase(account.npub).dao().getByKey(key)
                app?.let {
                    name = it.application.name
                    ApplicationNameCache.names["${account.npub.toShortenHex()}-$key"] = it.application.name
                }
            } else {
                ApplicationNameCache.names["${account.npub.toShortenHex()}-$key"]?.let {
                    name = it
                }
            }
        }
    }

    Row(
        modifier = Modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppIcon(key = key, name = name, size = 32.dp)
        Text(
            text = name.ifBlank { key.toShortenHex() },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (accepted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}
