package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.CenterCircularProgressIndicator
import com.greenart7c3.nostrsigner.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmberNavigationBar(
    items: List<Route>,
    destinationRoute: String,
    onClick: (Route) -> Unit,
    profileUrl: String?,
    account: Account,
) {
    NavigationBar(
        tonalElevation = 0.dp,
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                val selected = destinationRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        onClick(item)
                    },
                    icon = {
                        if (item.route == Route.Accounts.route) {
                            if (!profileUrl.isNullOrBlank() && !BuildFlavorChecker.isOfflineFlavor()) {
                                SubcomposeAsyncImage(
                                    profileUrl,
                                    item.route,
                                    Modifier
                                        .clip(
                                            RoundedCornerShape(50),
                                        )
                                        .height(28.dp)
                                        .width(28.dp),
                                    loading = {
                                        CenterCircularProgressIndicator(Modifier)
                                    },
                                    error = {
                                        Icon(
                                            Icons.Outlined.Person,
                                            item.route,
                                            tint = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color(0xFF555555)
                                            },
                                        )
                                    },
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Person,
                                    item.route,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color(0xFF555555)
                                    },
                                )
                            }
                        } else {
                            Icon(
                                painterResource(item.icon),
                                item.route,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color(0xFF555555)
                                },
                            )
                        }
                    },
                )
            }
        }
    }
}
