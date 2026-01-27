package com.unilumin.smartapp.ui.screens.dashboard

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.alarmConfirmOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.alarmLevelOptions
import com.unilumin.smartapp.client.data.DeviceAlarmInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAlarmScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    val context = LocalContext.current

    val application = context.applicationContext as Application

    val lampViewModel: LampViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LampViewModel(retrofitClient, application) as T
        }
    })
    val deviceAlarmFlow = lampViewModel.deviceAlarmFlow.collectAsLazyPagingItems()
    val alarmConfirm = lampViewModel.alarmConfirm.collectAsState()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)

    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CommonTopAppBar(
                        title = "告警管理",
                        onBack = { onBack() }
                    )
                }
            }
        }, containerColor = PageBackground
    ) { padding ->
        // 3. 【核心修改】根据状态 ID 渲染不同的内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BaseLampListScreen(
                statusOptions = alarmLevelOptions,
                viewModel = lampViewModel,
                pagingItems = deviceAlarmFlow,
                keySelector = { it.id },
                searchTitle = "搜索设备告警名称",
                middleContent = {

                    ModernStateSelector(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        options = alarmConfirmOptions,
                        selectedValue = alarmConfirm.value,
                        onValueChange = { newValue ->
                            lampViewModel.updateAlarmConfirm(newValue)
                        }
                    )
                }
            ) { item ->
                //显示告警列表
                DeviceAlarmCard(item)
            }
        }
    }
}


// --- 模拟主题色 (请替换为你项目中的实际颜色变量) ---
private val CardBgColor = Color.White
private val TextMain = Color(0xFF1F2937) // 深灰
private val TextSub = Color(0xFF6B7280)  // 浅灰
private val DividerColor = Color(0xFFE5E7EB)
private val CriticalRed = Color(0xFFEF4444)
private val WarningOrange = Color(0xFFF59E0B)
private val InfoBlue = Color(0xFF3B82F6)

/**
 * 告警信息卡片
 */
@Composable
fun DeviceAlarmCard(
    item: DeviceAlarmInfo,
    modifier: Modifier = Modifier,
    onClick: ((DeviceAlarmInfo) -> Unit)? = null
) {
    // 根据告警级别获取主题色 (假设 level 1是最高级/重要)
    val levelColor = getAlarmColor(item.level)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp) // 上下间距
            .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 1. 头部：图标 + 告警名称 + 级别标签 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 告警图标 (带淡色背景)
                Surface(
                    color = levelColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.TwoTone.Warning,
                            contentDescription = "Alarm",
                            tint = levelColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 中间：名称和产品类型
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name ?: "未知告警",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.primaryClassName ?: "未知设备类型",
                        fontSize = 12.sp,
                        color = TextSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 右上角：告警级别标签
                AlarmLevelChip(
                    levelName = item.levelName ?: "未知",
                    color = levelColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. 核心信息区 (设备号 + 时间) ---

            // 2.1 设备编号行
            InfoRowItem(
                icon = Icons.Outlined.Devices,
                label = "设备名称",
                value = item.source ?: "--"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2.2 首次告警时间
            InfoRowItem(
                icon = Icons.Outlined.AccessTime,
                label = "首次告警",
                value = item.firstAlarmTime ?: "--"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2.3 最后告警时间 (高亮显示，表示最近状态)
            InfoRowItem(
                icon = Icons.Outlined.AccessTime,
                label = "最后告警",
                value = item.lastAlarmTime ?: "--",
                valueColor = TextMain // 加深显示
            )

            // --- 3. (可选) 底部位置或额外信息 ---
            if (!item.address.isNullOrBlank() || !item.poleName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF3F4F6), // 极淡的灰色背景
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "位置: ${item.poleName ?: item.address}",
                        fontSize = 11.sp,
                        color = TextSub,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- 辅助组件 ---

/**
 * 简单的图文信息行
 */
@Composable
fun InfoRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextSub
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF9CA3AF), // 图标颜色淡一点
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = if (valueColor == TextMain) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 告警级别标签 (胶囊样式)
 */
@Composable
fun AlarmLevelChip(levelName: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f), // 背景为半透明的主色
        shape = RoundedCornerShape(100.dp), // 完全圆角
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = levelName,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// --- 逻辑辅助函数 ---

/**
 * 根据级别返回颜色
 * 根据截图："重要" 显示为红色/深色，假设逻辑如下：
 */
fun getAlarmColor(level: Int?): Color {
    return when (level) {
        1, 0 -> CriticalRed // 假设 1 是严重/重要
        2 -> WarningOrange  // 次要
        3 -> InfoBlue       // 一般
        else -> WarningOrange // 默认
    }
}