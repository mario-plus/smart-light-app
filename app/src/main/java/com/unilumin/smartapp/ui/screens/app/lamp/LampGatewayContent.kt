package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LampGateWayInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow

import com.unilumin.smartapp.ui.theme.*

import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGatewayContent(
    lampViewModel: LampViewModel
) {

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }
    val gateWayFlow = lampViewModel.lampGateWayFlow.collectAsLazyPagingItems()
    BaseLampListScreen(
        viewModel = lampViewModel,
        pagingItems = gateWayFlow,
        keySelector = { it.id },
        searchTitle = "搜索设备名称或序列码"
    ) { item ->
        LampGatewayCard(item = item, onDetailClick = {})
    }
}


/**
 * 集控器列表卡片
 */
@Composable
fun LampGatewayCard(
    item: LampGateWayInfo,
    onDetailClick: (LampGateWayInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // 与搜索框对齐，微调垂直间距
            .clickable { onDetailClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
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
                    // 图标容器 (使用集控器/Hub图标)
                    Surface(
                        color = IconBgColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Router, // 或者 Icons.Default.DeviceHub
                            contentDescription = "Gateway Icon",
                            tint = ThemeBlue,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.name ?: "未知设备",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextMain
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SN: ${item.serialNum ?: "--"}",
                            style = TextStyle(fontSize = 13.sp, color = TextSub)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.productName ?: "--",
                            style = TextStyle(fontSize = 13.sp, color = TextSub)
                        )
                    }
                }

                // 在线/离线 状态标签
                DeviceStatus(item.state)
            }

            Spacer(modifier = Modifier.height(16.dp))
            // --- 第二部分：实时参数面板 (三相电压/电流) ---
            GatewayRealTimeDataPanel(item)
            Spacer(modifier = Modifier.height(16.dp))
            //TODO 缺少告警字段
            DeviceStatusRow(
                isDisable = item.alarmType == 0,
                hasAlarm = item.alarmType == 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 实时数据面板：展示三相电压和电流
 */
@Composable
fun GatewayRealTimeDataPanel(item: LampGateWayInfo) {
    Surface(
        color = DataPanelBgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()) // 支持横向滚动以防数据过长
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 数据项列表：标题 - 值 - 单位
            val dataItems = listOf(
                Triple("A相电压", item.voltage1, "V"),
                Triple("B相电压", item.voltage2, "V"),
                Triple("C相电压", item.voltage3, "V"),
                Triple("A相电流", item.current1, "A"),
                Triple("B相电流", item.current2, "A"),
                Triple("C相电流", item.current3, "A")
            )

            dataItems.forEachIndexed { index, (label, value, unit) ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = TextSub
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value?.let { "$it$unit" } ?: "--",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }

                // 分割线 (最后一项不显示)
                if (index < dataItems.size - 1) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = DividerGrey
                    )
                }
            }
        }
    }
}





