package com.unilumin.smartapp.ui.screens.app.playBox

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.client.data.LedPageBO
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedDevManage(
    screenViewModel: ScreenViewModel
) {
    val deviceState by screenViewModel.state.collectAsState()
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val envDevicePagingFlow = screenViewModel.ledDevPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchHeader(
            statusOptions = statusOptions,
            currentStatus = deviceState,
            searchQuery = searchQuery,
            searchTitle = "搜索设备名称或序列码",
            onStatusChanged = { screenViewModel.updateState(it) },
            onSearchChanged = { screenViewModel.updateSearch(it) })

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = envDevicePagingFlow,
            itemKey = { it.id },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
            emptyMessage = "暂无播放盒设备",
            contentPadding = PaddingValues(bottom = 16.dp)
        ) { ledInfo ->
            LedPlayBoxCard(
                item = ledInfo, onDetailClick = { clickedItem ->
                })
        }
    }
}

@Composable
fun LedPlayBoxCard(
    item: LedPageBO, onDetailClick: (LedPageBO) -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 5.dp)
            .clickable { onDetailClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 第一部分：头部基本信息 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // 图标容器 (使用类似电视/屏幕的图标代表播放盒)
                    Surface(
                        color = Color(0xFFEBF2FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = Color(0xFF2F78FF),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.deviceName ?: item.name ?: "未知设备", style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF333333)
                            ), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SN: ${item.serialNum ?: "--"}",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.productName ?: "--",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999)),
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }

                // 在线/离线 状态标签
                DeviceStatus(item.state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 第二部分：实时参数面板 ---
            LedRealTimeDataPanel(item)

            Spacer(modifier = Modifier.height(16.dp))

            // --- 第三部分：告警/状态底栏 ---
            DeviceStatusRow(
                isDisable = false, // 如果 LedPageBO 有启用/禁用字段可以替换
                hasAlarm = item.alarmType == 1, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LedRealTimeDataPanel(item: LedPageBO) {
    Surface(
        color = Color(0xFFF7F8FA), // 浅灰色背景分区
        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // --- 第一部分：横向滚动的核心短参数 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dataItems = mutableListOf<Pair<String, String>>()
                dataItems.add("运行状态" to if (item.powerStatus == "1") "唤醒" else "休眠")
                val brightnessStr = if (item.brightness != null) {
                    if (item.autoBrightness == true) "${item.brightness}% (自动)" else "${item.brightness}%"
                } else "--"
                dataItems.add("亮度" to brightnessStr)
                dataItems.add("音量" to (item.volume?.let { "$it%" } ?: "--"))
                dataItems.add("分辨率" to (item.widthHeighProgram ?: "--"))
                dataItems.forEachIndexed { index, pair ->
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = pair.first, fontSize = 12.sp, color = Color(0xFF999999))
                        Spacer(modifier = Modifier.height(4.dp))
                        val valueColor = if (pair.first == "电源" || pair.first == "亮度") {
                            Color(0xFF2F78FF)
                        } else {
                            Color(0xFF333333)
                        }
                        Text(
                            text = pair.second,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = valueColor,
                            maxLines = 1
                        )
                    }
                    if (index < dataItems.size - 1) {
                        VerticalDivider(
                            modifier = Modifier.height(24.dp),
                            thickness = 1.dp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            item.playingProgramName?.let { e ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "当前节目:",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = e.toString(),
                            fontSize = 12.sp,
                            color = Color(0xFF333333),
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee()
                        )
                    }
                }
            }
        }
    }
}