package com.dcengineer.twolinks.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkColorEditor(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit
) {
    if (isExpanded) {
        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            Text("Color Editor")
        }
    }
}
