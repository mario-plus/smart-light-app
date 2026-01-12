package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.constant.DeviceConstant.DeviceMenus
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchBar
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    retrofitClient: RetrofitClient,
    onDetailClick: (IotDevice) -> Unit,
    onMenuClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val menuShape = RoundedCornerShape(16.dp)
    val context = LocalContext.current

    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, context) as T
        }
    })


    //产品类型选择
    val activeFilter by deviceViewModel.currentFilter.collectAsState()

    val totalCount = deviceViewModel.totalCount.collectAsState()

    //搜索条件
    val searchQuery by deviceViewModel.searchQuery.collectAsState()

    //分页数据
    val lazyPagingItems = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()

    //条件
    var lastSyncedParams by remember { mutableStateOf(Pair(activeFilter, searchQuery)) }

    //是否存在切换动作
    val isSwitching = lastSyncedParams != Pair(activeFilter, searchQuery)

    LaunchedEffect(lazyPagingItems.loadState.refresh) {
        if (lazyPagingItems.loadState.refresh is LoadState.NotLoading ||
            lazyPagingItems.loadState.refresh is LoadState.Error
        ) {
            lastSyncedParams = Pair(activeFilter, searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        Surface(
            color = White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "设备列表",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.FilterList, null, tint = Gray500)

                        MaterialTheme(shapes = Shapes(extraSmall = menuShape)) {
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .shadow(
                                        12.dp,
                                        menuShape,
                                        spotColor = Color.Black.copy(alpha = 0.2f)
                                    )
                                    .clip(menuShape)
                                    .background(Color.White)
                                    .border(0.5.dp, Color(0xFFF0F0F0), menuShape)
                            ) {
                                DeviceMenus.forEach { option ->
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Settings,
                                                null,
                                                Modifier.size(20.dp),
                                                Color.Gray
                                            )
                                        },
                                        text = {
                                            Text(
                                                option.second,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        onClick = { showMenu = false; onMenuClick() }
                                    )
                                }
                            }
                        }
                    }
                }

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { deviceViewModel.updateSearch(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DeviceConstant.DataList) { (id, label) ->
                        FilterChip(
                            label = label,
                            isActive = activeFilter == id,
                            onClick = { deviceViewModel.updateFilter(id) }
                        )
                    }
                }
            }
        }
        // 分页列表展示
        PagingList(
            totalCount = totalCount.value,
            lazyPagingItems = lazyPagingItems,
            forceLoading = isSwitching, // 传入切换状态
            modifier = Modifier.weight(1f),
            itemKey = { device -> device.id },
            emptyMessage = "未找到相关设备",
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
        ) { device ->
            DeviceCardItem(
                deviceViewModel = deviceViewModel,
                iotDevice = device,
                type = activeFilter,
                onDetailClick = { onDetailClick(device) }
            )
        }
    }
}
