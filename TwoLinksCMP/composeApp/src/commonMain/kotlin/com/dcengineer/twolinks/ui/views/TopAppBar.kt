package com.dcengineer.twolinks.ui.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dcengineer.twolinks.model.ViewMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoLinksTopAppBar(
    onEditDimensions: () -> Unit,
    onEditColors: () -> Unit,
    onShuffle: () -> Unit,
    isARAvailable: Boolean = false,
    viewMode: ViewMode = ViewMode.Standard,
    onSetViewMode: (ViewMode) -> Unit = {},
) {
    var arMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Bi Links") },
        actions = {
            if (isARAvailable) {
                IconButton(onClick = { arMenuExpanded = true }) {
                    Icon(Icons.Default.ViewInAr, "View Mode")
                }
                DropdownMenu(
                    expanded = arMenuExpanded,
                    onDismissRequest = { arMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Standard") },
                        leadingIcon = {
                            if (viewMode == ViewMode.Standard) Icon(Icons.Default.Check, null)
                        },
                        onClick = {
                            onSetViewMode(ViewMode.Standard)
                            arMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("AR") },
                        leadingIcon = {
                            if (viewMode == ViewMode.AR) Icon(Icons.Default.Check, null)
                        },
                        onClick = {
                            onSetViewMode(ViewMode.AR)
                            arMenuExpanded = false
                        }
                    )
                }
            }
            IconButton(onClick = onShuffle) {
                Icon(Icons.Default.Shuffle, "Shuffle")
            }
            IconButton(onClick = onEditDimensions) {
                Icon(Icons.Default.Straighten, "Edit Dimensions")
            }
            IconButton(onClick = onEditColors) {
                Icon(Icons.Default.Palette, "Edit Colors")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
