package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.unilumin.smartapp.client.data.GroupMemberInfo
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
@Composable
fun NormalDeviceCard(item: GroupMemberInfo) {
    StyledGroupMemberCard(
        title = item.deviceName ?: "未知设备",
        subTitle = "SN: ${item.serialNum ?: "--"}",
        iconVector = Icons.Default.Lightbulb,
        onClick = { },
        statusContent = {
            DeviceStatus(item.netState)
        }) {

        // --- 优化后的中间内容区域 ---
        // 不再使用深色背景块，改用简洁的布局
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // 使用细分割线增加层次感
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(bottom = 12.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 添加一个小图标，让“时间”看起来更精致
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = TextSub.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "加入时间",
                    style = TextStyle(fontSize = 13.sp, color = TextSub)
                )

                Spacer(modifier = Modifier.weight(1f)) // 弹簧效果，将时间推向右侧

                Text(
                    text = item.createTime ?: "--",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = TextTitle.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 底部状态行
        GroupMemberStatusRow(item.bindState, item.operateState)
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
            text = label,
            style = TextStyle(fontSize = 12.sp, color = TextSub.copy(alpha = 0.8f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value ?: "--",
            style = TextStyle(
                fontSize = 14.sp,
                color = TextTitle,
                fontWeight = FontWeight.SemiBold // 稍微加粗提高可读性
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 注意：请确保导入了以下图标（或者直接使用现有图标）
// import androidx.compose.material.icons.outlined.Schedule

// ==========================================
//              基础组件封装
// ==========================================

/**
 * 通用卡片外壳 (复刻 LampLightCard 风格)
 */
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