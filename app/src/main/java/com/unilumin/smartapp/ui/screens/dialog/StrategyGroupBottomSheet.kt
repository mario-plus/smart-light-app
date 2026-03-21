package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.unilumin.smartapp.client.data.StrategyGroupListVO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyGroupBottomSheet(
    sheetState: SheetState,
    lampStrategyGroupInfoFlow: LazyPagingItems<StrategyGroupListVO>,
    selectedGroups: MutableList<StrategyGroupListVO>,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp) // 底部留出安全区
        ) {
            Text(
                text = "选择策略分组",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp, top = 4.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.6f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp) // 使用间距替代 Divider
            ) {
                items(count = lampStrategyGroupInfoFlow.itemCount) { index ->
                    val groupItem = lampStrategyGroupInfoFlow[index]
                    if (groupItem != null) {
                        val isSelected = selectedGroups.any { it.groupId == groupItem.groupId }
                        val disableReason = getDisableReason(groupItem)
                        val isEnabled = disableReason == null

                        StrategyGroupItem(
                            item = groupItem,
                            isSelected = isSelected,
                            isEnabled = isEnabled,
                            disableReason = disableReason,
                            onClick = {
                                if (isEnabled) {
                                    if (isSelected) {
                                        selectedGroups.removeAll { it.groupId == groupItem.groupId }
                                    } else {
                                        selectedGroups.add(groupItem)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("确定", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StrategyGroupItem(
    item: StrategyGroupListVO,
    isSelected: Boolean,
    isEnabled: Boolean,
    disableReason: String?,
    onClick: () -> Unit
) {
    // 现代化的选中状态视觉反馈：微色背景 + 品牌色边框
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    val itemAlpha = if (isEnabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(start = 8.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
            .alpha(itemAlpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧 Checkbox
        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            enabled = isEnabled,
            colors = CheckboxDefaults.colors(
                disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
            // 第一行：标题 + 同步状态 (左右两端对齐)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.groupName ?: "未知分组",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            velocity = 30.dp
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))

                val isSynced = item.syncState == 1
                StatusTag(
                    text = if (isSynced) "已同步" else "未同步",
                    containerColor = if (isSynced) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    textColor = if (isSynced) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 第二行：所属设备 (淡色弱化)
            if (!item.deviceName.isNullOrBlank()) {
                Text(
                    text = "设备: ${item.deviceName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 第三行：聚合次要信息 (设备类型 • 成员数 • 策略数)
            val typeName = when (item.groupType) {
                1L -> "单灯控制器"
                25L -> "集控控制器"
                56L -> "回路控制器" // 修复了原代码的“回路回路控制器”笔误
                else -> "未知类型"
            }
            val policyText = if (item.isShowPolicyNumOfLight == 1) {
                "单灯策略 ${item.availablePolicyNumOfLight ?: 0}/${item.maxPolicyNumOfLight ?: 0}"
            } else {
                "策略 ${item.availablePolicyNum ?: 0}/${item.maxPolicyNum ?: 0}"
            }

            Text(
                text = "$typeName  •  成员 ${item.count ?: 0}  •  $policyText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            // 第四行：禁用原因 (仅禁用时显示，使用红色警示)
            if (!isEnabled && disableReason != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = disableReason,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 辅助组件：漂亮的状态小标签
 */
@Composable
fun StatusTag(
    text: String,
    containerColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .background(color = containerColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 核心业务逻辑：校验分组是否满足被选择的条件
 */
fun getDisableReason(item: StrategyGroupListVO): String? {
    if (item.syncState != 1) return "分组未同步"

    if ((item.count ?: 0) <= 0) return "分组成员数为0"

    val availableNormal = item.availablePolicyNum ?: 0
    val availableLight = item.availablePolicyNumOfLight ?: 0
    if (availableNormal <= 0 && availableLight <= 0) {
        return "可用策略数为0"
    }
    return null
}