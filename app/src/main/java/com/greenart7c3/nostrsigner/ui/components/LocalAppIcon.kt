package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun LocalAppIcon(packageName: String?) {
    packageName?.let {
        val appDisplayInfo = rememberAppDisplayInfo(packageName)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (appDisplayInfo.icon != null) {
                Image(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MaterialTheme.shapes.small),
                    bitmap = appDisplayInfo.icon.toBitmap().asImageBitmap(),
                    contentDescription = appDisplayInfo.name,
                    contentScale = ContentScale.Crop,
                )
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = appDisplayInfo.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (packageName != appDisplayInfo.name) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
