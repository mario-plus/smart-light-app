package com.unilumin.smartapp.mock

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unilumin.smartapp.client.constant.DeviceConstant.DEVICE_PRODUCT_TYPE_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_APP_LIST
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP_FUNC_LIST
import com.unilumin.smartapp.client.data.SystemConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 顶层扩展属性，保持单例模式
private val Context.dataStore by preferencesDataStore(name = "device_settings")
class SystemConfigManager(private val context: Context) {
    companion object {
        // 定义 Keys
        private val KEY_PRODUCT_TYPE_IDS = stringPreferencesKey("selected_product_ids")
        private val KEY_SMART_APP_IDS = stringPreferencesKey("selected_smart_app_ids")
        private val KEY_SMART_LAMP_FUNC = stringPreferencesKey("selected_smart_lamp_func")

    }

    val productTypesFlow: Flow<List<SystemConfig>> = loadConfigFlow(
        key = KEY_PRODUCT_TYPE_IDS,
        defaultList = DEVICE_PRODUCT_TYPE_LIST
    )
    suspend fun saveProductTypes(list: List<SystemConfig>) {
        saveConfig(KEY_PRODUCT_TYPE_IDS, list)
    }

    val smartAppsFlow: Flow<List<SystemConfig>> = loadConfigFlow(
        key = KEY_SMART_APP_IDS,
        defaultList = SMART_APP_LIST // 请替换为你实际的默认列表变量
    )
    suspend fun saveSmartApps(list: List<SystemConfig>) {
        saveConfig(KEY_SMART_APP_IDS, list)
    }


    val lampFunctionsFlow: Flow<List<SystemConfig>> = loadConfigFlow(
        key = KEY_SMART_LAMP_FUNC,
        defaultList =  SMART_LAMP_FUNC_LIST// 请替换为你实际的默认列表变量
    )
    suspend fun saveLampFunctions(list: List<SystemConfig>) {
        saveConfig(KEY_SMART_LAMP_FUNC, list)
    }






    /**
     * 【核心优化】通用的读取逻辑
     * @param key DataStore 的 Key
     * @param defaultList 这是全量的默认列表（包含 ID 和 Name 等信息）
     */
    private fun loadConfigFlow(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        defaultList: List<SystemConfig>
    ): Flow<List<SystemConfig>> {
        return context.dataStore.data.map { preferences ->
            val savedIdsString = preferences[key]
            if (savedIdsString == null) {
                defaultList
            } else {
                val savedIds = savedIdsString.split(",")
                    .filter { it.isNotEmpty() }
                    .toSet()
                defaultList.map { item ->
                    item.copy(isSelected = savedIds.contains(item.id))
                }
            }
        }
    }

    private suspend fun saveConfig(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        list: List<SystemConfig>
    ) {
        val idsString = list.filter { it.isSelected }
            .joinToString(",") { it.id }

        context.dataStore.edit { preferences ->
            preferences[key] = idsString
        }
    }
}