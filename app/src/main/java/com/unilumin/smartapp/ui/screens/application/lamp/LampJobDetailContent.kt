package com.unilumin.smartapp.ui.screens.application.lamp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.jobDetailStatusOptions
import com.unilumin.smartapp.client.data.TaskInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoLabelValue
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Green50
import com.unilumin.smartapp.ui.theme.Green500
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.util.TimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobDetailContent(
    lampViewModel: LampViewModel, onBack: () -> Unit
) {
    val lampJobDetailFlow = lampViewModel.lampJobDetailFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "任务详情", onBack = { onBack() })
        }, containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BaseLampListScreen(
                statusOptions = jobDetailStatusOptions,
                viewModel = lampViewModel,
                pagingItems = lampJobDetailFlow,
                keySelector = { it.id },
                searchTitle = "搜索设备名称"
            ) { item ->
                TaskDetailCard(item = item)
            }
        }
    }
}

/**
 * 顶层卡片容器，包裹根节点
 */
@Composable
fun TaskDetailCard(item: TaskInfo) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 从这里开始调用递归渲染组件，标记 isRoot = true
            TaskNodeContent(item = item, isRoot = true)
        }
    }
}

/**
 * 核心递归组件：负责渲染当前节点的所有数据，如果包含子节点，则递归调用 ChildJobsSection
 */
@Composable
fun TaskNodeContent(item: TaskInfo, isRoot: Boolean = false) {
    // 根节点和子节点的标题展示逻辑稍作区分
    val titleText = if (isRoot) {
        item.deviceName ?: "未知设备"
    } else {
        item.deviceName ?: "子任务 (ID: ${item.id})"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. 头部区域：标题与状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DeviceStatus(
                status = item.status, mapOf(
                    2 to Triple(Green50, Green500, "成功"),
                    1 to Triple(Gray100, Gray500, "失败"),
                    3 to Triple(Color(0xFF1976D2), Color(0xFFE3F2FD), "待确认")
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        InfoLabelValue(label = "重试次数", value = item.tryNum?.toString() ?: "0")
        if (item.cause != null) {
            InfoLabelValue(label = "执行结果", value = item.cause!!)
        }
        if (item.updateDate != null) InfoLabelValue(
            label = "更新时间",
            value = TimeUtil.formatIsoTime(item.updateDate!!)
        )
        if (!item.remark.isNullOrBlank()) {
            InfoLabelValue(label = "备注信息", value = item.remark!!)
        }
        if (!item.context.isNullOrBlank() || !item.responseInfo.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (!item.context.isNullOrBlank()) {
            ExpandableContentSection(title = "任务内容 (Context)", content = item.context!!)
        }
        if (!item.responseInfo.isNullOrBlank()) {
            ExpandableContentSection(title = "响应数据 (Response)", content = item.responseInfo!!)
        }

        // 4. 递归处理子节点！
        if (!item.childJobs.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            // 只有根节点下面加一条横线区分，避免深层嵌套横线过多显得杂乱
            if (isRoot) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
            // 展开并继续渲染子任务列表
            ChildJobsSection(childJobs = item.childJobs!!)
        }
    }
}

/**
 * 子任务列表容器
 */
@Composable
fun ChildJobsSection(childJobs: List<TaskInfo>) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "child_jobs_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "子任务 (${childJobs.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "折叠" else "展开",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(rotationState)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                childJobs.forEach { childJob ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            // 每次递归进来，背景会叠加一层透明度，天然形成层级深度视觉感！
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            // 这里减少了一点内边距(从12dp改为10dp)，防止无限嵌套导致内容被挤压得太窄
                            .padding(10.dp)
                    ) {
                        // 【核心递归调用】子节点再次调用 TaskNodeContent
                        TaskNodeContent(item = childJob, isRoot = false)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableContentSection(title: String, content: String) {
    var expanded by remember { mutableStateOf(false) }
    // 💡 关键修改：展开时旋转 90 度 (向右变成向下)
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "icon_rotation"
    )
    // 清理 JSON 中的转义斜杠
    val cleanContent = remember(content) {
        content.replace("\\", "")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "折叠" else "展开",
                tint = MaterialTheme.colorScheme.primary,
                // 应用旋转动画
                modifier = Modifier.rotate(rotationState)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    .padding(12.dp)
            ) {
                SelectionContainer {
                    Text(
                        // 渲染清理后的文本
                        text = cleanContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}