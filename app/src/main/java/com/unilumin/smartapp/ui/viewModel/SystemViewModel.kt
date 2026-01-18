package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.DEVICE_PRODUCT_TYPE_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_APP_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_FUNC_LIST
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.mock.SystemConfigManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SystemViewModel(
    val retrofitClient: RetrofitClient,application: Application
) : AndroidViewModel(application) {

    val context = getApplication<Application>()

    var configStore = SystemConfigManager(context)


    // 暴露给 UI 的状态流：使用 stateIn 保持热流，确保跨页面感知
    val productTypes: StateFlow<List<SystemConfig>> = configStore.productTypesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEVICE_PRODUCT_TYPE_LIST
        )

    // 切换选中状态
    fun toggleProductType(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            // 基于当前流中的最新值进行修改
            val currentList = productTypes.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            // 写入 DataStore，这会触发 productTypesFlow 发射新值，从而自动更新 UI
            configStore.saveProductTypes(currentList)
        }
    }

    // 暴露给 UI 的状态流：使用 stateIn 保持热流，确保跨页面感知
    val smartApps: StateFlow<List<SystemConfig>> = configStore.smartAppsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SMART_APP_LIST
        )

    // 切换选中状态
    fun toggleSmartApps(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            // 基于当前流中的最新值进行修改
            val currentList = smartApps.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            // 写入 DataStore，这会触发 productTypesFlow 发射新值，从而自动更新 UI
            configStore.saveSmartApps(currentList)
        }
    }


    // 暴露给 UI 的状态流：使用 stateIn 保持热流，确保跨页面感知
    val lampFunctions: StateFlow<List<SystemConfig>> = configStore.lampFunctionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SMART_LAMP_FUNC_LIST
        )

    // 切换选中状态
    fun toggleLampFunctions(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            // 基于当前流中的最新值进行修改
            val currentList = lampFunctions.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            // 写入 DataStore，这会触发 productTypesFlow 发射新值，从而自动更新 UI
            configStore.saveLampFunctions(currentList)
        }
    }


}