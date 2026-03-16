package com.greenart7c3.nostrsigner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import com.greenart7c3.nostrsigner.ui.components.SignPolicyCard
import com.greenart7c3.nostrsigner.ui.components.globalSignPolicyOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SignPolicySettingsScreen(
    modifier: Modifier = Modifier,
    account: Account,
    navController: NavController,
) {
    var selectedOption by remember { mutableIntStateOf(account.signPolicy) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SignPolicyCard(
                options = globalSignPolicyOptions(),
                selectedOption = selectedOption,
                onSelected = { selectedOption = it },
            )
        }

        item {
            AmberButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        account.signPolicy = selectedOption
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
