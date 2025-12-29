package com.unilumin.smartapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonParser
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.DeviceConfig
import com.unilumin.smartapp.client.data.DeviceDetail
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.client.service.DeviceService
import com.unilumin.smartapp.ui.components.DetailCard
import com.unilumin.smartapp.ui.components.DetailRow
import com.unilumin.smartapp.ui.components.DeviceTag
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.theme.TextGray

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    lightDevice: LightDevice,
    retrofitClient: RetrofitClient,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val deviceService = remember(retrofitClient) {
        retrofitClient.getService(DeviceService::class.java)
    }

    // 状态管理
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("设备基础信息", "物模型数据")
    var isLoading by remember { mutableStateOf(false) }

    val infoList = remember { mutableStateListOf<Pair<String, String>>() }
    val deviceConfigList = remember { mutableStateListOf<Pair<String, String>>() }
    val deviceServicesKeys = remember { mutableStateListOf<String>() }

    // 数据获取逻辑
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0 && infoList.isEmpty()) {
            isLoading = true
            try {
                val deviceDetail = UniCallbackService<DeviceDetail>().parseDataNewSuspend(
                    deviceService.getDeviceDetail(lightDevice.id), context
                )
                val deviceConfig = UniCallbackService<List<DeviceConfig>>().parseDataNewSuspend(
                    deviceService.getDeviceConfig(lightDevice.id), context
                )

                deviceDetail?.let { detail ->
                    infoList.clear()
                    detail.productFactoryName?.let { infoList.add("产品厂商" to it) }
                    detail.deviceName?.let { infoList.add("设备名称" to it) }
                    detail.serialNum?.let { infoList.add("序列码" to it) }
                    detail.productTypeName?.let { infoList.add("产品类型" to it) }
                    detail.name?.let { infoList.add("产品名称" to it) }
                    detail.transportProtocol?.let { infoList.add("传输协议" to it) }
                    detail.messageProtocol?.let { infoList.add("协议名称" to it) }

                    detail.metadata?.let { metadataStr ->
                        try {
                            val jsonObject = JsonParser().parse(metadataStr).asJsonObject
                            deviceServicesKeys.clear()
                            jsonObject.getAsJsonArray("services")?.forEach { element ->
                                deviceServicesKeys.add(element.asJsonObject.get("name").asString)
                            }
                        } catch (e: Exception) { Log.e("DeviceDetail", "Metadata parse error", e) }
                    }
                }
                deviceConfig?.let { configs ->
                    deviceConfigList.clear()
                    configs.forEach { deviceConfigList.add(it.keyDes to it.value) }
                }
            } catch (e: Exception) {
                Log.e("DeviceDetail", "Data fetch error", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) { // 顶部轻微投影
                Column(modifier = Modifier.background(CardWhite)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "${lightDevice.name}-详情",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "返回",
                                    tint = TextDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        // 背景设为透明，依靠 Surface 的颜色，避免双重背景色差
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )

                    // 现代化 TabRow
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = ControlBlue,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .padding(horizontal = 36.dp)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                                    height = 3.dp,
                                    color = ControlBlue
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) ControlBlue else TextGray.copy(alpha = 0.8f)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = PageBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ControlBlue)
            } else {
                SelectionContainer {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (selectedTabIndex == 0) {
                            item {
                                DetailCard(title = "基础信息") {
                                    infoList.forEach { (key, value) ->
                                        DetailRow(key, value)
                                    }
                                }
                            }
                            if (deviceConfigList.isNotEmpty()) {
                                item {
                                    DetailCard(title = "设备配置信息") {
                                        deviceConfigList.forEach { (key, value) ->
                                            DetailRow(key, value)
                                        }
                                    }
                                }
                            }
                            if (deviceServicesKeys.isNotEmpty()) {
                                item {
                                    DetailCard(title = "设备功能") {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            deviceServicesKeys.forEach { name ->
                                                DeviceTag(name)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("暂无物模型数据", color = TextGray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}