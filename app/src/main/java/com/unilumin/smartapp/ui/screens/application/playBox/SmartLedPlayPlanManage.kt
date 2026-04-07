package com.unilumin.smartapp.ui.screens.application.playBox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LedPlanBO
import com.unilumin.smartapp.client.data.LedProgramRes
import com.unilumin.smartapp.ui.components.CommonConfirmDialog
import com.unilumin.smartapp.ui.components.CommonDropdownMenu
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.InfoRowItem
import com.unilumin.smartapp.ui.components.ModernTimeRangePickerDialog
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader
import com.unilumin.smartapp.ui.components.WeekStrategySection
import com.unilumin.smartapp.ui.viewModel.ScreenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLedPlayPlanManage(
    screenViewModel: ScreenViewModel
) {
    val searchQuery by screenViewModel.searchQuery.collectAsState()
    val totalCount by screenViewModel.totalCount.collectAsState()
    val ledPlanPagingFlow = screenViewModel.ledPlanPagingFlow.collectAsLazyPagingItems()
    val ledProgramPagingFlow = screenViewModel.ledProgramPagingFlow.collectAsLazyPagingItems()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<LedPlanBO?>(null) }
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
            searchTitle = "搜索播放方案名称",
            onSearchChanged = { screenViewModel.updateSearch(it) }
        )

        PagingList(
            totalCount = totalCount,
            lazyPagingItems = ledPlanPagingFlow,
            itemKey = { it.id!! },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            onAddClick = {
                planToEdit = null
                showFormSheet = true
            },
            emptyMessage = "暂无播放方案信息",
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) { ledPlan ->
            PlayPlanCard(
                plan = ledPlan,
                onClick = {
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
        CommonConfirmDialog(
            title = "删除播放方案",
            message = "确定要删除「${planToDelete?.name ?: "未命名方案"}」吗？删除后将无法恢复",
            onConfirm = {
                screenViewModel.delLedPlans(planToDelete!!.id!!)
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

    if (showFormSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            PlayPlanForm(
                initialPlan = planToEdit,
                ledProgramPagingFlow = ledProgramPagingFlow,
                onDismiss = { showFormSheet = false },
                onSave = { isEdit, updatedPlan ->
                    if (isEdit) {
                        screenViewModel.editLedPlans(updatedPlan, onSuccess = {
                            showFormSheet = false
                            ledPlanPagingFlow.refresh()
                        })
                    } else {
                        screenViewModel.addLedPlans(updatedPlan, onSuccess = {
                            showFormSheet = false
                            ledPlanPagingFlow.refresh()
                        })
                    }

                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayPlanForm(
    initialPlan: LedPlanBO?,
    ledProgramPagingFlow: LazyPagingItems<LedProgramRes>,
    onDismiss: () -> Unit,
    onSave: (Boolean, LedPlanBO) -> Unit
) {
    val isEdit = initialPlan != null
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var name by remember { mutableStateOf(initialPlan?.name ?: "") }
    var playType by remember { mutableIntStateOf(initialPlan?.programPlayType ?: 100) }
    var programSort by remember { mutableStateOf(initialPlan?.programSort?.toString() ?: "0") }
    var programId by remember { mutableStateOf(initialPlan?.programId ?: "") }

    var startDate by remember { mutableStateOf(initialPlan?.startDate ?: "") }
    var endDate by remember { mutableStateOf(initialPlan?.endDate ?: "") }
    var startTime by remember { mutableStateOf(initialPlan?.programStartTime ?: "00:00:00") }
    var endTime by remember { mutableStateOf(initialPlan?.programEndTime ?: "23:59:59") }

    val weekStr = initialPlan?.weekValue ?: "0,0,0,0,0,0,0"
    val weekStates =
        remember { mutableStateListOf(*weekStr.split(",").map { it == "1" }.toTypedArray()) }

    var showDateRangePicker by remember { mutableStateOf(false) }
    var showTimeRangePicker by remember { mutableStateOf(false) }

    val programsList = remember(ledProgramPagingFlow.itemCount) {
        val list = mutableListOf<LedProgramRes>()
        for (i in 0 until ledProgramPagingFlow.itemCount) {
            ledProgramPagingFlow[i]?.let { list.add(it) }
        }
        list
    }
    val selectedProgram = programsList.find { it.id.toString() == programId }

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
            text = if (isEdit) "编辑播放方案" else "新建播放方案",
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
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        OutlinedTextField(
            value = programSort,
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    programSort = ""
                } else {
                    val intValue = newValue.toIntOrNull()
                    if (intValue != null && intValue in 1..100) {
                        programSort = newValue
                    }
                }
            },
            label = { Text("优先级 (0-100)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            prefix = {
                Icon(
                    Icons.Rounded.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        CommonDropdownMenu(
            items = programsList,
            selectedItem = selectedProgram,
            itemLabel = { it.name.toString() },
            onItemSelected = { programId = it.id.toString() },
            label = "关联播放表",
            placeholder = "请选择播放表",
            modifier = Modifier.fillMaxWidth()
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = playType == 100,
                onClick = { playType = 100 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("插播 (100)") }
            SegmentedButton(
                selected = playType == 200,
                onClick = { playType = 200 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("轮播 (200)") }
        }

        ClickableField(
            label = "有效日期区间",
            value = if (startDate.isNotEmpty() && endDate.isNotEmpty()) "$startDate 至 $endDate" else "点击选择起止日期",
            icon = Icons.Rounded.DateRange,
            onClick = { showDateRangePicker = true }
        )

        ClickableField(
            label = "有效时间段",
            value = "${startTime.take(5)} 至 ${endTime.take(5)}",
            icon = Icons.Rounded.AccessTime,
            onClick = { showTimeRangePicker = true }
        )

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
                    val newPlan = (initialPlan ?: LedPlanBO()).apply {
                        this.isDate = 1
                        this.isTime = 1
                        this.isWeek = 1
                        this.name = name
                        this.programPlayType = playType
                        this.programSort = programSort.toIntOrNull() ?: 1
                        this.programId = programId
                        this.startDate = startDate
                        this.endDate = endDate
                        this.programStartTime = startTime
                        this.programEndTime = endTime
                        this.weekValue = resultWeekStr
                        this.type = 2
                    }
                    onSave(isEdit, newPlan)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && programId.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()
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
                TextButton(onClick = {
                    showDateRangePicker = false
                }) { Text("取消") }
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

    if (showTimeRangePicker) {
        ModernTimeRangePickerDialog(
            initialStart = startTime,
            initialEnd = endTime,
            onDismiss = { showTimeRangePicker = false },
            onConfirm = { start, end ->
                startTime = start
                endTime = end
                showTimeRangePicker = false
            }
        )
    }
}

@Composable
fun ClickableField(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// --- 卡片视图展示区 ---
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // 使用统一点距移除冗余Spacer
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
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
            InfoRowItem(
                icon = Icons.Rounded.Layers,
                label = "优先级",
                value = plan.programSort?.toString() ?: "0"
            )
            InfoRowItem(
                icon = Icons.Rounded.PlayCircleOutline,
                label = "播放表",
                value = plan.programName.toString()
            )
            InfoRowItem(
                icon = Icons.Rounded.AccessTime,
                label = "播放时间",
                value = "${plan.programStartTime ?: "-"} 至 ${plan.programEndTime ?: "-"}"
            )
            InfoRowItem(
                icon = Icons.Rounded.DateRange,
                label = "有效期",
                value = "${plan.startDate ?: "-"} 至 ${plan.endDate ?: "-"}"
            )

            Box(modifier = Modifier.padding(top = 8.dp)) {
                WeekStrategySection(plan.weekValue)
            }
        }
    }
}
