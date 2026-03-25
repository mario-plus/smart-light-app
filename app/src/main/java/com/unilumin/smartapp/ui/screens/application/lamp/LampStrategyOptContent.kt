package com.unilumin.smartapp.ui.screens.application.lamp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.gson.JsonObject
import com.unilumin.smartapp.client.data.DayData
import com.unilumin.smartapp.client.data.KeyValue
import com.unilumin.smartapp.client.data.LngLatStrategyCondition
import com.unilumin.smartapp.client.data.LngLatStrategyContent
import com.unilumin.smartapp.client.data.RiseDown
import com.unilumin.smartapp.client.data.StrategyAction
import com.unilumin.smartapp.client.data.StrategyDTO
import com.unilumin.smartapp.client.data.StrategyGroupListVO
import com.unilumin.smartapp.client.data.StrategyProductVO
import com.unilumin.smartapp.client.data.TimeStrategyCondition
import com.unilumin.smartapp.client.data.TimeStrategyContent
import com.unilumin.smartapp.client.data.TimeTaskConfig
import com.unilumin.smartapp.ui.components.CommonDropdownMenu
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.StepProgressIndicator
import com.unilumin.smartapp.ui.screens.dialog.StrategyGroupBottomSheet
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.util.JsonUtils
import com.unilumin.smartapp.util.StrategyContentUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. 状态管理类 (State Holder) - 预留编辑功能入口
// ==========================================
class LampStrategyFormState(
    // TODO: 接入编辑功能时，可在此处传入 initialStrategy: StrategyDTO? 进行数据回显初始化
) {
    var currentStep by mutableIntStateOf(1)

    // --- Step 1: 基本信息 ---
    var strategyName by mutableStateOf("")
    var remarkInfo by mutableStateOf("")
    var selectedProduct by mutableStateOf<StrategyProductVO?>(null)
    var selectedStrategyType by mutableStateOf<Pair<Long, KeyValue>?>(null)
    var selectedPolicyType by mutableStateOf<Pair<Long, KeyValue>?>(null)
    var showGroupBottomSheet by mutableStateOf(false)
    val selectedGroups = mutableStateListOf<StrategyGroupListVO>()

    // --- Step 2: 策略详情 ---
    var selectedPolicyPeriod by mutableStateOf<Pair<Long, KeyValue>?>(null)
    val selectedDaysOfWeek = mutableStateListOf<Int>()
    var startDateMillis by mutableStateOf<Long?>(null)
    var endDateMillis by mutableStateOf<Long?>(null)
    var selectedPriority by mutableStateOf<Int?>(null)

    // --- 时间策略专属 ---
    var nextTaskId by mutableIntStateOf(1)
    val timeTasks = mutableStateListOf(TimeTaskConfig(id = 0))
    var editingTimeIndex by mutableStateOf<Int?>(null)

    // --- 经纬度策略专属 ---
    var sunriseOffset by mutableStateOf("")
    var sunriseActionType by mutableStateOf<Pair<Long, KeyValue>?>(null)
    var sunriseActionValue by mutableStateOf("")

    var sunsetOffset by mutableStateOf("")
    var sunsetActionType by mutableStateOf<Pair<Long, KeyValue>?>(null)
    var sunsetActionValue by mutableStateOf("")

    // 弹窗控制
    var showDateRangePicker by mutableStateOf(false)
}

@Composable
fun rememberLampStrategyFormState() = remember { LampStrategyFormState() }


// ==========================================
// 2. 主页面骨架
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampStrategyOptContent(
    lampViewModel: LampViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.getGroupProductList()
    }

    val formState = rememberLampStrategyFormState()
    val dateFormatter = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }

    BackHandler(enabled = true) {
        if (formState.currentStep == 2) formState.currentStep = 1 else onBack()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = if (formState.currentStep == 1) "新建策略 (1/2)" else "策略详情 (2/2)",
                onBack = { if (formState.currentStep == 2) formState.currentStep = 1 else onBack() }
            )
        },
        containerColor = PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            StepProgressIndicator(
                steps = listOf("基本信息", "策略详情"),
                currentStep = formState.currentStep - 1
            )

            AnimatedContent(
                targetState = formState.currentStep,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val slideDirection = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(300)) { it * slideDirection } + fadeIn()) togetherWith
                            (slideOutHorizontally(tween(300)) { -it * slideDirection } + fadeOut())
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> StepOneBasicInfo(formState, lampViewModel)
                    2 -> StepTwoStrategyDetails(formState, lampViewModel, dateFormatter)
                }
            }
        }

        // 全局弹窗处理
        DialogAndBottomSheetHandler(formState, lampViewModel, dateFormatter)
    }
}

// ==========================================
// 3. 步骤一：基本信息
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepOneBasicInfo(
    state: LampStrategyFormState,
    viewModel: LampViewModel
) {
    val strategyGroupProductList by viewModel.strategyGroupProductList.collectAsState()
    val strategyTypeList by viewModel.strategyTypeList.collectAsState()
    val policyTypeList by viewModel.policyTypeList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.strategyName,
            onValueChange = { state.strategyName = it },
            label = { RequiredLabel("策略名称") },
            placeholder = { Text("请输入策略名称") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        CommonDropdownMenu(
            items = strategyGroupProductList,
            selectedItem = state.selectedProduct,
            itemLabel = { it.productName },
            label = "所属产品 *",
            placeholder = "请选择产品",
            onItemSelected = { product ->
                state.selectedProduct = product
                state.selectedGroups.clear()
                state.selectedStrategyType = null
                state.selectedPolicyType = null
                viewModel.updateCurrentProductId(product.productId)
            }
        )

        GroupSelectionField(state)

        if (strategyTypeList.isNotEmpty()) {
            CommonDropdownMenu(
                items = strategyTypeList,
                selectedItem = state.selectedStrategyType,
                itemLabel = { it.second.value },
                label = "策略类型",
                placeholder = "请选择策略类型",
                onItemSelected = { state.selectedStrategyType = it }
            )
        }
        if (policyTypeList.isNotEmpty()) {
            CommonDropdownMenu(
                items = policyTypeList,
                selectedItem = state.selectedPolicyType,
                itemLabel = { it.second.value },
                label = "策略模式",
                placeholder = "请选择策略模式",
                onItemSelected = { state.selectedPolicyType = it }
            )
        }

        OutlinedTextField(
            value = state.remarkInfo,
            onValueChange = { state.remarkInfo = it },
            label = { Text("备注信息") },
            placeholder = { Text("请输入备注信息 (选填)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { state.currentStep = 2 },
            enabled = state.strategyName.isNotBlank() && state.selectedProduct != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("下一步：配置策略详情", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// 4. 步骤二：策略详情
// ==========================================
@Composable
private fun StepTwoStrategyDetails(
    state: LampStrategyFormState,
    viewModel: LampViewModel,
    dateFormatter: SimpleDateFormat
) {
    val policyContent by viewModel.policyContent.collectAsState()

    val policyActionTypes = remember(state.selectedProduct, state.selectedPolicyType, policyContent) {
        if (state.selectedProduct != null && state.selectedPolicyType != null) {
            StrategyContentUtil.getPolicyActionTypes(
                productId = state.selectedProduct!!.productId,
                jsonObject = policyContent,
                key = state.selectedPolicyType!!.second.key
            )
        } else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 优先级与生效周期设置 ---
        PriorityAndPeriodSection(state, policyContent)

        // --- 动态策略配置区 ---
        when (state.selectedPolicyType?.second?.key) {
            "timeStrategies" -> {
                TimeStrategySection(state, policyContent, policyActionTypes)
            }
            "lngLatStrategies" -> {
                LngLatStrategySection(state, policyActionTypes)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { submitStrategy(state, viewModel, dateFormatter) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("提交并同步", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


// ==========================================
// 5. 局部的功能化组件 (增强复用性和可读性)
// ==========================================
@Composable
private fun GroupSelectionField(state: LampStrategyFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = if (state.selectedGroups.isEmpty()) "" else "已选择 ${state.selectedGroups.size} 个分组",
            onValueChange = {},
            readOnly = true,
            label = { Text("关联分组 (多选)") },
            placeholder = { Text("请点击选择关联分组") },
            trailingIcon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { state.showGroupBottomSheet = true },
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (state.selectedGroups.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.selectedGroups.forEach { group ->
                    InputChip(
                        selected = true,
                        onClick = { state.selectedGroups.removeAll { it.groupId == group.groupId } },
                        label = { Text(group.groupName ?: "未知分组") },
                        trailingIcon = { Icon(Icons.Default.Close, "移除", Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityAndPeriodSection(state: LampStrategyFormState, policyContent: JsonObject?) {
    val policyPriorityRange = StrategyContentUtil.getPolicyPriorityRange(
        policyContent,
        state.selectedPolicyType?.second?.key.toString()
    )
    val policyPeriodTypes = StrategyContentUtil.getPolicyPeriodTypes(
        policyContent,
        state.selectedPolicyType?.second?.key.toString()
    )

    LaunchedEffect(policyPriorityRange) {
        if (policyPriorityRange != null) {
            if (state.selectedPriority == null || state.selectedPriority!! !in policyPriorityRange.min..policyPriorityRange.max) {
                state.selectedPriority = policyPriorityRange.min
            }
        } else {
            state.selectedPriority = null
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (policyPriorityRange != null) {
            Box(modifier = Modifier.weight(1f)) {
                CommonDropdownMenu(
                    items = (policyPriorityRange.min..policyPriorityRange.max).toList(),
                    selectedItem = state.selectedPriority,
                    itemLabel = { it.toString() },
                    label = "策略优先级",
                    placeholder = "请选择",
                    onItemSelected = { state.selectedPriority = it }
                )
            }
        }

        if (policyPeriodTypes.isNotEmpty()) {
            Box(modifier = Modifier.weight(2f)) {
                CommonDropdownMenu(
                    items = policyPeriodTypes,
                    selectedItem = state.selectedPolicyPeriod,
                    itemLabel = { it.second.value },
                    label = "生效周期",
                    placeholder = "请选择",
                    onItemSelected = { data ->
                        state.selectedPolicyPeriod = data
                        state.selectedDaysOfWeek.clear()
                        state.startDateMillis = null
                        state.endDateMillis = null
                    }
                )
            }
        }
    }

    // 周/日期选择 UI
    if (state.selectedPolicyPeriod != null && state.selectedPolicyPeriod?.first!! > 1) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                when (state.selectedPolicyPeriod?.first) {
                    2L -> WeekSelectionUI(state)
                    3L -> DateRangeSelectionUI(state)
                }
            }
        }
    }
}

@Composable
private fun WeekSelectionUI(state: LampStrategyFormState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val weekDays = listOf(1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日")
        weekDays.forEach { (dayValue, dayName) ->
            val isSelected = state.selectedDaysOfWeek.contains(dayValue)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (isSelected) state.selectedDaysOfWeek.remove(dayValue)
                        else state.selectedDaysOfWeek.add(dayValue)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DateRangeSelectionUI(state: LampStrategyFormState) {
    val dateFormatter = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { state.showDateRangePicker = true },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = state.startDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("开始日期") },
            trailingIcon = { Icon(Icons.Default.DateRange, null) },
            modifier = Modifier.weight(1f),
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )
        OutlinedTextField(
            value = state.endDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("结束日期") },
            trailingIcon = { Icon(Icons.Default.DateRange, null) },
            modifier = Modifier.weight(1f),
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun TimeStrategySection(
    state: LampStrategyFormState,
    policyContent: JsonObject?,
    policyActionTypes: List<Pair<Long, KeyValue>>
) {
    Text(
        text = "任务配置",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    val maxSize = StrategyContentUtil.getPolicyItemMaxSize(
        jsonObject = policyContent,
        key = state.selectedPolicyType!!.second.key
    )

    state.timeTasks.forEachIndexed { index, task ->
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "时间节点 ${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.timeTasks.size > 1) {
                        IconButton(onClick = { state.timeTasks.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, "删除配置", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                OutlinedTextField(
                    value = task.time,
                    onValueChange = {},
                    readOnly = true,
                    label = { RequiredLabel("执行时间") },
                    placeholder = { Text("请点击选择") },
                    trailingIcon = { Icon(Icons.Default.DateRange, "选择时间") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { state.editingTimeIndex = index },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                if (policyActionTypes.isNotEmpty()) {
                    CommonDropdownMenu(
                        items = policyActionTypes,
                        selectedItem = task.actionType,
                        itemLabel = { it.second.value },
                        label = "执行动作",
                        placeholder = "请选择",
                        onItemSelected = { state.timeTasks[index] = task.copy(actionType = it) }
                    )
                }

                OutlinedTextField(
                    value = task.actionValue,
                    onValueChange = { state.timeTasks[index] = task.copy(actionValue = it) },
                    label = { RequiredLabel("操作值") },
                    placeholder = { Text("请输入数值") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }

    val isAddEnabled = state.timeTasks.size < maxSize
    TextButton(
        onClick = { state.timeTasks.add(TimeTaskConfig(id = state.nextTaskId++)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isAddEnabled
    ) {
        Text(
            text = if (isAddEnabled) "+ 添加时间节点 (上限 ${maxSize} 个)" else "已达到最大配置数量 (${maxSize})",
            color = if (isAddEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LngLatStrategySection(
    state: LampStrategyFormState,
    policyActionTypes: List<Pair<Long, KeyValue>>
) {
    Text(
        text = "经纬度(日出日落)配置",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    // 日出卡片
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🌅 日出配置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            LngLatConfigRow(
                offset = state.sunriseOffset,
                onOffsetChange = { state.sunriseOffset = it },
                actionType = state.sunriseActionType,
                onActionTypeChange = { state.sunriseActionType = it },
                actionValue = state.sunriseActionValue,
                onActionValueChange = { state.sunriseActionValue = it },
                policyActionTypes = policyActionTypes
            )
        }
    }

    // 日落卡片
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🌇 日落配置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            LngLatConfigRow(
                offset = state.sunsetOffset,
                onOffsetChange = { state.sunsetOffset = it },
                actionType = state.sunsetActionType,
                onActionTypeChange = { state.sunsetActionType = it },
                actionValue = state.sunsetActionValue,
                onActionValueChange = { state.sunsetActionValue = it },
                policyActionTypes = policyActionTypes
            )
        }
    }
}

@Composable
private fun LngLatConfigRow(
    offset: String, onOffsetChange: (String) -> Unit,
    actionType: Pair<Long, KeyValue>?, onActionTypeChange: (Pair<Long, KeyValue>) -> Unit,
    actionValue: String, onActionValueChange: (String) -> Unit,
    policyActionTypes: List<Pair<Long, KeyValue>>
) {
    OutlinedTextField(
        value = offset,
        onValueChange = { if (it.isEmpty() || it == "-" || it.toIntOrNull() != null) onOffsetChange(it) },
        label = { RequiredLabel("偏移量 (分钟)") },
        placeholder = { Text("正为延后，负为提前") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text("min") }
    )

    if (policyActionTypes.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CommonDropdownMenu(
                    items = policyActionTypes,
                    selectedItem = actionType,
                    itemLabel = { it.second.value },
                    label = "执行动作 *",
                    placeholder = "请选择",
                    onItemSelected = onActionTypeChange
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = actionValue,
                    onValueChange = onActionValueChange,
                    label = { RequiredLabel("操作值") },
                    placeholder = { Text("请输入数值") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}


// ==========================================
// 6. 辅助工具与逻辑
// ==========================================
@Composable
private fun RequiredLabel(text: String) {
    Text(buildAnnotatedString {
        append(text)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
            append(" *")
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogAndBottomSheetHandler(
    state: LampStrategyFormState,
    viewModel: LampViewModel,
    dateFormatter: SimpleDateFormat
) {
    // 1. 分组选择 BottomSheet
    if (state.showGroupBottomSheet) {
        val lampStrategyGroupInfoFlow = viewModel.lampStrategyGroupInfoFlow.collectAsLazyPagingItems()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        StrategyGroupBottomSheet(
            sheetState = sheetState,
            lampStrategyGroupInfoFlow = lampStrategyGroupInfoFlow,
            selectedGroups = state.selectedGroups,
            onDismissRequest = { state.showGroupBottomSheet = false }
        )
    }

    // 2. 日期选择弹窗
    if (state.showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.startDateMillis,
            initialSelectedEndDateMillis = state.endDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { state.showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.startDateMillis = dateRangePickerState.selectedStartDateMillis
                        state.endDateMillis = dateRangePickerState.selectedEndDateMillis
                        state.showDateRangePicker = false
                    },
                    enabled = dateRangePickerState.selectedEndDateMillis != null
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { state.showDateRangePicker = false }) { Text("取消") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f),
                title = { Text(text = "指定策略生效周期", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
                headline = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateRangePickerState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "开始日期",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(" - ", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = dateRangePickerState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: "结束日期",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            )
        }
    }

    // 3. 时间选择弹窗
    if (state.editingTimeIndex != null) {
        val timePickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { state.editingTimeIndex = null },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    val currentIndex = state.editingTimeIndex!!
                    state.timeTasks[currentIndex] = state.timeTasks[currentIndex].copy(time = newTime)
                    state.editingTimeIndex = null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { state.editingTimeIndex = null }) { Text("取消") }
            },
            text = { TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth()) }
        )
    }
}

// ==========================================
// 7. 数据构建与提交逻辑提取
// ==========================================
private fun submitStrategy(
    state: LampStrategyFormState,
    viewModel: LampViewModel,
    dateFormatter: SimpleDateFormat
) {
    val strategyContent = mutableListOf<JsonObject>()
    val timeType = state.selectedPolicyPeriod?.first?.toInt()
    val weekString = if (timeType == 2) state.selectedDaysOfWeek.sorted().joinToString(",") else ""

    val daysData = if (timeType == 3) {
        DayData(
            startTime = state.startDateMillis?.let { dateFormatter.format(Date(it)) },
            endTime = state.endDateMillis?.let { dateFormatter.format(Date(it)) }
        )
    } else DayData()

    when (state.selectedPolicyType?.second?.key) {
        "timeStrategies" -> {
            state.timeTasks.forEach { e ->
                val require = TimeStrategyCondition(
                    timeType = timeType.toString(),
                    priority = state.selectedPriority,
                    timePoint = e.time,
                    week = weekString,
                    days = daysData
                )
                val action = StrategyAction(
                    actionType = e.actionType?.first?.toString(),
                    actionValue = e.actionValue.toIntOrNull()
                )
                val content = TimeStrategyContent(id = e.id.toLong(), action = action, require = require)
                JsonUtils.toGsonJsonObject(content)?.let { strategyContent.add(it) }
            }
        }
        "lngLatStrategies" -> {
            // 构造日出
            val sunRiseRequire = LngLatStrategyCondition(
                timeType = timeType.toString(), priority = state.selectedPriority,
                week = weekString, days = daysData,
                riseDown = RiseDown(riseType = "1", sundown = 0, sunrise = state.sunriseOffset.toIntOrNull() ?: 0)
            )
            val sunRiseContent = LngLatStrategyContent(
                require = sunRiseRequire,
                action = StrategyAction(actionType = state.sunriseActionType?.first.toString(), actionValue = state.sunriseActionValue.toIntOrNull())
            )
            JsonUtils.toGsonJsonObject(sunRiseContent)?.let { strategyContent.add(it) }

            // 构造日落
            val sunSetRequire = LngLatStrategyCondition(
                timeType = timeType.toString(), priority = state.selectedPriority,
                week = weekString, days = daysData,
                riseDown = RiseDown(riseType = "2", sundown = state.sunsetOffset.toIntOrNull() ?: 0, sunrise = 0)
            )
            val sunSetContent = LngLatStrategyContent(
                require = sunSetRequire,
                action = StrategyAction(actionType = state.sunsetActionType?.first.toString(), actionValue = state.sunsetActionValue.toIntOrNull())
            )
            JsonUtils.toGsonJsonObject(sunSetContent)?.let { strategyContent.add(it) }
        }
    }

    if (strategyContent.isNotEmpty()) {
        val strategyDTO = StrategyDTO(
            name = state.strategyName,
            productId = state.selectedProduct?.productId,
            groupId = state.selectedGroups.map { it.groupId as Long },
            strategyClass = state.selectedPolicyType?.first?.toInt(),
            strategyType = state.selectedStrategyType?.first?.toInt(),
            content = strategyContent,
            description = state.remarkInfo,
            executeType = 0,
            subSystemType = 1
        )
        viewModel.saveStrategyAndSync(strategyDTO)
    }
}