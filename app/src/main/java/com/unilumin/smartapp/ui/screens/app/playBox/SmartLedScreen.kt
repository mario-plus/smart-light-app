package com.unilumin.smartapp.ui.screens.app.playBox

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LED_DEV_MANAGE
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_PLAY_BOX
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel

/**
 * 智慧屏幕
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedScreen(
    retrofitClient: RetrofitClient,
    onBack: () -> Unit,
    /**跳转至新页面*/
    toNew: (ScreenViewModel) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, application) as T
        }
    })

    val screenViewModel: ScreenViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScreenViewModel(retrofitClient, application) as T
        }
    })

    val ledFunctions by systemViewModel.ledFunctions.collectAsState()
    var currentFunctionId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(ledFunctions) {
        if (currentFunctionId == null && ledFunctions.isNotEmpty()) {
            currentFunctionId =
                ledFunctions.firstOrNull { it.isSelected }?.id ?: SMART_LED_DEV_MANAGE
        }
    }
    val effectiveId = currentFunctionId ?: SMART_LED_DEV_MANAGE
    val currentTitle = remember(effectiveId, ledFunctions) {
        ledFunctions.find { it.id == effectiveId }?.name ?: getSmartAppName(SMART_PLAY_BOX)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = currentTitle,
                onBack = { onBack() },
                menuItems = ledFunctions,
                onMenuItemClick = { systemConfig ->
                    currentFunctionId = systemConfig.id
                })
        }, containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (effectiveId) {
                SMART_LED_DEV_MANAGE -> {
                    SmartLedDevManage(screenViewModel)
                }
                else -> {
                    EmptyDataView("未开发的功能")
                }
            }
        }
    }
}