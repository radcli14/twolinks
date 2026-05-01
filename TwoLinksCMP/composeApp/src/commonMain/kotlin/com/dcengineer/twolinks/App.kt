package com.dcengineer.twolinks

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(viewModel: MainViewModel = MainViewModel()) {
    val elapsedTime by viewModel.elapsedTimeState.collectAsState()
    val state by viewModel.twoLinksState.collectAsState()

    MaterialTheme {
        Scaffold(
            bottomBar = {
                BottomAppBar {
                    Text("Elapsed: $elapsedTime")
                    Text("Theta0: ${state.links[0].theta}")
                    Text("Theta1: ${state.links[1].theta}")
                }
            }
        ) {
            TwoLinksSceneView(viewModel)
        }
    }
}