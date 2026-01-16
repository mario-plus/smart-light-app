package com.unilumin.smartapp.ui.screens.app.lamp

import android.service.controls.DeviceTypes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchBar
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.PageBgColor
import com.unilumin.smartapp.ui.theme.PlaceholderColor
import com.unilumin.smartapp.ui.theme.SearchBarBg

import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampLightContent(
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
    val lampLightFlow = lampViewModel.lampLightFlow.collectAsLazyPagingItems()

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

        // --- 分页列表展示 ---
        // 移除原来的 Spacer(modifier = Modifier.width(8.dp))，用 padding 控制
        PagingList(
            totalCount = totalCount.value,
            lazyPagingItems = lampLightFlow,
            forceLoading = isSwitching.value,
            modifier = Modifier.weight(1f),
            itemKey = { lampLightInfo -> lampLightInfo.id },
            emptyMessage = "未找到相关设备",
            // 调整 Padding，让列表内容不顶着搜索框
            contentPadding = PaddingValues(top = 0.dp, bottom = 24.dp, start = 0.dp, end = 0.dp)
        ) { lampLightInfo ->
            // 调用之前优化好的卡片
            LampLightCard(
                item = lampLightInfo,
                onDetailClick = { /* 点击事件 */ }

            )
        }
    }
}


@Composable
fun LampLightCard(
    item: LampLightInfo,
    onDetailClick: (LampLightInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp) // 减小外部缩进，增加屏幕利用率
            .clickable { onDetailClick(item) }, // 增加点击反馈
        shape = RoundedCornerShape(16.dp), // 更圆润的角，符合现代审美
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 略微增加阴影增强层次
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
                    // 图标容器
                    Surface(
                        color = Color(0xFFEBF2FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF2F78FF),
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
                                color = Color(0xFF333333)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SN: ${item.serialNum ?: "--"}",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.productName ?: "--",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999))
                        )
                    }
                }

                // 在线/离线 状态标签
                DeviceStatus(item.state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 第二部分：实时参数面板 (参考图片中的灰色块) ---
            LampRealTimeDataPanel(item)

            Spacer(modifier = Modifier.height(16.dp))


            DeviceStatusRow(
                isDisable = item.deviceState == 0,
                hasAlarm = item.alarmType == 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LampRealTimeDataPanel(item: LampLightInfo) {
    Surface(
        color = Color(0xFFF7F8FA), // 浅灰色背景分区
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dataItems = mutableListOf(
                "开关" to if (item.onOff == 1) "开" else "关",
                "亮度" to "${item.bright1?.toInt() ?: "--"}%"
            )
            if (DeviceConstant.colorTempSupportedList.contains(item.productId.toString())) {
                dataItems.add("色温" to "${item.bright2?.toInt() ?: "--"}%")
            }
            dataItems.add("电压" to (item.voltage?.let { "${it}V" } ?: "--"))
            dataItems.add("电流" to (item.current?.let { "${it}mA" } ?: "--"))
            dataItems.add("功率" to (item.power?.let { "${it}W" } ?: "--"))


            dataItems.forEachIndexed { index, pair ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = pair.first, fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pair.second,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pair.first == "开关" || pair.first == "亮度") Color(0xFF2F78FF) else Color(
                            0xFF333333
                        )
                    )
                }
                // 只有不是最后一项时才显示分割线
                if (index < dataItems.size - 1) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}


