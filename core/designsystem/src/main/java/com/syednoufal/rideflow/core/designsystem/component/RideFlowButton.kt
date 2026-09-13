package com.syednoufal.rideflow.core.designsystem.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme

/**
 * RideFlow's primary call-to-action button. Shows a spinner in place of
 * [label] while [isLoading], and disables itself automatically in that state
 * to prevent duplicate taps on slow (simulated or real) network calls.
 */
@Composable
fun RideFlowPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(vertical = RideFlowTheme.spacing.extraSmall)
                    .height(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** A lower-emphasis, outlined counterpart to [RideFlowPrimaryButton], e.g. for "Cancel". */
@Composable
fun RideFlowSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}
