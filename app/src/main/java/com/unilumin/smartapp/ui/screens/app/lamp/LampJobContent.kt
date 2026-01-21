package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.JobSceneElement
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.viewModel.LampViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {

    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.getJobScene()
    }



    BaseLampListScreen(
        statusOptions = DeviceConstant.jobStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampJobFlow,
        keySelector = { it.id },
        searchTitle = "搜索分组名称或产品名称",
        middleContent = {
            //增加场景选择组件
            ModernDropdownFilter(lampViewModel)
        }
    ) { item ->
        Text("ssss")
    }


}
@Composable
fun ModernDropdownFilter(
    viewModel: LampViewModel,
    modifier: Modifier = Modifier
) {
    val options by viewModel.flatCheckboxOptions.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val selectedCount = selectedIds.size
    val buttonText = remember(selectedIds, options) {
        when {
            options.isEmpty() -> "无场景"
            selectedIds.isEmpty() -> "全部场景"
            selectedIds.containsAll(options.map { it.second }) -> "已全选"
            else -> "已选($selectedCount)"
        }
    }

    // 选中态颜色
    val hasFilter = selectedIds.isNotEmpty() && !selectedIds.containsAll(options.map { it.second })
    // 有筛选时用 PrimaryContainer 色，无筛选时用透明背景+灰色边框，降低视觉干扰
    val containerColor = if (hasFilter) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (hasFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (hasFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    // --- 布局结构 ---
    Box(
        modifier = modifier.fillMaxWidth(), // 填满父容器提供的一整行空间
        contentAlignment = Alignment.CenterEnd // 【核心】内容靠右对齐
    ) {
        // A. 触发器按钮 (胶囊样式 - 精致版)
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50), // 完全圆角
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.height(32.dp) // 【适配】高度设为 32dp，比搜索框(通常50dp)小，显出层级
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                // 筛选图标 (仅在有筛选时显示，或者一直显示但变色)
                if (hasFilter) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp).padding(end = 4.dp),
                        tint = contentColor
                    )
                }

                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelMedium, // 【适配】使用较小的字号
                    color = contentColor,
                    fontWeight = if(hasFilter) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.width(4.dp))

                // 箭头动画
                val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).rotate(rotation), // 图标也相应调小
                    tint = contentColor.copy(alpha = 0.6f)
                )
            }
        }

        // B. 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(200.dp) // 菜单宽度固定，不需要太宽
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .heightIn(max = 350.dp),
            offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 6.dp) // 向下偏移一点，不遮挡按钮
        ) {
            // ... 菜单内容逻辑保持不变 ...
            if (options.isNotEmpty()) {
                val allIds = options.map { it.second }
                val isAllSelected = selectedIds.containsAll(allIds) && allIds.isNotEmpty()

                DropdownMenuItem(
                    text = { Text("全选所有", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) },
                    onClick = { viewModel.toggleAllSelection() },
                    leadingIcon = { CustomCheckbox(checked = isAllSelected) },
                    contentPadding = PaddingValues(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            }

            options.forEach { (label, uniqueId) ->
                val isSelected = selectedIds.contains(uniqueId)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { viewModel.toggleSelection(uniqueId) },
                    leadingIcon = { CustomCheckbox(checked = isSelected) },
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                )
            }

            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("暂无数据", color = MaterialTheme.colorScheme.outline) },
                    onClick = {}, enabled = false
                )
            }
        }
    }
}

// 保持你的 CustomCheckbox 不变
@Composable
fun CustomCheckbox(checked: Boolean) {
    val color by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "checkboxColor"
    )
    val icon = if (checked) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}