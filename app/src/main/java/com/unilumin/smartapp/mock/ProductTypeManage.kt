package com.unilumin.smartapp.mock

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unilumin.smartapp.client.constant.DeviceConstant.DEVICE_PRODUCT_TYPE_LIST
import com.unilumin.smartapp.client.data.SystemConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "device_settings")

class ProductTypeManage(private val context: Context) {

    private val SELECTED_IDS_KEY = stringPreferencesKey("selected_product_ids")

    val productTypesFlow: Flow<List<SystemConfig>> = context.dataStore.data.map { preferences ->
        val savedIdsString = preferences[SELECTED_IDS_KEY]

        if (savedIdsString == null) {
            DEVICE_PRODUCT_TYPE_LIST
        } else {
            val savedIds = savedIdsString.split(",")
                .filter { it.isNotEmpty() }
                .toSet()

            DEVICE_PRODUCT_TYPE_LIST.map { item ->
                item.copy(isSelected = savedIds.contains(item.id))
            }
        }
    }

    // 保存选中的 ID 列表
    suspend fun saveProductTypes(list: List<SystemConfig>) {
        val idsString = list.filter { it.isSelected }
            .joinToString(",") { it.id }

        context.dataStore.edit { preferences ->
            preferences[SELECTED_IDS_KEY] = idsString
        }
    }
}