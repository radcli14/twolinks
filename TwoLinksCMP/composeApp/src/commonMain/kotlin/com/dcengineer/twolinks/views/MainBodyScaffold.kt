package com.dcengineer.twolinks.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dcengineer.twolinks.MainViewModel
import com.dcengineer.twolinks.TwoLinksSceneView

/**
 * The primary view content, including the top bar, the 3D scene, and editor sheets
 */
@Composable
fun MainBodyScaffold(
    viewModel: MainViewModel
) {
    Scaffold(
        topBar = {
            TwoLinksTopAppBar(
                onEditDimensions = viewModel::toggleLinkDimensionEditor,
                onEditColors = viewModel::toggleLinkColorEditor
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        // Main content is the scene view, which is overlaid by the play and reset buttons
        Box {
            TwoLinksSceneView(viewModel)
            PlayAndResetButtons(
                modifier = Modifier
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .align(Alignment.BottomCenter),
                isPaused = viewModel.isPaused.value,
                onClickPlayPause = viewModel::pause,
                onClickReset = viewModel::resetStates
            )
        }

        // The editors are modal bottom sheets, with visibility controlled by the view model
        LinkDimensionEditor(viewModel)
        LinkColorEditor(viewModel)
    }
}