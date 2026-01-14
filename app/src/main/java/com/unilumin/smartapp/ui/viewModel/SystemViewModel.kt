package com.unilumin.smartapp.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.DEVICE_PRODUCT_TYPE_LIST
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.mock.ProductTypeManage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SystemViewModel(
    val retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {

    var configStore = ProductTypeManage(context)


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
}