package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.LampJobInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.GridMultiSelectBar
import com.unilumin.smartapp.ui.theme.CardBgColor
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.FailColor
import com.unilumin.smartapp.ui.theme.ProcessingColor
import com.unilumin.smartapp.ui.theme.SuccessColor
import com.unilumin.smartapp.ui.theme.TextPrimary
import com.unilumin.smartapp.ui.theme.TextSecondary
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {
    val sceneOptions = lampViewModel.sceneOptions.collectAsState()
    val sceneSelectIds = lampViewModel.selectSceneIds.collectAsState()


    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.getJobScene()
    }

    BaseLampListScreen(
        statusOptions = DeviceConstant.jobOrStrategyStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampJobFlow,
        keySelector = { it.id },
        searchTitle = "搜索业务对象名称",
        middleContent = {
            GridMultiSelectBar(
                title = "全部场景",
                options = sceneOptions.value,          // 传入 Pair 列表
                selectedKeys = sceneSelectIds.value, // 传入 Key 集合
                onSelectionChanged = { newIds -> // 回调 Key 集合
                    lampViewModel.updateSceneIds(newIds)
                }
            )
        }
    ) { item ->
        LampJobItem(sceneOptions.value, item, onItemClick = {})
    }
}


@Composable
fun LampJobItem(
    sceneOptions: List<Pair<Int, String>>,
    item: LampJobInfo,
    modifier: Modifier = Modifier,
    onItemClick: (LampJobInfo) -> Unit = {}
) {

    val (statusColor, statusText) = remember(item.status) {
        getStatusVisuals(item.status)
    }
    val businessTypeText = remember(item.businessType) {
        sceneOptions.find { it.first == item.businessType }?.second.toString()
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp) // 调整间距
            .clickable { onItemClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //业务对象名称
                Text(
                    text = item.businessName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(text = statusText, color = statusColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. 标签行：业务类型 | 失败策略 | 执行次数 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 业务场景标签 (例如: 单灯)
                InfoTag(text = businessTypeText, color = Color(0xFF42A5F5))
                Spacer(modifier = Modifier.width(8.dp))
                // 执行次数
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "执行次数:${item.tryNum}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            //  3. 任务名称
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Devices,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateInfoItem(
                    label = "创建时间",
                    time = item.createDate
                )
                if (item.exeDate.isNotEmpty()) {
                    DateInfoItem(
                        label = "执行时间",
                        time = item.exeDate
                    )
                }
            }
        }
    }
}

// --- 辅助组件 ---

@Composable
fun StatusBadge(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50)) // 半透明背景
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoTag(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun DateInfoItem(
    label: String,
    time: String,
    icon: ImageVector? = null
) {
    // 简单的截断处理，如果时间字符串太长 (例如 "2026-01-23 14:20:00")
    // 视需求可以只显示 "01-23 14:20"
    val displayTime = if (time.length > 16) time.substring(5, 16).replace("T", " ") else time
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
        Text(
            text = displayTime,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}


private fun getStatusVisuals(status: Int): Pair<Color, String> {
    return when (status) {
        1 -> TextSecondary to "待执行"
        2 -> ProcessingColor to "执行中"
        3 -> SuccessColor to "成功"
        4 -> FailColor to "失败"
        else  -> TextSecondary to "未知"
    }
}





