package com.dcengineer.twolinks.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dcengineer.twolinks.MainViewModel
import com.dcengineer.twolinks.model.lengthNorm
import com.dcengineer.twolinks.model.offsetNorm

/**
 * A bottom sheet with five sliders for editing the link and pivot dimensions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDimensionEditor(
    viewModel: MainViewModel
) {
    val state by viewModel.twoLinksState.collectAsState()
    if (viewModel.linkDimensionEditorIsVisible.value) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.linkDimensionEditorIsVisible.value = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "Link Dimension Editor",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                LabeledSlider(
                    label = "First Link Length",
                    value = state.links[0].lengthNorm,
                    onValueChange = viewModel::setLinkOneLengthFromNorm
                )
                LabeledSlider(
                    label = "First Link Offset",
                    value = state.links[0].offsetNorm,
                    onValueChange = viewModel::setLinkOneOffsetFromNorm
                )
                LabeledSlider(
                    label = "Pivot Position",
                    value = state.pivotNorm,
                    onValueChange = viewModel::setPivotFromNorm
                )
                LabeledSlider(
                    label = "Second Link Length",
                    value = state.links[1].lengthNorm,
                    onValueChange = viewModel::setLinkTwoLengthFromNorm
                )
                LabeledSlider(
                    label = "Second Link Offset",
                    value = state.links[1].offsetNorm,
                    onValueChange = viewModel::setLinkTwoOffsetFromNorm
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmallEmphasized
            )
            Slider(
                value = value,
                onValueChange = onValueChange
            )
        }
    }
}