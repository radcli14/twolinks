package com.dcengineer.twolinks

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dcengineer.twolinks.views.MainBodyScaffold
import com.dcengineer.twolinks.ui.theme.TwoLinksTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App(viewModel: MainViewModel = MainViewModel()) {
    TwoLinksTheme {
        MainBodyScaffold(viewModel)
    }
}
