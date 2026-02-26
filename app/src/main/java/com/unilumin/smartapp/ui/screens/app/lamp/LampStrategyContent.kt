package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.jobOrStrategyStatusOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.syncStrategyOptions
import com.unilumin.smartapp.client.data.LampStrategyInfo
import com.unilumin.smartapp.client.data.LngLatStrategy
import com.unilumin.smartapp.client.data.TimeStrategy
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.util.JsonUtils
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampStrategyContent(
    lampViewModel: LampViewModel
) {
    val lampStrategyFlow = lampViewModel.lampStrategyFlow.collectAsLazyPagingItems()
    val syncState = lampViewModel.syncState.collectAsState()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.updateSyncState(-1)
    }

    BaseLampListScreen(
        statusOptions = jobOrStrategyStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampStrategyFlow,
        keySelector = { it.id },
        searchTitle = "搜索策略名称或产品名称",
        middleContent = {
            // 单选框组件
            ModernStateSelector(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                options = syncStrategyOptions,
                selectedValue = syncState.value,
                onValueChange = { newValue ->
                    lampViewModel.updateSyncState(newValue)
                })
        }) { item ->
        LampStrategyCard(item = item, onClick = { /* TODO: 跳转详情 */ })
    }
}

//回路策略
fun formatTimeStrategy(contents: List<Any>?): List<TimeStrategy> {
    if (contents.isNullOrEmpty()) return emptyList()
    return contents.mapNotNull { item ->
        try {
            when (item) {
                is String -> if (item.isNotBlank()) JsonUtils.fromJson(
                    item, TimeStrategy::class.java
                ) else null

                is Map<*, *> -> {
                    val jsonString = JsonUtils.gson.toJson(item)
                    JsonUtils.fromJson(jsonString, TimeStrategy::class.java)
                }

                is TimeStrategy -> item
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

//经纬度策略
fun formatLngLatStrategy(contents: List<Any>?): List<LngLatStrategy> {
    if (contents.isNullOrEmpty()) return emptyList()
    return contents.mapNotNull { item ->
        try {
            when (item) {
                is String -> if (item.isNotBlank()) JsonUtils.fromJson(
                    item,
                    LngLatStrategy::class.java
                ) else null

                is Map<*, *> -> {
                    val jsonString = JsonUtils.gson.toJson(item)
                    JsonUtils.fromJson(jsonString, LngLatStrategy::class.java)
                }

                is LngLatStrategy -> item
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun LampStrategyCard(
    item: LampStrategyInfo,
    modifier: Modifier = Modifier,
    onClick: ((LampStrategyInfo) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                StrategyIcon(strategyClass = item.strategyClass)
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name ?: "未命名策略", style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1A1A1A)
                        ), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.productName ?: "未知设备",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isSynced = item.syncState == 1
                    StatusTag(
                        text = if (isSynced) "已同步" else "未同步",
                        color = if (isSynced) BluePrimary else Color(0xFF999999),
                        bgColor = if (isSynced) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val (taskText, taskColor, taskBg) = when (item.taskState) {
                        3 -> Triple("成功", Color(0xFF4CAF50), Color(0xFFE8F5E9))
                        4 -> Triple("失败", Color(0xFFF44336), Color(0xFFFFEBEE))
                        2 -> Triple("执行中", Color(0xFFFFA000), Color(0xFFFFF3E0))
                        else -> Triple("待执行", Color(0xFF999999), Color(0xFFF5F5F5))
                    }
                    StatusTag(text = taskText, color = taskColor, bgColor = taskBg)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            StrategyDataPanel(item)
        }
    }
}

@Composable
private fun LngLatStrategyItem(strategy: LngLatStrategy) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeTag(text = "条件", color = Color(0xFFE3F2FD), textColor = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(8.dp))
            val riseDown = strategy.require.riseDown
            val isSunrise = riseDown.riseType?.toInt() == 1
            val eventName = if (isSunrise) "日出" else "日落"
            val offset = if (isSunrise) {
                riseDown.sunrise.toInt() ?: 0
            } else {
                riseDown.sundown?.toInt() ?: 0
            }
            val offsetText = when {
                offset > 0 -> "延后 $offset 分钟"
                offset < 0 -> "提前 ${abs(offset)} 分钟"
                else -> "准时"
            }
            Text(
                text = "$eventName $offsetText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // --- 2. 执行动作 ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeTag(text = "动作", color = Color(0xFFF1F8E9), textColor = Color(0xFF388E3C))
            Spacer(modifier = Modifier.width(8.dp))
            val actionType = strategy.action.actionType
            val actionValue = strategy.action.actionValue
            val actionDesc = when (actionType) {
                2 -> if (actionValue == 1) "开启" else "关闭"
                else -> "执行动作: $actionValue"
            }
            Text(
                text = actionDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
//        // --- 3. 辅助信息 (经纬度来源提示) ---
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(
//                imageVector = Icons.Outlined.Info,
//                contentDescription = null,
//                modifier = Modifier.size(14.dp),
//                tint = Color(0xFF999999)
//            )
//            Spacer(modifier = Modifier.width(4.dp))
//            val isCustomLngLat = strategy.require.lngLatData.isLngLat?.toInt() == 1
//            Text(
//                text = if (isCustomLngLat) "使用自定义经纬度计算" else "根据设备所在经纬度自动计算",
//                style = MaterialTheme.typography.labelSmall,
//                color = Color(0xFF999999)
//            )
//        }
    }
}

@Composable
fun StrategyIcon(strategyClass: Int?) {
    val isEarth = strategyClass == 1
    val icon = if (isEarth) Icons.Outlined.Public else Icons.Outlined.Schedule

    Surface(
        color = Color(0xFFF2F6FF),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatusTag(text: String, color: Color, bgColor: Color) {
    Surface(
        color = bgColor, shape = RoundedCornerShape(6.dp)
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

@OptIn(ExperimentalLayoutApi::class) // 引入 FlowRow 需要用到此注解
@Composable
fun StrategyDataPanel(item: LampStrategyInfo) {
    Surface(
        color = Color(0xFFF7F8FA),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            // 让内部组件自动保持间距
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isLocation = item.strategyClass == 1
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isLocation) "📍" else "⏱️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLocation) "经纬度策略" else "时间策略",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            if (item.strategyClass == 2) {
                //时间策略
                val strategies = remember(item.contents) {
                    formatTimeStrategy(item.contents)
                }
                if (strategies.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        strategies.forEach { strategy ->
                            TimeStrategyItem(strategy)
                        }
                    }
                }
            } else if (item.strategyClass == 1) {
                //经纬度策略
                val strategies = remember(item.contents) {
                    formatLngLatStrategy(item.contents)
                }
                if (strategies.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        strategies.forEach { strategy ->
                            LngLatStrategyItem(strategy)
                        }
                    }
                }
            }
            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 策略成员(${item.groups!!.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.groups?.forEach { group ->
                        GroupTag(name = group.name ?: "未知分组")
                    }
                }
            }
        }
    }
}


@Composable
private fun TimeStrategyItem(strategy: TimeStrategy) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        val timeScale = strategy.require.timeType
        val week = strategy.require.week
        val days = strategy.require.days
        val requireDes = when (timeScale) {
            1 -> "每天"
            2 -> " 星期$week"
            3 -> "${days?.startTime} 至 ${days?.endTime}"
            else -> ""
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeTag(text = "周期", color = Color(0xFFE3F2FD), textColor = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = requireDes,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeTag(text = "条件", color = Color(0xFFE3F2FD), textColor = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = " ${strategy.require.timePoint ?: "--:--"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 执行动作
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgeTag(text = "动作", color = Color(0xFFF1F8E9), textColor = Color(0xFF388E3C))
            Spacer(modifier = Modifier.width(8.dp))
            val actionType = strategy.action.actionType
            val actionValue = strategy.action.actionValue
            val customize = strategy.action.customize
            val actionDesc = when (actionType) {
                1 -> "调光值: ${actionValue}%"
                2 -> " ${if (actionValue == 1) "开灯" else "关灯"}"
                3 -> "色温值:${actionValue}%"
                5 -> "$customize"
                else -> "执行动作: ${actionValue ?: "未知"}"
            }
            Text(
                text = actionDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun BadgeTag(text: String, color: Color, textColor: Color) {
    Surface(
        color = color, shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 精美的分组标签组件
 */
@Composable
private fun GroupTag(name: String) {
    Surface(
        color = Color(0xFFE8F0FE), // 极浅的灵动蓝背景
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF1967D2), // 深蓝色字体，与背景形成对比
            fontWeight = FontWeight.Medium
        )
    }
}