package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LedCtlPlanDetail
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.Tuple4
import com.unilumin.smartapp.ui.components.CommonConfirmDialog
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.components.WeekStrategySection
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

@Composable
fun SmartLedCtlPlanManage(
    screenViewModel: ScreenViewModel
) {
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val ledPlanPagingFlow = screenViewModel.ledPlanPagingFlow.collectAsLazyPagingItems()
    // 状态管理：控制删除确认弹窗及记录当前选中项
    var showDeleteDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<LedPlanBO?>(null) }
    // 获取触觉反馈服务
    val hapticFeedback = LocalHapticFeedback.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchHeader(
            searchQuery = searchQuery,
            searchTitle = "搜索控制方案名称",
            onSearchChanged = { screenViewModel.updateSearch(it) }
        )

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledPlanPagingFlow,
            itemKey = { it.id },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            emptyMessage = "暂无控制方案信息",
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) { ledPlan ->
            LedPlanCard(
                plan = ledPlan,
                modifier = Modifier.padding(vertical = 6.dp),
                onClick = {
                    // TODO: 跳转详情
                },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    planToDelete = ledPlan
                    showDeleteDialog = true
                }
            )
        }
    }
    if (showDeleteDialog && planToDelete != null) {
        val planName = planToDelete?.name ?: "未命名方案"
        CommonConfirmDialog(
            title = "删除控制方案",
            message = "确定要删除「$planName」吗？删除后将无法恢复",
            onConfirm = {
                screenViewModel.delLedPlans(planToDelete!!.id)
                showDeleteDialog = false
                planToDelete = null
                ledPlanPagingFlow.refresh()
            },
            onDismiss = {
                showDeleteDialog = false
                planToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedPlanCard(
    plan: LedPlanBO,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // 移除 onClick 传参，改用 combinedClickable 处理长按和点击
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plan.name ?: "未命名方案",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DeviceStatus(
                    plan.commandType, mapOf(
                        1 to Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "休眠"),
                        2 to Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "唤醒"),
                        3 to Triple(Color(0xFFFFF3E0), Color(0xFFF57C00), "重启"),
                        4 to Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "亮度"),
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))
            InfoRowItem(
                icon = Icons.Rounded.DateRange,
                label = "有效期",
                value = "${plan.startDate ?: "-"} 至 ${plan.endDate ?: "-"}"
            )
            Spacer(modifier = Modifier.height(12.dp))
            WeekStrategySection(
                plan.weekValue
            )
            Spacer(modifier = Modifier.height(12.dp))
            CtlPlanDetailsSection(plan.ctlPlanDetails)

        }
    }
}

/**
 * 执行计划区域容器
 */
@Composable
private fun CtlPlanDetailsSection(details: List<LedCtlPlanDetail>?) {
    if (details.isNullOrEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "执行计划 (${details.size})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        // 使用浅色背景块包裹列表，增加内聚感
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            details.forEach { detail ->
                CtlPlanDetailItem(detail)
            }
        }
    }
}

/**
 * 核心逻辑：根据指令类型渲染不同的 UI 样式
 */
@Composable
private fun CtlPlanDetailItem(detail: LedCtlPlanDetail) {
    val (icon, tintColor, typeName, timeDesc) = when (detail.commandType) {
        2 -> { // 唤醒
            val time = "${detail.startTime ?: "--"} ~ ${detail.endTime ?: "--"}"
            Tuple4(Icons.Rounded.WbSunny, Color(0xFF388E3C), "唤醒", time)
        }
        3 -> { // 重启
            Tuple4(Icons.Rounded.Refresh, Color(0xFFF57C00), "重启", detail.time ?: "--")
        }
        4 -> { // 亮度
            Tuple4(Icons.Rounded.Brightness6, Color(0xFF7B1FA2), "亮度", detail.time ?: "--")
        }
        else -> { // 未知或休眠等其他类型
            Tuple4(
                Icons.Rounded.DateRange,
                MaterialTheme.colorScheme.outline,
                "执行指令",
                detail.time ?: "--"
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 左侧图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // 2. 中间文字 (操作类型 + 时间)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = typeName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (detail.commandType == 4 && detail.commandValue != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${detail.commandValue}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}