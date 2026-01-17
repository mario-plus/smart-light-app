package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.LampGateWayInfo
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.components.PagingList

import com.unilumin.smartapp.ui.theme.*

import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGatewayContent(
    retrofitClient: RetrofitClient
) {
    val context = LocalContext.current
    val lampViewModel: LampViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LampViewModel(retrofitClient, context) as T
        }
    })
    val statusOptions = remember {
        listOf(-1 to "全部状态", 1 to "设备在线", 0 to "设备离线")
    }
    // 设备状态 (-1:全部, 0:离线, 1:在线)
    val deviceState by lampViewModel.state.collectAsState()
    // 搜索条件
    val searchQuery by lampViewModel.searchQuery.collectAsState()
    // 状态下拉框控制
    var statusExpanded by remember { mutableStateOf(false) }
    val totalCount = lampViewModel.totalCount.collectAsState()
    val isSwitching = lampViewModel.isSwitch.collectAsState()

    // 分页数据
    val gateWayFlow = lampViewModel.lampGateWayFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBgColor) // 整个页面使用浅灰底色
    ) {
        // --- 顶部搜索区域 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // 外部间距
        ) {
            // 一体化搜索容器
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // 稍微加高一点，便于点击
                shape = RoundedCornerShape(26.dp), // 全圆角胶囊样式
                color = SearchBarBg,
                shadowElevation = 3.dp // 添加柔和阴影，营造悬浮感
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 左侧：状态筛选下拉
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .clickable { statusExpanded = true }
                            .padding(start = 16.dp, end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = statusOptions.find { it.first == deviceState }?.second?.replace(
                                    "设备",
                                    ""
                                ) ?: "全部",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // 下拉菜单
                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            statusOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (value == deviceState) BluePrimary else Color(
                                                0xFF333333
                                            ),
                                            fontWeight = if (value == deviceState) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        lampViewModel.updateState(value)
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    // 2. 中间：竖向分割线
                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = DividerColor
                    )
                    // 3. 右侧：自定义纯净搜索框
                    // 这里不使用 bulky 的 SearchBar，而是用 BasicTextField 自定义
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索设备名称或地址...",
                                    color = PlaceholderColor,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { lampViewModel.updateSearch(it) },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.Black
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(BluePrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        PagingList(
            totalCount = totalCount.value,
            lazyPagingItems = gateWayFlow,
            forceLoading = isSwitching.value,
            modifier = Modifier.weight(1f),
            itemKey = { gatewayInfo -> gatewayInfo.id },
            emptyMessage = "未找到相关设备",
            contentPadding = PaddingValues(top = 0.dp, bottom = 24.dp, start = 0.dp, end = 0.dp)
        ) { gatewayInfo ->

            LampGatewayCard(item = gatewayInfo, onDetailClick = {})
        }
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

/**
 * 底部信息栏：告警状态、绑定统计、白名单状态
 */
@Composable
fun GatewayFooterInfo(item: LampGateWayInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：绑定统计信息
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "已绑: ${item.boundDevCount ?: 0}",
                fontSize = 12.sp,
                color = TextSub
            )
            Spacer(modifier = Modifier.width(8.dp))
            VerticalDivider(modifier = Modifier.height(10.dp), color = DividerGrey)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "未绑: ${item.unboundDevCount ?: 0}",
                fontSize = 12.sp,
                color = TextSub
            )
        }

        // 右侧：状态标签组合
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 1. 白名单状态
            // 0未同步 1同步中 2已同步 3同步失败
            val (whiteListText, whiteListColor) = when (item.whiteListState) {
                1 -> "同步中" to ThemeBlue
                2 -> "已同步" to SuccessGreen
                3 -> "同步失败" to ErrorRed
                else -> "未同步" to TextSub
            }

            Surface(
                color = whiteListColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = whiteListText,
                    color = whiteListColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // 2. 告警状态 (如果有告警才显示)
            if (item.alarmType == 1) {
                Surface(
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "告警",
                            color = ErrorRed,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 在线/离线 状态标签组件
 */
@Composable
fun GatewayStatusBadge(state: Int?) {
    val isOnline = state == 1
    val bgColor = if (isOnline) SuccessGreen.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
    val textColor = if (isOnline) SuccessGreen else Color(0xFF999999)
    val text = if (isOnline) "在线" else "离线"

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}



