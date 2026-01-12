package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.DeviceMenus
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.client.data.ProductType
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchBar
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DevicesScreen(
    retrofitClient: RetrofitClient,
    profileViewModel: ProfileViewModel,
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

    val productTypes by profileViewModel.productTypes.collectAsState()
    val activeTypes = remember(productTypes) { productTypes.filter { it.isSelected } }

    //产品类型选择
    val productType by deviceViewModel.productType.collectAsState()
    val totalCount = deviceViewModel.totalCount.collectAsState()

    //搜索条件
    val searchQuery by deviceViewModel.searchQuery.collectAsState()

    //分页数据
    val lazyPagingItems = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()

    //条件
    var lastSyncedParams by remember { mutableStateOf(Pair(productType, searchQuery)) }

    //是否存在切换动作
    val isSwitching = lastSyncedParams != Pair(productType, searchQuery)

    LaunchedEffect(lazyPagingItems.loadState.refresh) {
        if (lazyPagingItems.loadState.refresh is LoadState.NotLoading || lazyPagingItems.loadState.refresh is LoadState.Error) {
            lastSyncedParams = Pair(productType, searchQuery)
        }
    }

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
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
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

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.FilterList, null, tint = Gray500)

                        MaterialTheme(shapes = Shapes(extraSmall = menuShape)) {
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .shadow(
                                        12.dp, menuShape, spotColor = Color.Black.copy(alpha = 0.2f)
                                    )
                                    .clip(menuShape)
                                    .background(Color.White)
                                    .border(0.5.dp, Color(0xFFF0F0F0), menuShape)
                            ) {
                                DeviceMenus.forEach { option ->
                                    DropdownMenuItem(leadingIcon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            null,
                                            Modifier.size(20.dp),
                                            Color.Gray
                                        )
                                    }, text = {
                                        Text(
                                            option.second,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }, onClick = { showMenu = false; onMenuClick() })
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

                Spacer(modifier = Modifier.height(8.dp))

                // 筛选区域
                DeviceFilterSection(
                    activeTypes = activeTypes,
                    selectedId = productType,
                    onSelect = { id -> deviceViewModel.updateFilter(id) }
                )
            }
        }

        // 分页列表展示
        PagingList(
            totalCount = totalCount.value,
            lazyPagingItems = lazyPagingItems,
            forceLoading = isSwitching,
            modifier = Modifier.weight(1f),
            itemKey = { device -> device.id },
            emptyMessage = "未找到相关设备",
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
        ) { device ->
            DeviceCardItem(
                deviceViewModel = deviceViewModel,
                iotDevice = device,
                productType = productType,
                onDetailClick = { onDetailClick(device) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceFilterSection(
    activeTypes: List<ProductType>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 【修改点 1】创建 LazyList 的状态对象，用于控制滚动
    val listState = rememberLazyListState()

    // 【修改点 2】监听选中项或展开状态的变化
    // 当选中项改变，且当前处于收起状态（或刚刚收起）时，滚动到对应位置
    LaunchedEffect(selectedId, isExpanded) {
        if (!isExpanded) {
            val index = activeTypes.indexOfFirst { it.id == selectedId }
            if (index >= 0) {
                // 平滑滚动到指定位置，并且稍微偏移一点(可选)，保证选中项完全可见
                listState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        AnimatedContent(targetState = isExpanded, label = "FilterAnimation") { expanded ->
            if (expanded) {
                // --- 展开状态 ---
                Column(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 头部：标题 + 收起按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "所有类型",
                            fontSize = 13.sp,
                            color = Gray500,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Blue600)
                        }
                    }

                    // 网格容器
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        maxItemsInEachRow = 4
                    ) {
                        activeTypes.forEach { type ->
                            GridFilterItem(
                                type = type,
                                isSelected = type.id == selectedId,
                                onClick = {
                                    onSelect(type.id)
                                    isExpanded = false // 选中后自动收起
                                }
                            )
                        }
                    }
                }
            } else {
                // --- 收起状态 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        // 【修改点 3】绑定状态，使滚动生效
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeTypes) { type ->
                            FilterChip(
                                label = type.name,
                                isActive = type.id == selectedId,
                                onClick = { onSelect(type.id) }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clickable { isExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "展开",
                            tint = Gray500,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 网格单项组件 (确保内部内容居中)
 */
@Composable
fun GridFilterItem(
    type: ProductType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        // 【关键修改 3】水平居中对齐图标和文字
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp) // 给定固定宽度，确保网格整齐
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isSelected) Blue600 else Gray100
        ) {
            // 图标在圆圈内居中
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) Color.White else Gray900.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = type.name,
            fontSize = 12.sp,
            maxLines = 1,
            // 文字居中
            textAlign = TextAlign.Center,
            color = if (isSelected) Blue600 else Gray900,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
