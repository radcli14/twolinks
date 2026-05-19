package com.dcengineer.twolinks.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dcengineer.twolinks.MainViewModel
import dev.romainguy.kotlin.math.Float4
import kotlin.random.Random
import kotlin.time.Clock

private data class ShuffleState(
    val link1Length: Float,
    val link1Offset: Float,
    val pivotNorm: Float,
    val link2Length: Float,
    val link2Offset: Float,
    val link1Color: Float4,
    val link2Color: Float4
)

private fun generateShuffleState(): ShuffleState {
    val random = Random(Clock.System.now().toEpochMilliseconds())
    return ShuffleState(
        link1Length = random.nextFloat(),
        link1Offset = random.nextFloat(),
        pivotNorm = random.nextFloat(),
        link2Length = random.nextFloat(),
        link2Offset = random.nextFloat(),
        link1Color = Float4(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f),
        link2Color = Float4(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuffleDialog(viewModel: MainViewModel) {
    if (!viewModel.shuffleDialogIsVisible.value) return

    var shuffleState by remember { mutableStateOf(generateShuffleState()) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun dismiss() {
        scope.launch {
            sheetState.hide()
            viewModel.shuffleDialogIsVisible.value = false
        }
    }

    val animSpec = tween<Float>(durationMillis = 500)
    val link1Length by animateFloatAsState(shuffleState.link1Length, animSpec)
    val link1Offset by animateFloatAsState(shuffleState.link1Offset, animSpec)
    val pivotNorm   by animateFloatAsState(shuffleState.pivotNorm,   animSpec)
    val link2Length by animateFloatAsState(shuffleState.link2Length, animSpec)
    val link2Offset by animateFloatAsState(shuffleState.link2Offset, animSpec)
    val colorSpec = tween<Color>(durationMillis = 500)
    val link1Color  by animateColorAsState(Color(shuffleState.link1Color.x, shuffleState.link1Color.y, shuffleState.link1Color.z), colorSpec)
    val link2Color  by animateColorAsState(Color(shuffleState.link2Color.x, shuffleState.link2Color.y, shuffleState.link2Color.z), colorSpec)

    ModalBottomSheet(
        onDismissRequest = { viewModel.shuffleDialogIsVisible.value = false },
        sheetState = sheetState
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shuffle",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { shuffleState = generateShuffleState() }) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Reshuffle")
                }
            }

            // Dimension bars
            DimensionBar(link1Length)
            DimensionBar(link1Offset)
            DimensionBar(pivotNorm)
            DimensionBar(link2Length)
            DimensionBar(link2Offset)

            // Color swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSwatch(link1Color, Modifier.weight(1f))
                ColorSwatch(link2Color, Modifier.weight(1f))
            }

            // Cancel / Confirm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { dismiss() },
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                Button(
                    onClick = {
                        viewModel.setLinkOneLengthFromNorm(shuffleState.link1Length)
                        viewModel.setLinkOneOffsetFromNorm(shuffleState.link1Offset)
                        viewModel.setPivotFromNorm(shuffleState.pivotNorm)
                        viewModel.setLinkTwoLengthFromNorm(shuffleState.link2Length)
                        viewModel.setLinkTwoOffsetFromNorm(shuffleState.link2Offset)
                        viewModel.setLinkOneColor(shuffleState.link1Color)
                        viewModel.setLinkTwoColor(shuffleState.link2Color)
                        dismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Confirm") }
            }
        }
    }
}

@Composable
private fun DimensionBar(value: Float) {
    LinearProgressIndicator(
        progress = { value },
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(MaterialTheme.shapes.small)
    )
}

@Composable
private fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = MaterialTheme.shapes.medium,
        color = color
    ) {}
}
