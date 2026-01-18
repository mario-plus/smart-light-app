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
fun LampStrategyContent(
    lampViewModel: LampViewModel
) {

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
    val lampStrategyFlow = lampViewModel.lampStrategyFlow.collectAsLazyPagingItems()

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
            lazyPagingItems = lampStrategyFlow,
            forceLoading = isSwitching.value,
            modifier = Modifier.weight(1f),
            itemKey = { gatewayInfo -> gatewayInfo.id },
            emptyMessage = "未找到相关设备",
            contentPadding = PaddingValues(top = 0.dp, bottom = 24.dp, start = 0.dp, end = 0.dp)
        ) { gatewayInfo ->


        }
    }
}





