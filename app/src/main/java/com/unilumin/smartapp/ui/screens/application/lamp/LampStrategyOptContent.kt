package com.unilumin.smartapp.ui.screens.application.lamp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unilumin.smartapp.client.constant.DeviceConstant.GROUP_DEV_REMOVE_TYPE
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampStrategyOptContent(
    lampViewModel: LampViewModel, onBack: () -> Unit
) {

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "新建策略",
                menuItems = GROUP_DEV_REMOVE_TYPE,
                onMenuItemClick = { option ->
                },
                onBack = { onBack() })
        }, containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {}
    }
}
