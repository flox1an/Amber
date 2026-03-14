package com.greenart7c3.nostrsigner.ui.components

import android.view.WindowManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.greenart7c3.nostrsigner.Amber

@Composable
fun RandomPinInput(
    text: String? = null,
    onPinEntered: (String) -> Unit,
) {
    val randomNumbers = remember { (0..9).shuffled() }
    var selectedPin by remember { mutableStateOf("") }
    var obscureText by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Amber.instance.getMainActivity()?.window?.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE,
                    )
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Amber.instance.getMainActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Amber.instance.getMainActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            text?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (!obscureText) selectedPin else selectedPin.map { '*' }.joinToString(""),
                )
            }

            IconButton(
                onClick = { obscureText = !obscureText },
            ) {
                Icon(
                    imageVector = if (obscureText) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Show/Hide",
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(randomNumbers.dropLast(1)) { _, number ->
                Button(
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        if (selectedPin.length < 6) {
                            selectedPin += number.toString()
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(
                        number.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.scale(1.50f),
                    )
                }
            }

            item {
                Button(
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        if (selectedPin.isNotEmpty()) {
                            selectedPin = selectedPin.dropLast(1) // Remove last digit
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Erase",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            item {
                Button(
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        if (selectedPin.length < 6) {
                            selectedPin += randomNumbers.last().toString()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                ) {
                    Text(
                        randomNumbers.last().toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.scale(1.50f),
                    )
                }
            }

            item {
                Button(
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedPin.length >= 4,
                    onClick = {
                        if (selectedPin.length >= 4) {
                            onPinEntered(selectedPin) // Confirm the PIN
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = "Done",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}
