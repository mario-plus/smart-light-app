package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.twotone.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupTypeOptions
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.theme.*
import com.unilumin.smartapp.ui.viewModel.LampViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGroupContent(
    lampViewModel: LampViewModel
) {

    val lampGroupFlow = lampViewModel.lampGroupFlow.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }

    BaseLampListScreen(
        statusOptions = groupTypeOptions,
        viewModel = lampViewModel,
        pagingItems = lampGroupFlow,
        keySelector = { it.id },
        searchTitle = "搜索分组名称或产品名称",
        middleContent = {

        }
    ) { item ->
        LampGroupCard(item = item, onClick = {})
    }

}

/**
 * 分组信息卡片
 * 风格参考 LampGatewayCard
 */
@Composable
fun LampGroupCard(
    item: LampGroupInfo,
    modifier: Modifier = Modifier,
    onClick: ((LampGroupInfo) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor), // 确保 CardBgColor 已定义，通常是 White
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 1. 头部：图标 + 名称/产品 + 状态 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // 图标容器
                    Surface(
                        color = IconBgColor, // 浅色背景，如 Color(0xFFE3F2FD)
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            // 使用 GridView 或 Folder 图标代表分组
                            imageVector = Icons.TwoTone.GridView,
                            contentDescription = "Group Icon",
                            tint = ThemeBlue, // 主题色
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // 分组名称
                        Text(
                            text = item.groupName ?: "未命名分组",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextMain // 深黑
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 产品名称
                        Text(
                            text = item.productName ?: "--",
                            style = TextStyle(fontSize = 12.sp, color = TextSub), // 浅灰
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 同步状态标签
                SyncStatusChip(state = item.syncState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. 核心数据面板 (仿 GatewayRealTimeDataPanel) ---
            GroupInfoPanel(item)

            // --- 3. 底部备注 (如果有) ---
            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = TextSub,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.description ?: "",
                        fontSize = 12.sp,
                        color = TextSub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 分组核心信息面板
 * 展示：分组类型、集控名称、成员数量
 */
@Composable
fun GroupInfoPanel(item: LampGroupInfo) {
    Surface(
        color = DataPanelBgColor, // 浅灰背景，如 Color(0xFFF7F8FA)
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 分组类型
            // 映射类型：1->单灯控制器, 25->集控, 56->回路
            val typeName = when (item.groupType) {
                1 -> "单灯控制器"
                25 -> "集中控制器"
                56 -> "回路控制器"
                else -> "未知类型"
            }
            InfoColumnItem(
                label = "分组类型",
                value = typeName,
                icon = Icons.Outlined.Folder,
                modifier = Modifier.weight(1.2f)
            )

            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = DividerGrey
            )

            // 2. 所属集控 (如果为空则显示--)
            InfoColumnItem(
                label = "所属集控",
                value = item.deviceName ?: "--",
                icon = Icons.Outlined.Hub,
                modifier = Modifier.weight(1.2f)
            )

            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = DividerGrey
            )

            // 3. 成员数量 (突出显示)
            InfoColumnItem(
                label = "成员数",
                value = "${item.deviceNum ?: 0}",
                icon = Icons.Outlined.Devices,
                isHighlight = true,
                modifier = Modifier.weight(0.8f)
            )
        }
    }
}

@Composable
fun InfoColumnItem(
    label: String,
    value: String,
    icon: ImageVector,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSub
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = if (isHighlight) 16.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) ThemeBlue else TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SyncStatusChip(state: Int?) {
    // 假设 1 代表已同步
    val isSynced = state == 1
    val bgColor = if (isSynced) Color(0xFFE3F2FD) else Color(0xFFF5F5F5) // 浅蓝 vs 浅灰
    val dotColor = if (isSynced) BluePrimary else Color(0xFFBDBDBD) // 蓝点 vs 灰点
    val textColor = if (isSynced) BluePrimary else Color(0xFF757575)
    val text = if (isSynced) "已同步" else "未同步"

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}
