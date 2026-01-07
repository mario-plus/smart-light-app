package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceType
import com.unilumin.smartapp.client.constant.DeviceType.DeviceMenus
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchBar
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel

// --- Main Screen ---
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    retrofitClient: RetrofitClient,
    onDetailClick: (LightDevice) -> Unit,
    onMenuClick: () -> Unit
) {
    // 1. 状态管理
    var showMenu by remember { mutableStateOf(false) }

    val menuShape = RoundedCornerShape(16.dp)

    val context = LocalContext.current

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, context) as T
        }
    })

    val activeFilter by deviceViewModel.currentFilter.collectAsState()
    val searchQuery by deviceViewModel.searchQuery.collectAsState()

    /**
     * 设备列表
     * */
    val lazyPagingItems = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {

        Surface(
            color = White, shadowElevation = 2.dp, modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                // 1. 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "设备列表", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900
                    )
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Gray200),
                        color = White,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                showMenu = true
                            }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.FilterList,
                                null,
                                tint = Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        MaterialTheme(
                            // 关键：通过主题统一修改 DropdownMenu 的形状
                            shapes = Shapes(extraSmall = menuShape)
                        ) {
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    // 1. 先设置阴影。ambientColor 可以让阴影更淡更高级
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = menuShape,
                                        spotColor = Color.Black.copy(alpha = 0.2f)
                                    )
                                    // 2. 必须裁剪，保证内容不溢出圆角
                                    .clip(menuShape)
                                    // 3. 背景色建议带一点点透明度或纯白
                                    .background(Color.White)
                                    // 4. 去掉那个灰色的 border，或者改用极浅的颜色
                                    .border(0.5.dp, Color(0xFFF0F0F0), menuShape)
                            ) {
                                DeviceMenus.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.Gray
                                            )
                                        },
                                        text = {
                                            Text(
                                                option.second,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        onClick = {
                                            onMenuClick()
                                        },
                                        // 增加点击区域的内边距，看起来更舒展
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 12.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. 搜索框
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { deviceViewModel.updateSearch(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. 筛选 Tabs
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DeviceType.DataList) { (id, label) ->
                        val isActive = activeFilter == id
                        FilterChip(
                            label = label,
                            isActive = isActive,
                            onClick = { deviceViewModel.updateFilter(id) })
                    }
                }
            }
        }

        PagingList(
            lazyPagingItems = lazyPagingItems,
            modifier = Modifier.weight(1f),
            itemKey = { device -> device.id },
            emptyMessage = "未找到相关设备",
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
        ) { device ->
            DeviceCardItem(
                deviceViewModel = deviceViewModel,
                lightDevice = device,
                type = deviceViewModel.currentFilter.value,
                onDetailClick = { onDetailClick(device) }
            )
        }
    }

}
