package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.R

@Composable
fun NewBunkerFloatingButton(
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(end = 8.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                focusedElevation = 6.dp,
                hoveredElevation = 8.dp,
                pressedElevation = 12.dp,
            ),
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.connect_app),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
