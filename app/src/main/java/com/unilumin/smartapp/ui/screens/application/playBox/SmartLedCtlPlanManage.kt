package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
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
import com.unilumin.smartapp.ui.components.InteractiveControlCard
import com.unilumin.smartapp.ui.components.ModernTimePickerDialog
import com.unilumin.smartapp.ui.components.ModernTimeRangePickerDialog
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.components.WeekStrategySection
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActionDetailState(
    val id: Long? = null,
    val startTime: String = "08:00:00",
    val endTime: String = "18:00:00",
    val singleTime: String = "00:00:00",
    val brightness: String = "0"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedCtlPlanManage(
    screenViewModel: ScreenViewModel
) {
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val ledPlanPagingFlow = screenViewModel.ledPlanPagingFlow.collectAsLazyPagingItems()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<LedPlanBO?>(null) }

    // 表单状态管理
    var showFormSheet by remember { mutableStateOf(false) }
    var planToEdit by remember { mutableStateOf<LedPlanBO?>(null) }

    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        SearchHeader(
            searchQuery = searchQuery,
            searchTitle = "搜索控制方案名称",
            onSearchChanged = { screenViewModel.updateSearch(it) }
        )

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledPlanPagingFlow,
            itemKey = { it.id!! },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            onAddClick = {
                planToEdit = null
                showFormSheet = true
            },
            emptyMessage = "暂无控制方案信息",
            contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)
        ) { ledPlan ->
            LedPlanCard(
                plan = ledPlan,
                onEditClick = {
                    planToEdit = ledPlan
                    showFormSheet = true
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
                screenViewModel.delLedPlans(planToDelete!!.id!!) {
                    showDeleteDialog = false
                    planToDelete = null
                    ledPlanPagingFlow.refresh()
                }
            },
            onDismiss = {
                showDeleteDialog = false
                planToDelete = null
            }
        )
    }

    // 底部表单弹窗
    if (showFormSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            CtlPlanForm(
                initialPlan = planToEdit,
                onDismiss = { showFormSheet = false },
                onSave = { isEdit, updatedPlan ->
                    if (isEdit) {
                        screenViewModel.editLedPlans(updatedPlan) {
                            showFormSheet = false
                            ledPlanPagingFlow.refresh()
                        }
                    } else {
                        screenViewModel.addLedPlans(updatedPlan) {
                            showFormSheet = false
                            ledPlanPagingFlow.refresh()
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CtlPlanForm(
    initialPlan: LedPlanBO?,
    onDismiss: () -> Unit,
    onSave: (Boolean, LedPlanBO) -> Unit
) {
    val isEdit = initialPlan != null
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var name by remember { mutableStateOf(initialPlan?.name ?: "") }
    var commandType by remember { mutableIntStateOf(initialPlan?.commandType ?: 2) }

    var startDate by remember { mutableStateOf(initialPlan?.startDate ?: "") }
    var endDate by remember { mutableStateOf(initialPlan?.endDate ?: "") }

    val weekStr = initialPlan?.weekValue ?: "0,0,0,0,0,0,0"
    val weekStates =
        remember { mutableStateListOf(*weekStr.split(",").map { it == "1" }.toTypedArray()) }

    var showDateRangePicker by remember { mutableStateOf(false) }

    // 初始化动作列表状态
    var actionItems by remember {
        mutableStateOf(
            if (initialPlan != null && !initialPlan.executePlans.isNullOrEmpty()) {
                val mapped = initialPlan.executePlans!!.map {
                    ActionDetailState(
                        id = it.id,
                        startTime = it.startTime ?: "08:00:00",
                        endTime = it.endTime ?: "18:00:00",
                        singleTime = it.time ?: "12:00:00",
                        brightness = it.commandValue?.toString() ?: "50"
                    )
                }
                if (initialPlan.commandType == 4 && mapped.size < 2) {
                    mapped + ActionDetailState()
                } else mapped
            } else {
                listOf(ActionDetailState())
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEdit) "编辑控制方案" else "新增控制方案",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("方案名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            prefix = {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        )

        // 指令类型选择
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = commandType == 2,
                    onClick = { commandType = 2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("唤醒") }
                SegmentedButton(
                    selected = commandType == 3,
                    onClick = { commandType = 3 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("重启") }
                SegmentedButton(
                    selected = commandType == 4,
                    onClick = {
                        commandType = 4

                        if (actionItems.size < 2) {
                            val newList = actionItems.toMutableList()
                            repeat(2 - actionItems.size) { newList.add(ActionDetailState()) }
                            actionItems = newList
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("亮度") }
            }
        }

        ClickableField(
            label = "有效日期区间",
            value = if (startDate.isNotEmpty() && endDate.isNotEmpty()) "$startDate 至 $endDate" else "点击选择起止日期",
            icon = Icons.Rounded.DateRange,
            onClick = { showDateRangePicker = true }
        )

        // 星期选择
        val weekNames = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekNames.forEachIndexed { index, day ->
                val isSelected = weekStates[index]
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { weekStates[index] = !weekStates[index] },
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f)
        )

        // 动态执行明细列表
        Text(
            text = "执行动作配置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        actionItems.forEachIndexed { index, item ->
            ActionItemEditor(
                index = index,
                commandType = commandType,
                itemState = item,
                canRemove = if (commandType == 4) actionItems.size > 2 else actionItems.size > 1,
                onUpdate = { updatedItem ->
                    val newList = actionItems.toMutableList()
                    newList[index] = updatedItem
                    actionItems = newList
                },
                onRemove = {
                    val newList = actionItems.toMutableList()
                    newList.removeAt(index)
                    actionItems = newList
                }
            )
        }

        FilledTonalButton(
            onClick = { actionItems = actionItems + ActionDetailState() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加执行动作", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 底部按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("取消") }

            Button(
                onClick = {
                    val resultWeekStr = weekStates.joinToString(",") { if (it) "1" else "0" }
                    val detailList = actionItems.map { state ->
                        LedCtlPlanDetail().apply {
//                            this.id = state.id
                            if (commandType == 2) {
                                this.startTime = state.startTime
                                this.endTime = state.endTime
                            } else {
                                this.time = state.singleTime
                                if (commandType == 4) {
                                    this.commandValue = state.brightness.toIntOrNull() ?: 50
                                }
                            }
                        }
                    }

                    val newPlan = (initialPlan ?: LedPlanBO()).apply {
                        this.isDate = 1
                        this.isTime = 1
                        this.isWeek = 1
                        this.type = 1
                        this.name = name
                        this.commandType = commandType
                        this.dateRange = listOf<String>(startDate, endDate)
                        this.startDate = startDate
                        this.endDate = endDate
                        this.weekValue = resultWeekStr
                        this.executePlans = detailList
                    }
                    onSave(isEdit, newPlan)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() &&
                        (commandType != 4 || actionItems.all { it.brightness.isNotBlank() })
            ) { Text(if (isEdit) "保存修改" else "确认新增", fontWeight = FontWeight.Bold) }
        }
    }

    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateRangePickerState.selectedStartDateMillis?.let {
                        startDate = dateFormatter.format(Date(it))
                    }
                    dateRangePickerState.selectedEndDateMillis?.let {
                        endDate = dateFormatter.format(Date(it))
                    }
                    showDateRangePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("取消") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(480.dp),
                title = { Text("选择起止日期", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionItemEditor(
    index: Int,
    commandType: Int,
    itemState: ActionDetailState,
    canRemove: Boolean,
    onUpdate: (ActionDetailState) -> Unit,
    onRemove: () -> Unit
) {
    var showTimeRangePicker by remember { mutableStateOf(false) }
    var showSingleTimePicker by remember { mutableStateOf(false) }

    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "动作 ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            when (commandType) {
                2 -> {
                    ClickableField(
                        label = "有效时间段 (唤醒)",
                        value = "${itemState.startTime.take(5)} 至 ${itemState.endTime.take(5)}",
                        icon = Icons.Rounded.AccessTime,
                        onClick = { showTimeRangePicker = true }
                    )
                }

                3, 4 -> {
                    ClickableField(
                        label = "执行时间点",
                        value = itemState.singleTime.take(5),
                        icon = Icons.Rounded.AccessTime,
                        onClick = { showSingleTimePicker = true }
                    )
                }
            }

            if (commandType == 4) {
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    InteractiveControlCard(
                        title = "亮度",
                        value = itemState.brightness.toIntOrNull() ?: 0,
                        unit = "%",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onValueChange = { newInt ->
                            onUpdate(itemState.copy(brightness = newInt.toString()))
                        },
                        onCommit = { finalInt ->
                            onUpdate(itemState.copy(brightness = finalInt.toString()))
                        }
                    )
                }
            }
        }
    }

    if (showTimeRangePicker) {
        ModernTimeRangePickerDialog(
            initialStart = itemState.startTime,
            initialEnd = itemState.endTime,
            onDismiss = { showTimeRangePicker = false },
            onConfirm = { start, end ->
                onUpdate(itemState.copy(startTime = start, endTime = end))
                showTimeRangePicker = false
            }
        )
    }
    if (showSingleTimePicker) {
        ModernTimePickerDialog(initialTime = itemState.singleTime, onDismiss = {
            showSingleTimePicker = false
        }, onConfirm = {
            onUpdate(itemState.copy(singleTime = it))
            showSingleTimePicker = false
        })
    }
}

@Composable
fun LedPlanCard(
    plan: LedPlanBO,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongClick() }) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 1.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
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
            WeekStrategySection(plan.weekValue)
            Spacer(modifier = Modifier.height(12.dp))
            CtlPlanDetailsSection(plan.executePlans)
        }
    }
}

@Composable
private fun CtlPlanDetailsSection(details: List<LedCtlPlanDetail>?) {
    if (details.isNullOrEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "执行明细 (${details.size})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
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
            details.forEach { detail -> CtlPlanDetailItem(detail) }
        }
    }
}

@Composable
private fun CtlPlanDetailItem(detail: LedCtlPlanDetail) {
    val (icon, tintColor, typeName, timeDesc) = when (detail.commandType) {
        2 -> Tuple4(
            Icons.Rounded.WbSunny,
            Color(0xFF388E3C),
            "唤醒",
            "${detail.startTime ?: "--"} ~ ${detail.endTime ?: "--"}"
        )

        3 -> Tuple4(Icons.Rounded.Refresh, Color(0xFFF57C00), "重启", detail.time ?: "--")
        4 -> Tuple4(Icons.Rounded.Brightness6, Color(0xFF7B1FA2), "亮度", detail.time ?: "--")
        else -> Tuple4(
            Icons.Rounded.DateRange,
            MaterialTheme.colorScheme.outline,
            "执行指令",
            detail.time ?: "--"
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
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