package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LedDevGroupRes
import com.unilumin.smartapp.client.data.LedGroupMemberUpdate
import com.unilumin.smartapp.client.data.PlayBoxDeviceBO
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedGroupManage(
    screenViewModel: ScreenViewModel, onGroupClick: () -> Unit = {}
) {
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()

    // 已关联成员
    val ledGroupDevMember by screenViewModel.ledGroupDevMember.collectAsState()
    // 未关联成员
    val ledGroupDevOptional by screenViewModel.ledGroupDevOptional.collectAsState()

    val selectLedGroup by screenViewModel.selectLedGroup.collectAsState()

    val ledGroupPagingFlow = screenViewModel.ledGroupPagingFlow.collectAsLazyPagingItems()

    var showEditSheet by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchHeader(
                searchQuery = searchQuery,
                searchTitle = "搜索分组名称或备注信息",
                onSearchChanged = { screenViewModel.updateSearch(it) })

            PagingList(
                totalCount = totalCount,
                lazyPagingItems = ledGroupPagingFlow,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                emptyMessage = "暂无分组信息",
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) { ledGroup ->
                ledGroup?.let {
                    LedGroupCard(
                        ledGroup = ledGroup,
                        onClick = {
                            screenViewModel.getLedGroupFunc(ledGroup, onSuccess = {
                                onGroupClick()
                            })
                        },
                        onEditDevsClick = { groupRes ->
                            screenViewModel.editGroupMember(groupRes, onSuccess = {
                                showEditSheet = true
                            })
                        }
                    )
                }
            }
        }

        if (showEditSheet) {
            EditMembersBottomSheet(
                linkedMembers = ledGroupDevMember ?: emptyList(),
                unlinkedMembers = ledGroupDevOptional ?: emptyList(),
                onDismiss = {
                    showEditSheet = false
                },
                onSubmit = { selectedIds ->
                    if (selectLedGroup != null) {
                        val request = LedGroupMemberUpdate(
                            deviceIds = selectedIds.toList(),
                            groupId = selectLedGroup!!.id
                        )
                        screenViewModel.updateLedGroupMember(request, onSuccess = {
                            showEditSheet = false
                            ledGroupPagingFlow.refresh()
                        })
                    }
                }
            )
        }
    }
}

/**
 * --- 现代化底部弹窗编辑组件 ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMembersBottomSheet(
    linkedMembers: List<PlayBoxDeviceBO>,
    unlinkedMembers: List<PlayBoxDeviceBO>,
    onDismiss: () -> Unit,
    onSubmit: (Set<Long>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("已关联成员", "未关联成员")
    var selectedIds by remember {
        mutableStateOf(linkedMembers.map { it.id }.toSet())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp) // 给定一个合理的高度
        ) {
            // 标题区
            Text(
                text = "关联播放盒",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Tab 切换栏
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // 列表内容区 (权重 1f，让出底部按钮空间)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val currentList = if (selectedTabIndex == 0) linkedMembers else unlinkedMembers

                if (currentList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无数据",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    items(
                        items = currentList,
                        key = { it.id }
                    ) { device ->
                        val isSelected = selectedIds.contains(device.id)
                        DeviceSelectionItem(
                            device = device,
                            isSelected = isSelected,
                            onToggle = { checked ->
                                selectedIds = if (checked) {
                                    selectedIds + device.id
                                } else {
                                    selectedIds - device.id
                                }
                            }
                        )
                    }
                }
            }

            // 底部悬浮操作区
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onSubmit(selectedIds) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("完成选定 (${selectedIds.size})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * --- 现代化列表选中项 UI ---
 */
@Composable
fun DeviceSelectionItem(
    device: PlayBoxDeviceBO,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        label = "bgColorAnim"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else Color.Transparent,
        label = "borderColorAnim"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(!isSelected) },
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "未知设备",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SN: ${device.serialNum ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "未选中",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 分组信息卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedGroupCard(
    ledGroup: LedDevGroupRes,
    onClick: () -> Unit,
    onEditDevsClick: (LedDevGroupRes) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp, pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ledGroup.name ?: "未命名分组",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                DeviceStatus(
                    ledGroup.groupState, mapOf(
                        0 to Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "正常"),
                        1 to Triple(Color(0xFFF5F5F5), Color(0xFF9E9E9E), "异常")
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    InfoRowItem(
                        icon = Icons.Rounded.Devices,
                        label = "产品",
                        value = ledGroup.productName ?: ledGroup.primaryClassName ?: "-"
                    )
                    val creatorInfo =
                        "${ledGroup.createName ?: "-"}  ·  ${ledGroup.createTime ?: "-"}"
                    InfoRowItem(
                        icon = Icons.Rounded.AccountCircle, label = "创建", value = creatorInfo
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GroupDevsSection(
                devs = ledGroup.groupDevs,
                onEditClick = { onEditDevsClick(ledGroup) } // 抛出
            )
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupDevsSection(
    devs: List<PlayBoxDeviceBO>?, onEditClick: () -> Unit
) {
    val safeDevs = devs ?: emptyList()
    Column(modifier = Modifier.fillMaxWidth()) {

        // 标题与行内编辑图标 (左对齐排列)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Dns,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "分组成员 (${safeDevs.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.width(4.dp)) // 文字和图标之间留出微小的间距

            // 极简的行内编辑图标按钮
            Box(
                modifier = Modifier
                    .clip(CircleShape) // 确保点击水波纹是圆形的，更精致
                    .clickable(onClick = onEditClick)
                    .padding(6.dp), // 增加内边距，扩大触控区域，同时让水波纹有扩散空间
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "编辑成员",
                    modifier = Modifier.size(14.dp), // 尺寸与文字字号保持协调
                    // 使用主题色并增加一点透明度，既能暗示可点击，又不会刺眼
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 下方设备列表部分保持不变
        if (safeDevs.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val maxDisplayCount = 6
                val displayDevs = safeDevs.take(maxDisplayCount)
                displayDevs.forEach { dev ->
                    DeviceChip(name = dev.name ?: "未知设备", isMoreCounter = false)
                }
                if (safeDevs.size > maxDisplayCount) {
                    val remainCount = safeDevs.size - maxDisplayCount
                    DeviceChip(name = "+$remainCount", isMoreCounter = true)
                }
            }
        } else {
            Text(
                text = "暂未关联播放盒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 22.dp)
            )
        }
    }
}
/**
 * 单个设备微型标签
 */
@Composable
private fun DeviceChip(name: String, isMoreCounter: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isMoreCounter) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isMoreCounter) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isMoreCounter) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}