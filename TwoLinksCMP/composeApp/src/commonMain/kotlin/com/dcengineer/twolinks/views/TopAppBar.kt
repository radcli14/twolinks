package com.dcengineer.twolinks.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoLinksTopAppBar(
    onEditDimensions: () -> Unit,
    onEditColors: () -> Unit
) {
    TopAppBar(
        title = { Text("Bi Links") },
        actions = {
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
