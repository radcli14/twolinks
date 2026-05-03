package com.dcengineer.twolinks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App(viewModel: MainViewModel = MainViewModel()) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Color.Blue)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bi Links") },
                    actions = {
                    }
                )
            }
        ) {
            Box {
                TwoLinksSceneView(viewModel)
                PlayAndResetButtons(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isPaused = viewModel.isPaused.value,
                    onClickPlayPause = viewModel::pause,
                    onClickReset = viewModel::resetStates
                )
            }
        }
    }
}

@Composable
fun PlayAndResetButtons(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    onClickPlayPause: () -> Unit,
    onClickReset: () -> Unit
) {
    Box(
        modifier.width(196.dp).padding(bottom = 32.dp)
    ) {
        FilledIconButton(
            modifier = Modifier.size(96.dp).align(Alignment.Center),
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
                containerColor = MaterialTheme.colorScheme.primaryContainer, // Or MaterialTheme.colorScheme.surfaceVariant for a themed gray
                //contentColor = Color.White    // Ensures the refresh icon is visible
            ),
            onClick = onClickReset
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Pause")
        }
    }
}