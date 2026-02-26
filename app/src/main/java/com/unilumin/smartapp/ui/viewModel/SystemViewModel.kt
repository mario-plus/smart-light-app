package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.DEVICE_PRODUCT_TYPE_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.ENV_PRODUCT_TYPE_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_APP_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_FUNC_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LED_FUNC_LIST
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.mock.SystemConfigManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SystemViewModel(
    val retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {

    val context = getApplication<Application>()

    var configStore = SystemConfigManager(context)

    /***********************************************************************************************************************************************/
    val productTypes: StateFlow<List<SystemConfig>> = configStore.productTypesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DEVICE_PRODUCT_TYPE_LIST
    )

    // 切换选中状态
    fun toggleProductType(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentList = productTypes.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            configStore.saveProductTypes(currentList)
        }
    }

    /***********************************************************************************************************************************************/
    val smartApps: StateFlow<List<SystemConfig>> = configStore.smartAppsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SMART_APP_LIST
    )

    // 切换选中状态
    fun toggleSmartApps(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentList = smartApps.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            configStore.saveSmartApps(currentList)
        }
    }


    /***********************************************************************************************************************************************/
    val lampFunctions: StateFlow<List<SystemConfig>> = configStore.lampFunctionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SMART_LAMP_FUNC_LIST
    )
    fun toggleLampFunctions(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentList = lampFunctions.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            configStore.saveLampFunctions(currentList)
        }
    }


    /***********************************************************************************************************************************************/
    val envProductTypeList: StateFlow<List<SystemConfig>> =
        configStore.envProductTypeListFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ENV_PRODUCT_TYPE_LIST
        )
    fun toggleEnvProductTypeList(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentList = envProductTypeList.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            configStore.saveEnvProductTypeIds(currentList)
        }
    }


    /***********************************************************************************************************************************************/
    val ledFunctions: StateFlow<List<SystemConfig>> = configStore.ledFunctionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SMART_LED_FUNC_LIST
    )
    fun toggleLedFunctions(id: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentList = ledFunctions.value.map {
                if (it.id == id) it.copy(isSelected = isSelected) else it
            }
            configStore.saveLedFunctions(currentList)
        }
    }



}