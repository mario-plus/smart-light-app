package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LampStrategyInfo
import com.unilumin.smartapp.ui.theme.*
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import com.unilumin.smartapp.client.constant.DeviceConstant.jobOrStrategyStatusOptions
import com.unilumin.smartapp.ui.components.BaseLampListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampStrategyContent(
    lampViewModel: LampViewModel
) {

    // 分页数据
    val lampStrategyFlow = lampViewModel.lampStrategyFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }

    BaseLampListScreen(
        statusOptions = jobOrStrategyStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampStrategyFlow,
        keySelector = { it.id },
        searchTitle = "搜索策略名称或产品名称",
        middleContent = {

        }
    ) { item ->
        LampStrategyCard(item = item, onClick = {})
    }

}


/**
 * 优化后的策略卡片
 * 风格：现代化、紧凑、信息层级分明
 */
@Composable
fun LampStrategyCard(
    item: LampStrategyInfo,
    modifier: Modifier = Modifier,
    onClick: ((LampStrategyInfo) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp) // 卡片间距
            .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // 纯白背景
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 柔和阴影
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 1. 顶部区域：图标 + 标题信息 + 右上角状态 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 左侧图标 (带浅色背景)
                StrategyIcon(strategyClass = item.strategyClass)

                Spacer(modifier = Modifier.width(12.dp))

                // 中间：标题与产品名
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name ?: "未命名策略",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1A1A1A) // 深黑色，强调标题
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.productName ?: "未知设备",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999)), // 浅灰副标题
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 右侧：状态标签组 (紧凑排列)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 同步状态
                    val isSynced = item.syncState == 1
                    StatusTag(
                        text = if (isSynced) "已同步" else "未同步",
                        color = if (isSynced) BluePrimary else Color(0xFF999999),
                        bgColor = if (isSynced) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // 任务状态
                    val (taskText, taskColor, taskBg) = when (item.taskState) {
                        3 -> Triple("成功", Color(0xFF4CAF50), Color(0xFFE8F5E9)) // 绿
                        4 -> Triple("失败", Color(0xFFF44336), Color(0xFFFFEBEE)) // 红
                        2 -> Triple("执行中", Color(0xFFFFA000), Color(0xFFFFF3E0)) // 橙
                        else -> Triple("未知", Color(0xFF999999), Color(0xFFF5F5F5))
                    }
                    StatusTag(text = taskText, color = taskColor, bgColor = taskBg)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. 中间：核心参数面板 (灰色圆角背景) ---
            StrategyDataPanel(item)

            // --- 3. 底部：策略内容详情 ---
            // 优先显示策略内容描述，如果没有则显示备注
            val footerContent = if (!item.contents.isNullOrEmpty()) {
                // 这里假设 contents 是个列表，简单处理转为字符串，实际根据业务逻辑调整
                "策略内容: ${item.executeTime ?: "详见配置"}"
            } else {
                item.executeTime ?: item.description
            }

            if (!footerContent.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description, // 或者用文档图标
                        contentDescription = null,
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = footerContent,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 组件：左侧策略图标
 */
@Composable
private fun StrategyIcon(strategyClass: Int?) {
    // 1:经纬度, 其他:时间
    val isEarth = strategyClass == 1
    val icon = if (isEarth) Icons.Outlined.Public else Icons.Outlined.Schedule
    val bg = Color(0xFFF2F6FF) // 非常浅的蓝色背景
    val tint = BluePrimary

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 组件：精致的状态小标签
 */
@Composable
private fun StatusTag(text: String, color: Color, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 组件：中间灰色数据面板
 */
@Composable
private fun StrategyDataPanel(item: LampStrategyInfo) {
    Surface(
        color = Color(0xFFF7F8FA), // 浅灰背景，提升质感
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly // 均匀分布
        ) {
            // 1. 策略模式
            val modeName = if (item.strategyClass == 1) "经纬度策略" else "时间策略"
            PanelItem(label = "策略模式", value = modeName)

            // 分割线
            PanelDivider()

            // 2. 触发方式
            val triggerName = if (item.executeType == 1) "自动执行" else "手动触发"
            PanelItem(label = "触发方式", value = triggerName)

            // 分割线
            PanelDivider()

            // 3. 成员数 (高亮数字)
            PanelItem(
                label = "成员数",
                value = "${item.groupNum ?: 0}",
                isHighlight = true
            )
        }
    }
}

@Composable
private fun PanelItem(label: String, value: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF999999)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = if (isHighlight) 16.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) BluePrimary else Color(0xFF333333)
        )
    }
}

@Composable
private fun PanelDivider() {
    VerticalDivider(
        modifier = Modifier.height(20.dp),
        thickness = 1.dp,
        color = Color(0xFFE0E0E0)
    )
}