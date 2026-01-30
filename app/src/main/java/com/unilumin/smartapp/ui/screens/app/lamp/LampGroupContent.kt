package com.unilumin.smartapp.ui.screens.app.lamp

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.twotone.GridView
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupTypeOptions
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.screens.dialog.DeviceControlDialog
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.CardBgColor
import com.unilumin.smartapp.ui.theme.DataPanelBgColor
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.DividerGrey
import com.unilumin.smartapp.ui.theme.IconBgColor
import com.unilumin.smartapp.ui.theme.TextMain
import com.unilumin.smartapp.ui.theme.TextSub
import com.unilumin.smartapp.ui.theme.ThemeBlue
import com.unilumin.smartapp.ui.viewModel.LampViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGroupContent(
    lampViewModel: LampViewModel, toNew: (LampViewModel) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }
    val lampGroupFlow = lampViewModel.lampGroupFlow.collectAsLazyPagingItems()
    val currentGroup = lampViewModel.currentGroupInfo.collectAsState()

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
        }) { item ->
        LampGroupCard(item = item, onClick = { e ->
            lampViewModel.updateCurrentGroupInfo(e)
            showDialog = true
        }, onMemberClick = { e ->
            lampViewModel.updateCurrentGroupInfo(e)
            toNew(lampViewModel)
        })
    }
    //分组控制
    if (showDialog) {
        DeviceControlDialog(
            productId = currentGroup.value?.productId?.toString() ?: "",
            deviceName = currentGroup.value?.groupName ?: "未知分组",
            initialBrightness = 0,
            initColorT = 0,
            onDismiss = {
                lampViewModel.updateCurrentGroupInfo(null)
                showDialog = false
            },
            onClick = { a, b ->
                lampViewModel.lampCtl(currentGroup.value?.id ?: 0L, a, b)
                lampViewModel.updateCurrentGroupInfo(null)
                showDialog = false
            }
        )
    }


}

@Composable
fun LampGroupCard(
    item: LampGroupInfo,
    modifier: Modifier = Modifier,
    onClick: ((LampGroupInfo) -> Unit)? = null,
    onMemberClick: ((LampGroupInfo) -> Unit)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor), // 确保 CardBgColor 已定义，通常是 White
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
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
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)
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
                            text = item.groupName ?: "未命名分组", style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextMain // 深黑
                            ), maxLines = 1, overflow = TextOverflow.Ellipsis
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
                DeviceStatus(
                    item.syncState, mapOf(
                        1 to Triple(Color(0xFFE3F2FD), BluePrimary, "已同步"),
                        0 to Triple(Color(0xFFF5F5F5), Color(0xFFBDBDBD), "未同步")
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GroupInfoPanel(item, onClick = {
                onMemberClick(item)
            })

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
fun GroupInfoPanel(item: LampGroupInfo, onClick: ((LampGroupInfo) -> Unit)) {
    Surface(
        color = DataPanelBgColor,
        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val typeName = when (item.groupType) {
                1 -> "单灯控制器"
                25 -> "集中控制器"
                56 -> "回路控制器"
                else -> "未知类型"
            }
            InfoColumnItem(
                label = "分组类型", value = typeName, modifier = Modifier.weight(1.2f)
            )
            VerticalDivider(
                modifier = Modifier.height(24.dp), color = DividerGrey
            )
            if (item.groupType != 1) {
                InfoColumnItem(
                    label = "所属集控",
                    value = item.deviceName ?: "--",
                    modifier = Modifier.weight(1.2f)
                )
                VerticalDivider(
                    modifier = Modifier.height(24.dp), color = DividerGrey
                )
            }

            InfoColumnItem(
                label = "成员数",
                value = "${item.deviceNum ?: 0}",
                isHighlight = true,
                modifier = Modifier.weight(0.8f),
                onClick = { onClick(item) })
        }
    }
}

@Composable
fun InfoColumnItem(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label, fontSize = 11.sp, color = TextSub
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

