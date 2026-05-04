package com.dcengineer.twolinks.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dcengineer.twolinks.MainViewModel
import com.dcengineer.twolinks.model.Link
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorChangeSource
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.HueSlider
import com.github.skydoves.colorpicker.compose.SaturationSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.romainguy.kotlin.math.Float4


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkColorEditor(
    viewModel: MainViewModel
) {
    val state by viewModel.twoLinksState.collectAsState()

    if (viewModel.linkColorEditorIsVisible.value) {
        ModalBottomSheet(onDismissRequest = { viewModel.linkColorEditorIsVisible.value = false }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "Link Color Editor",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SingleLinkColorEditor(
                        name = "First Link",
                        link = state.links[0],
                        modifier = Modifier.weight(1f),
                        onColorChanged = viewModel::setLinkOneColor
                    )
                    SingleLinkColorEditor(
                        name = "Second Link",
                        link = state.links[1],
                        modifier = Modifier.weight(1f),
                        onColorChanged = viewModel::setLinkTwoColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SingleLinkColorEditor(
    name: String,
    link: Link,
    modifier: Modifier = Modifier,
    onColorChanged: (Float4) -> Unit
) {
    val controller = rememberColorPickerController()
    LaunchedEffect(Unit) {
        controller.selectByColor(link.color.asColor, fromUser = true)
    }

    val sliderModifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
        .height(35.dp)

    Surface(
        modifier = modifier.requiredHeightIn(max = 256.dp),
        shape = MaterialTheme.shapes.large,
        color = controller.selectedColor.value.copy(alpha = 0.628f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = controller.selectedColor.value.contrastColor
            )
            HsvColorPicker(
                modifier = Modifier.weight(1f, fill = false),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    onColorChanged(colorEnvelope.color.asFloat4)
                },
            )

            SaturationSlider(sliderModifier, controller, initialColor = link.color.asColor)
            BrightnessSlider(sliderModifier, controller, initialColor = link.color.asColor)
        }
    }
}

private val Color.asFloat4: Float4
    get() = Float4(red, green, blue, alpha)

private val Color.inverse: Color
    get() = Color(red = 1f - red, green = 1f - green, blue = 1f - blue)

// Standard formula for relative luminance
private val Color.luminance: Float get() = 0.2126f * red + 0.7152f * green + 0.0722f * blue

// Helper to determine if a color is "dark" or "light"
private val Color.contrastColor: Color get() = if (luminance > 0.5f) Color.Black else Color.White

private val Float4.asColor: Color
    get() = Color(red = x, green = y, blue = z, alpha = w)