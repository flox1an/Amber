package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable sign policy radio card.
 * Accepts a list of options so it can be used for both
 * app-specific (3 options) and global settings (2 options).
 */
@Composable
fun SignPolicyCard(
    options: List<TitleExplainer>,
    selectedOption: Int,
    onSelected: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            options.forEachIndexed { index, option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedOption == index,
                            onClick = {
                                onSelected(index)
                            },
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedOption == index,
                        onClick = {
                            onSelected(index)
                        },
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        option.explainer?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (index < options.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    }
}

/**
 * App-specific sign policy chooser (3 options: basic, manual for new apps, fully manual).
 */
@Composable
fun ChooseSignPolicy(
    selectedOption: Int,
    onSelected: (Int) -> Unit,
) {
    SignPolicyCard(
        options = appSignPolicyOptions(),
        selectedOption = selectedOption,
        onSelected = onSelected,
    )
}

@Composable
fun appSignPolicyOptions() = listOf(
    TitleExplainer(
        title = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_basic),
        explainer = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_basic_explainer),
    ),
    TitleExplainer(
        title = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_manual_new_app),
        explainer = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_manual_new_app_explainer),
    ),
    TitleExplainer(
        title = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_fully),
        explainer = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_fully_explainer),
    ),
)

@Composable
fun globalSignPolicyOptions() = listOf(
    TitleExplainer(
        title = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_basic),
        explainer = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_basic_explainer),
    ),
    TitleExplainer(
        title = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_manual),
        explainer = androidx.compose.ui.res.stringResource(com.greenart7c3.nostrsigner.R.string.sign_policy_manual_explainer),
    ),
)
