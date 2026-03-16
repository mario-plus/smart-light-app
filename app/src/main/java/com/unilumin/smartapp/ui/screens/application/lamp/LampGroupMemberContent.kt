package com.unilumin.smartapp.ui.screens.application.lamp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.GROUP_DEV_REMOVE_TYPE
import com.unilumin.smartapp.client.constant.DeviceConstant.GROUP_FORCE_REMOVE_DEV
import com.unilumin.smartapp.client.constant.DeviceConstant.GROUP_REMOVE_DEV
import com.unilumin.smartapp.client.constant.DeviceConstant.groupDeviceBindOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.client.data.ForceDelGroupDev
import com.unilumin.smartapp.client.data.GroupDevActionType
import com.unilumin.smartapp.client.data.GroupMemberInfo
import com.unilumin.smartapp.client.data.GroupOptDevVO
import com.unilumin.smartapp.client.data.OptGroupDev
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.IconBgBlue
import com.unilumin.smartapp.ui.theme.IconTintBlue
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.PanelBg
import com.unilumin.smartapp.ui.theme.StatusGray
import com.unilumin.smartapp.ui.theme.StatusGreen
import com.unilumin.smartapp.ui.theme.TextSub
import com.unilumin.smartapp.ui.theme.TextTitle
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGroupMemberContent(
    lampViewModel: LampViewModel, onBack: () -> Unit
) {
    val groupMemberFlow = lampViewModel.groupMemberFlow.collectAsLazyPagingItems()

    val groupDevToAddFlow = lampViewModel.groupDevToAddFlow.collectAsLazyPagingItems()

    val bindState = lampViewModel.bindState.collectAsState()

    val currentGroupInfo = lampViewModel.currentGroupInfo.collectAsState()


    var showAddDevBottomSheet by remember { mutableStateOf(false) }
    var showRemoveBottomSheet by remember { mutableStateOf(false) }
    var showForceBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.updateBindState(-1)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = currentGroupInfo.value?.groupName.toString(),
                subTitle = "分组成员",
                menuItems = GROUP_DEV_REMOVE_TYPE,
                onMenuItemClick = { option ->
                    if (option.id == GROUP_REMOVE_DEV) {
                        showRemoveBottomSheet = true
                    } else if (option.id == GROUP_FORCE_REMOVE_DEV) {
                        showForceBottomSheet = true
                    } else {
                        lampViewModel.updateGroupId(currentGroupInfo.value?.id)
                        showAddDevBottomSheet = true
                    }

                },
                onBack = { onBack() })
        }, containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BaseLampListScreen(
                statusOptions = groupDeviceBindOptions,
                viewModel = lampViewModel,
                pagingItems = groupMemberFlow,
                keySelector = { it.deviceId },
                searchTitle = "搜索设备名称或序列码",
                middleContent = {
                    if (currentGroupInfo.value?.groupType != 56) {
                        ModernStateSelector(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            options = statusOptions,
                            selectedValue = bindState.value,
                            onValueChange = { newValue ->
                                lampViewModel.updateBindState(newValue)
                            })
                    }

                }) { item ->
                if (currentGroupInfo.value?.groupType == 56) {
                    LoopControllerCard(item)
                } else {
                    NormalDeviceCard(item)
                }
            }
            if (showAddDevBottomSheet) {
                DeviceActionBottomSheet(
                    viewModel = lampViewModel,
                    groupId = currentGroupInfo.value?.id,
                    onDismiss = {
                        showAddDevBottomSheet = false
                        lampViewModel.updateGroupId(-1L)
                    },
                    availableDevices = groupDevToAddFlow,
                    actionType = GroupDevActionType.ADD,
                    onSuccess = {
                        showAddDevBottomSheet = false
                        lampViewModel.updateGroupId(-1L)
                        groupMemberFlow.refresh()
                    }
                )
            }
            if (showRemoveBottomSheet) {
                DeviceActionBottomSheet(
                    viewModel = lampViewModel,
                    groupId = currentGroupInfo.value?.id,
                    onDismiss = { showRemoveBottomSheet = false },
                    availableDevices = lampViewModel.groupMemberFlow.collectAsLazyPagingItems(),
                    actionType = GroupDevActionType.REMOVE,
                    onSuccess = {
                        showRemoveBottomSheet = false
                        groupMemberFlow.refresh()
                    }
                )
            }

            if (showForceBottomSheet) {
                DeviceActionBottomSheet(
                    viewModel = lampViewModel,
                    groupId = currentGroupInfo.value?.id,
                    onDismiss = { showForceBottomSheet = false },
                    availableDevices = lampViewModel.groupMemberFlow.collectAsLazyPagingItems(),
                    actionType = GroupDevActionType.FORCE_REMOVE,
                    onSuccess = {
                        showForceBottomSheet = false
                        groupMemberFlow.refresh()
                    }
                )
            }


        }
    }
}

/**
 * 样式 1: 回路控制器卡片
 */
@Composable
fun LoopControllerCard(item: GroupMemberInfo) {
    StyledGroupMemberCard(
        title = item.loopCtlName ?: "未知控制器",
        subTitle = "网关: ${item.gwName ?: "--"}",
        iconVector = Icons.Default.DeviceHub, // 使用集线器图标
        onClick = { },
        statusContent = {
            DeviceStatus(
                item.syncState, mapOf(
                    1 to Triple(Color(0xFFE3F2FD), BluePrimary, "已同步"),
                    0 to Triple(Color(0xFFF5F5F5), Color(0xFFBDBDBD), "未同步")
                )
            )
        }) {
        // --- 中间数据面板 ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBg, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                DataFieldItem(
                    label = "回路序号", value = item.loopNum, modifier = Modifier.weight(1f)
                )
                DataFieldItem(
                    label = "回路编号", value = item.loopCode, modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        GroupMemberStatusRow(item.bindState, item.operateState)
    }
}


/**
 * 样式 2: 普通设备卡片（优化版）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NormalDeviceCard(
    item: GroupMemberInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .combinedClickable(onClick = { /* 点击进入详情 */ }, onLongClick = {/*长按*/ }),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFF0F4FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.deviceName ?: "未知设备",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)
                        )
                    )
                    Text(
                        text = "SN: ${item.serialNum ?: "--"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
                DeviceStatus(item.netState)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "加入时间", style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = item.createTime ?: "--", style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF444444),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            GroupMemberStatusRow(item.bindState, item.operateState)
        }
    }
}


/**
 * 优化后的数据项展示（如果 LoopControllerCard 也要改，可以参考这个）
 */
@Composable
fun DataFieldItem(
    label: String, value: String?, modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label, style = TextStyle(fontSize = 12.sp, color = TextSub.copy(alpha = 0.8f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value ?: "--", style = TextStyle(
                fontSize = 14.sp, color = TextTitle, fontWeight = FontWeight.SemiBold // 稍微加粗提高可读性
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StyledGroupMemberCard(
    title: String,
    subTitle: String,
    iconVector: ImageVector,
    onClick: () -> Unit,
    statusContent: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 头部信息 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // 图标容器
                    Surface(
                        color = IconBgBlue,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = IconTintBlue,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title, style = TextStyle(
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, // 稍微调整大小以适应长标题
                                color = TextTitle
                            ), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subTitle,
                            style = TextStyle(fontSize = 13.sp, color = TextSub),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 右上角状态
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    statusContent()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 差异化内容区域 ---
            content()
        }
    }
}


/**
 * @param isActive
 * @param activeText 为true显示的内容
 * @param inactiveText 为false显示的内容
 * @param icon 图标
 */
@Composable
fun StatusIconText(isActive: Boolean, activeText: String, inactiveText: String, icon: ImageVector) {
    val color = if (isActive) StatusGreen else StatusGray
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isActive) activeText else inactiveText,
            style = TextStyle(fontSize = 13.sp, color = color)
        )
    }
}


@Composable
fun GroupMemberStatusRow(
    bindState: Int?, operateState: Int?, modifier: Modifier = Modifier
) {
    val optType = if (operateState == 0) {
        "绑定中"
    } else if (operateState == 1) {
        "绑定失败"
    } else if (operateState == 3) {
        "解绑中"
    } else if (operateState == 4) {
        "解绑失败"
    } else {
        "未知状态"
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 左侧：绑定状态 ---
        StatusIconText(
            isActive = bindState == 1,
            activeText = "已绑定",
            inactiveText = "未绑定",
            icon = if (bindState == 1) Icons.Outlined.Link else Icons.Outlined.LinkOff
        )
        // --- 右侧：操作状态 ---
        StatusIconText(
            isActive = operateState == 2,
            activeText = "绑定成功",
            inactiveText = optType,
            icon = if (operateState == 1) Icons.Outlined.CheckCircle else Icons.Outlined.Info
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceActionBottomSheet(
    viewModel: LampViewModel,
    groupId: Long?,
    availableDevices: LazyPagingItems<out Any>,
    actionType: GroupDevActionType = GroupDevActionType.ADD, // 默认是新增
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedDeviceIds by remember { mutableStateOf(setOf<Long>()) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    // --- 动态文案与颜色 ---
    val titleText = when (actionType) {
        GroupDevActionType.ADD -> "选择要添加的设备"
        GroupDevActionType.REMOVE -> "选择要移除的设备"
        GroupDevActionType.FORCE_REMOVE -> "选择要强制移除的设备"
    }

    val actionText = when (actionType) {
        GroupDevActionType.ADD -> "确定添加"
        GroupDevActionType.REMOVE -> "确定移除"
        GroupDevActionType.FORCE_REMOVE -> "强制移除"
    }

    val actionColor = when (actionType) {
        GroupDevActionType.ADD -> BluePrimary
        GroupDevActionType.REMOVE -> Color(0xFFF57C00) // 警告橙
        GroupDevActionType.FORCE_REMOVE -> Color(0xFFD32F2F) // 危险红
    }

    // --- 每次打开弹窗刷新状态 ---
    LaunchedEffect(Unit) {
        viewModel.updateSearch("")
        selectedDeviceIds = setOf()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8F9FA),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 1. 顶部标题栏 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTitle
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- 2. 搜索框 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFEEEEEE).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearch(it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索设备名称或序列码",
                                    style = TextStyle(fontSize = 15.sp, color = Color.LightGray)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // --- 3. 设备列表 ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    availableDevices.loadState.refresh is LoadState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = actionColor
                        )
                    }

                    availableDevices.loadState.refresh is LoadState.NotLoading && availableDevices.itemCount == 0 -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "暂无相关设备",
                                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(availableDevices.itemCount) { index ->
                                val deviceInfo = availableDevices[index]
                                if (deviceInfo != null) {
                                    val currentDeviceId: Long? = when (deviceInfo) {
                                        is GroupMemberInfo -> deviceInfo.deviceId
                                        is GroupOptDevVO -> deviceInfo.id
                                        else -> null
                                    }

                                    val deviceName: String = when (deviceInfo) {
                                        is GroupMemberInfo -> deviceInfo.deviceName
                                            ?: deviceInfo.loopCtlName

                                        is GroupOptDevVO -> deviceInfo.deviceName
                                            ?: deviceInfo.loopCtlName

                                        else -> null
                                    } ?: "未知设备"

                                    val deviceSn: String = when (deviceInfo) {
                                        is GroupMemberInfo -> deviceInfo.serialNum
                                            ?: deviceInfo.loopCode

                                        is GroupOptDevVO -> deviceInfo.serialNum
                                            ?: deviceInfo.loopCode

                                        else -> null
                                    } ?: "--"

                                    if (currentDeviceId != null) {
                                        val isSelected = selectedDeviceIds.contains(currentDeviceId)
                                        SelectableDeviceItem(
                                            deviceName = deviceName,
                                            deviceSn = "SN: $deviceSn",
                                            isSelected = isSelected,
                                            activeColor = actionColor,
                                            onClick = {
                                                selectedDeviceIds = if (isSelected) {
                                                    selectedDeviceIds - currentDeviceId
                                                } else {
                                                    selectedDeviceIds + currentDeviceId
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            if (availableDevices.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = actionColor,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. 底部操作栏 ---
            Column(
                modifier = Modifier.background(Color.White)
            ) {
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已选择: ${selectedDeviceIds.size} 项",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = if (selectedDeviceIds.isNotEmpty()) actionColor else TextSub,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Button(
                        onClick = {
                            if (selectedDeviceIds.isNotEmpty() && groupId != null) {
                                // 👇 核心分支：根据枚举调用不同接口
                                when (actionType) {
                                    GroupDevActionType.ADD -> {
                                        viewModel.optGroupDev(
                                            optInfo = OptGroupDev(
                                                groupId = groupId,
                                                deviceIds = selectedDeviceIds.toList(),
                                                type = 1
                                            ),
                                            onSuccess = onSuccess
                                        )
                                    }

                                    GroupDevActionType.REMOVE -> {
                                        viewModel.optGroupDev(
                                            optInfo = OptGroupDev(
                                                groupId = groupId,
                                                deviceIds = selectedDeviceIds.toList(),
                                                type = 0
                                            ),
                                            onSuccess = onSuccess
                                        )
                                    }

                                    GroupDevActionType.FORCE_REMOVE -> {
                                        viewModel.forceDelGroupDev(
                                            ForceDelGroupDev(
                                                groupId = groupId,
                                                deviceIds = selectedDeviceIds.toList()
                                            ),
                                            onSuccess = onSuccess
                                        )
                                    }
                                }
                            }
                        },
                        enabled = selectedDeviceIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionColor,
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(actionText, style = TextStyle(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableDeviceItem(
    deviceName: String,
    deviceSn: String,
    isSelected: Boolean,
    activeColor: Color = BluePrimary, // 👈 新增参数：动态主题色
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.05f) else Color.White
        ),
        // 外描边也跟随主题色
        border = if (isSelected) BorderStroke(
            1.dp,
            activeColor.copy(alpha = 0.4f)
        ) else BorderStroke(1.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isSelected) activeColor.copy(alpha = 0.15f) else Color(0xFFF5F7FA),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = if (isSelected) activeColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = deviceSn,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF888888)),
                    maxLines = 1
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = activeColor, // Checkbox 也跟随主题色
                    uncheckedColor = Color.LightGray,
                    checkmarkColor = Color.White
                )
            )
        }
    }
}