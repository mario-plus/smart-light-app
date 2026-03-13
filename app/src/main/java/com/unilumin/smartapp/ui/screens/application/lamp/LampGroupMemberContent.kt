package com.unilumin.smartapp.ui.screens.application.lamp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupDeviceBindOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.client.data.ForceDelGroupDev
import com.unilumin.smartapp.client.data.GroupMemberInfo
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
    val bindState = lampViewModel.bindState.collectAsState()

    val currentGroupInfo = lampViewModel.currentGroupInfo.collectAsState()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.updateBindState(-1)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "分组成员", onBack = { onBack() })
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
                onAddClick = {
                    //增加分组成员，加载分组成员，然后勾选
                },
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
                    NormalDeviceCard(item, onRemove = {
                        lampViewModel.optGroupDev(
                            OptGroupDev(
                                groupId = currentGroupInfo.value?.id ?: 0,
                                deviceIds = listOf(item.deviceId),
                                type = 0
                            )
                        )
                        groupMemberFlow.refresh()
                    }, onForceRemove = {
                        lampViewModel.forceDelGroupDev(
                            ForceDelGroupDev(
                                groupId = currentGroupInfo.value?.id ?: 0,
                                deviceIds = listOf(item.deviceId),
                            )
                        )
                        groupMemberFlow.refresh()
                    })
                }

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
    item: GroupMemberInfo, onRemove: () -> Unit, onForceRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .combinedClickable(onClick = { /* 点击进入详情 */ }, onLongClick = {
                showMenu = true
            }),
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
    if (showMenu) {
        DeviceActionBottomSheet(
            onDismiss = { showMenu = false }, onRemove = onRemove, onForceRemove = onForceRemove
        )
    }
}


/**
 * 优化后的数据项展示（如果 LoopControllerCard 也要改，可以参考这个）
 */
@Composable
fun DataFieldItem(
    label: String, value: String?, modifier: Modifier = Modifier, isFullWidth: Boolean = false
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
    onDismiss: () -> Unit, onRemove: () -> Unit, onForceRemove: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }, // 顶部的横条
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp) // 底部留出安全距离
        ) {
            // 选项 1: 普通移除
            ActionItem(
                icon = Icons.Outlined.Delete,
                title = "移除设备",
                subTitle = "从当前分组中正常移除该设备",
                onClick = {
                    onRemove()
                    onDismiss()
                })
            // 选项 2: 强制移除 (强调危险/警示)
            ActionItem(
                icon = Icons.Outlined.WarningAmber,
                title = "强制移除",
                subTitle = "当设备离线无法响应时使用，直接清除记录",
                titleColor = Color(0xFFD32F2F), // 红色警示
                onClick = {
                    onForceRemove()
                    onDismiss()
                })
            Spacer(modifier = Modifier.height(8.dp))
            // 取消按钮
            TextButton(
                onClick = onDismiss, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("取消", color = Color.Gray)
            }
        }
    }
}

/**
 * 底部弹窗内的单个功能行
 */
@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    subTitle: String,
    titleColor: Color = Color(0xFF1A1A1A),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == Color(0xFF1A1A1A)) Color.Gray else titleColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title, style = TextStyle(
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = titleColor
                )
            )
            Text(
                text = subTitle, style = TextStyle(
                    fontSize = 12.sp, color = Color.Gray
                )
            )
        }
    }
}