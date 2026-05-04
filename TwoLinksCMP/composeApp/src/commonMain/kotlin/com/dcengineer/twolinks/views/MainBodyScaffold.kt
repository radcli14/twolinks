package com.dcengineer.twolinks.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dcengineer.twolinks.MainViewModel
import com.dcengineer.twolinks.TwoLinksSceneView

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

        LinkDimensionEditor(viewModel)

        LinkColorEditor(
            isExpanded = viewModel.linkColorEditorIsVisible.value,
            onDismissRequest = viewModel::toggleLinkColorEditor
        )
    }
}