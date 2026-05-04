package com.dcengineer.twolinks.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * A big round play button, with reset button offset and to its right, at the bottom of the screen
 */
@Composable
fun PlayAndResetButtons(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    onClickPlayPause: () -> Unit,
    onClickReset: () -> Unit
) {
    Box(
        modifier.width(196.dp).padding(bottom = 16.dp)
    ) {
        FilledIconButton(
            modifier = Modifier.size(96.dp).align(Alignment.Center),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onClick = onClickPlayPause
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = "Pause",
                modifier = Modifier.size(56.dp)
            )
        }
        FilledIconButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            onClick = onClickReset
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Pause")
        }
    }
}
