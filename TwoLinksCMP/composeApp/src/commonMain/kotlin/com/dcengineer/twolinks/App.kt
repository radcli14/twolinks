package com.dcengineer.twolinks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App(viewModel: MainViewModel = MainViewModel()) {
    val elapsedTime by viewModel.elapsedTimeState.collectAsState()
    val state by viewModel.twoLinksState.collectAsState()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bi Links") },
                    actions = {
                        IconButton(onClick = viewModel::pause) {
                            Icon(if (viewModel.isPaused.value) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = "Pause")
                        }
                        IconButton(onClick = viewModel::resetStates) {
                            Icon(Icons.Default.Refresh, contentDescription = "Pause")
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar {
                    Text("Elapsed: $elapsedTime")
                    //Text("Theta0: ${state.links[0].theta}")
                    //Text("Theta1: ${state.links[1].theta}")
                }
            }
        ) {
            TwoLinksSceneView(viewModel)
        }
    }
}