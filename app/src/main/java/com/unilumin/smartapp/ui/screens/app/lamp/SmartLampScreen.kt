package com.unilumin.smartapp.ui.screens.app.lamp

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
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_GATEWAY
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_GROUP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_JOB
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_LIGHT
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_LOOP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_STRATEGY
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LIGHT_GATEWAY
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.EmptyDataView
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLampScreen(
    retrofitClient: RetrofitClient,
    onBack: () -> Unit,
    toNew: (LampViewModel) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, application) as T
        }
    })

    val lampViewModel: LampViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LampViewModel(retrofitClient, application) as T
        }
    })

    val lampFunctions by systemViewModel.lampFunctions.collectAsState()

    var currentFunctionId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(lampFunctions) {
        if (currentFunctionId == null && lampFunctions.isNotEmpty()) {
            currentFunctionId = lampFunctions.firstOrNull { it.isSelected }?.id ?: SMART_LAMP_LIGHT
        }
    }


    val effectiveId = currentFunctionId ?: SMART_LAMP_LIGHT

    // 动态获取当前页面的标题
    val currentTitle = remember(effectiveId, lampFunctions) {
        lampFunctions.find { it.id == effectiveId }?.name ?: getSmartAppName(SMART_LAMP)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(title = currentTitle,
                onBack = { onBack() },
                menuItems = lampFunctions,
                onMenuItemClick = { systemConfig ->
                    // 更新 String 类型的 ID
                    currentFunctionId = systemConfig.id
                })
        }, containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 根据 String 类型的 effectiveId 判断
            when (effectiveId) {
                // 单灯管理页面
                SMART_LAMP_LIGHT -> {
                    LampLightContent(lampViewModel)
                }
                // 集中控制器
                SMART_LAMP_GATEWAY -> {
                    LampGatewayContent(lampViewModel)
                }
                // 回路控制器
                SMART_LAMP_LOOP -> {
                    LampLoopCtlContent(lampViewModel)
                }

                SMART_LIGHT_GATEWAY -> {
                    LampLightGwContent(lampViewModel)
                }

                // 分组管理
                SMART_LAMP_GROUP -> {
                    LampGroupContent(lampViewModel, toNew = { toNew(lampViewModel) })
                }

                SMART_LAMP_STRATEGY -> {
                    LampStrategyContent(lampViewModel)
                }

                SMART_LAMP_JOB -> {
                    LampJobContent(lampViewModel)
                }

                else -> {
                    EmptyDataView("未开发的功能")
                }
            }
        }
    }
}