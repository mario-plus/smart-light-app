package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.ui.components.CommonConfirmDialog
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.components.WeekStrategySection
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel

@Composable
fun SmartLedPlayPlanManage(
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
            searchTitle = "搜索播放方案名称",
            onSearchChanged = { screenViewModel.updateSearch(it) }
        )

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledPlanPagingFlow,
            itemKey = { it.id },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            onAddClick = {
                //TODO 新增播放方案
            },
            emptyMessage = "暂无播放方案信息",
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) { ledPlan ->
            PlayPlanCard(
                plan = ledPlan,
                onClick = {
//                    onEditPlan(ledPlan)
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
            title = "删除播放方案",
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
fun PlayPlanCard(
    plan: LedPlanBO,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = plan.name ?: "未命名方案",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                DeviceStatus(
                    plan.programPlayType, mapOf(
                        100 to Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "插播"),
                        200 to Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "轮播")
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            InfoRowItem(
                icon = Icons.Rounded.Layers,
                label = "优先级",
                value = plan.programSort?.toString() ?: "0",
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRowItem(
                icon = Icons.Rounded.PlayCircleOutline,
                label = "播放表",
                value = plan.programName.toString(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRowItem(
                icon = Icons.Rounded.AccessTime,
                label = "播放时间",
                value = "${plan.programStartTime ?: "-"} 至 ${plan.programEndTime ?: "-"}",
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRowItem(
                icon = Icons.Rounded.DateRange,
                label = "有效期",
                value = "${plan.startDate ?: "-"} 至 ${plan.endDate ?: "-"}"
            )
            Spacer(modifier = Modifier.height(16.dp))

            WeekStrategySection(plan.weekValue)
        }
    }
}