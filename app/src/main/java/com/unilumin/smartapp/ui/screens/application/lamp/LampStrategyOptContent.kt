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
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
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
import com.unilumin.smartapp.client.data.StrategyDTO
import com.unilumin.smartapp.client.data.StrategyGroupListVO
import com.unilumin.smartapp.client.data.StrategyProductVO
import com.unilumin.smartapp.client.data.TimeStrategyAction
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
import com.unilumin.smartapp.util.StrategyContentUtil.getPolicyActionTypes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LampStrategyOptContent(
    lampViewModel: LampViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.getGroupProductList()
    }

    val lampStrategyGroupInfoFlow =
        lampViewModel.lampStrategyGroupInfoFlow.collectAsLazyPagingItems()
    val strategyGroupProductList by lampViewModel.strategyGroupProductList.collectAsState()
    val strategyTypeList by lampViewModel.strategyTypeList.collectAsState()
    val policyTypeList by lampViewModel.policyTypeList.collectAsState()
    val policyContent by lampViewModel.policyContent.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) }

    // --- Step 1: 基本信息表单状态 ---
    var strategyName by remember { mutableStateOf("") }
    var remarkInfo by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<StrategyProductVO?>(null) }
    var selectedStrategyType by remember { mutableStateOf<Pair<Long, KeyValue>?>(null) }
    var selectedPolicyType by remember { mutableStateOf<Pair<Long, KeyValue>?>(null) }
    var showGroupBottomSheet by remember { mutableStateOf(false) }
    val selectedGroups = remember { mutableStateListOf<StrategyGroupListVO>() }

    // --- Step 2: 策略详情通用状态 ---
    var selectedPolicyPeriod by remember { mutableStateOf<Pair<Long, KeyValue>?>(null) }
    val selectedDaysOfWeek = remember { mutableStateListOf<Int>() }
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedPriority by remember { mutableStateOf<Int?>(null) }

    // --- Time Strategies 专属状态 ---
    // nextTaskId 保证无论怎么增删，ID 都能持续递增，作为唯一标识
    var nextTaskId by remember { mutableIntStateOf(1) }
    val timeTasks = remember { mutableStateListOf(TimeTaskConfig(id = 0)) }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    // --- LngLat Strategies 专属状态 (严格隔离: 同时需要日出和日落) ---
    var sunriseOffset by remember { mutableStateOf("") }
    var sunriseActionType by remember { mutableStateOf<Pair<Long, KeyValue>?>(null) }
    var sunriseActionValue by remember { mutableStateOf("") }

    var sunsetOffset by remember { mutableStateOf("") }
    var sunsetActionType by remember { mutableStateOf<Pair<Long, KeyValue>?>(null) }
    var sunsetActionValue by remember { mutableStateOf("") }

    // 统一的弹窗控制
    var showDateRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDateMillis,
        initialSelectedEndDateMillis = endDateMillis
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 帮助生成带必填星号的Label
    val requiredLabel: @Composable (String) -> Unit = { text ->
        Text(buildAnnotatedString {
            append(text)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                append(" *")
            }
        })
    }

    BackHandler(enabled = true) {
        if (currentStep == 2) currentStep = 1 else onBack()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = if (currentStep == 1) "新建策略 (1/2)" else "策略详情 (2/2)",
                onBack = { if (currentStep == 2) currentStep = 1 else onBack() }
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
                currentStep = currentStep - 1
            )

            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(300)) { it } + fadeIn() togetherWith slideOutHorizontally(tween(300)) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(tween(300)) { -it } + fadeIn() togetherWith slideOutHorizontally(tween(300)) { it } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedTextField(
                                value = strategyName,
                                onValueChange = { strategyName = it },
                                label = { requiredLabel("策略名称") },
                                placeholder = { Text("请输入策略名称") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            CommonDropdownMenu(
                                items = strategyGroupProductList,
                                selectedItem = selectedProduct,
                                itemLabel = { it.productName },
                                label = "所属产品",
                                placeholder = "请选择产品",
                                onItemSelected = { product ->
                                    selectedProduct = product
                                    selectedGroups.clear()
                                    selectedStrategyType = null
                                    selectedPolicyType = null
                                    lampViewModel.updateCurrentProductId(product.productId)
                                }
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (selectedGroups.isEmpty()) "" else "已选择 ${selectedGroups.size} 个分组",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("关联分组 (多选)") },
                                    placeholder = { Text("请点击选择关联分组") },
                                    trailingIcon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                                    modifier = Modifier.fillMaxWidth().clickable { showGroupBottomSheet = true },
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

                                if (selectedGroups.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selectedGroups.forEach { group ->
                                            InputChip(
                                                selected = true,
                                                onClick = { selectedGroups.removeAll { it.groupId == group.groupId } },
                                                label = { Text(group.groupName ?: "未知分组") },
                                                trailingIcon = { Icon(Icons.Default.Close, "移除", Modifier.size(16.dp)) }
                                            )
                                        }
                                    }
                                }
                            }

                            if (strategyTypeList.isNotEmpty()) {
                                CommonDropdownMenu(
                                    items = strategyTypeList,
                                    selectedItem = selectedStrategyType,
                                    itemLabel = { it.second.value },
                                    label = "策略类型",
                                    placeholder = "请选择策略类型",
                                    onItemSelected = { selectedStrategyType = it }
                                )
                            }
                            if (policyTypeList.isNotEmpty()) {
                                CommonDropdownMenu(
                                    items = policyTypeList,
                                    selectedItem = selectedPolicyType,
                                    itemLabel = { it.second.value },
                                    label = "策略模式",
                                    placeholder = "请选择策略模式",
                                    onItemSelected = { selectedPolicyType = it }
                                )
                            }

                            OutlinedTextField(
                                value = remarkInfo,
                                onValueChange = { remarkInfo = it },
                                label = { Text("备注信息") },
                                placeholder = { Text("请输入备注信息 (选填)") },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = { currentStep = 2 },
                                enabled = strategyName.isNotBlank() && selectedProduct != null,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("下一步：配置策略详情", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    2 -> {
                        val policyActionTypes = remember(selectedProduct, selectedPolicyType, policyContent) {
                            if (selectedProduct != null && selectedPolicyType != null) {
                                getPolicyActionTypes(
                                    productId = selectedProduct!!.productId,
                                    jsonObject = policyContent,
                                    key = selectedPolicyType!!.second.key
                                )
                            } else emptyList()
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val policyPriorityRange = StrategyContentUtil.getPolicyPriorityRange(
                                policyContent,
                                selectedPolicyType?.second?.key.toString()
                            )
                            val policyPeriodTypes = StrategyContentUtil.getPolicyPeriodTypes(
                                policyContent,
                                selectedPolicyType?.second?.key.toString()
                            )

                            LaunchedEffect(policyPriorityRange) {
                                if (policyPriorityRange != null) {
                                    if (selectedPriority == null || selectedPriority!! < policyPriorityRange.min || selectedPriority!! > policyPriorityRange.max) {
                                        selectedPriority = policyPriorityRange.min
                                    }
                                } else {
                                    selectedPriority = null
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (policyPriorityRange != null) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        val priorityList = (policyPriorityRange.min..policyPriorityRange.max).toList()
                                        CommonDropdownMenu(
                                            items = priorityList,
                                            selectedItem = selectedPriority,
                                            itemLabel = { it.toString() },
                                            label = "策略优先级",
                                            placeholder = "请选择",
                                            onItemSelected = { selectedPriority = it }
                                        )
                                    }
                                }

                                if (policyPeriodTypes.isNotEmpty()) {
                                    Box(modifier = Modifier.weight(2f)) {
                                        CommonDropdownMenu(
                                            items = policyPeriodTypes,
                                            selectedItem = selectedPolicyPeriod,
                                            itemLabel = { it.second.value },
                                            label = "生效周期",
                                            placeholder = "请选择",
                                            onItemSelected = { data ->
                                                selectedPolicyPeriod = data
                                                selectedDaysOfWeek.clear()
                                                startDateMillis = null
                                                endDateMillis = null
                                            }
                                        )
                                    }
                                }
                            }

                            if (selectedPolicyPeriod != null && selectedPolicyPeriod?.first!! > 1) {
                                when (selectedPolicyPeriod?.first) {
                                    2L -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                val weekDays = listOf(1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日")
                                                weekDays.forEach { (dayValue, dayName) ->
                                                    val isSelected = selectedDaysOfWeek.contains(dayValue)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                            .clickable {
                                                                if (isSelected) selectedDaysOfWeek.remove(dayValue)
                                                                else selectedDaysOfWeek.add(dayValue)
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = dayName,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    3L -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showDateRangePicker = true },
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = startDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
                                                    onValueChange = {}, readOnly = true, label = { Text("开始日期") }, placeholder = { Text("请选择") },
                                                    trailingIcon = { Icon(Icons.Default.DateRange, null) }, modifier = Modifier.weight(1f), enabled = false,
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                                OutlinedTextField(
                                                    value = endDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
                                                    onValueChange = {}, readOnly = true, label = { Text("结束日期") }, placeholder = { Text("请选择") },
                                                    trailingIcon = { Icon(Icons.Default.DateRange, null) }, modifier = Modifier.weight(1f), enabled = false,
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            when (selectedPolicyType?.second?.key) {
                                // --- 时间策略 ---
                                "timeStrategies" -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            "任务配置",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val maxSize = StrategyContentUtil.getPolicyItemMaxSize(
                                            jsonObject = policyContent,
                                            key = selectedPolicyType!!.second.key
                                        )

                                        timeTasks.forEachIndexed { index, task ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // 这里可以直接用 ID 或 Index，Index 对用户更友好，因为它是连续的 (1, 2, 3...)
                                                    Text(
                                                        "配置项 ${index + 1}",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    if (timeTasks.size > 1) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "删除配置",
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clickable {
                                                                    timeTasks.removeAt(index)
                                                                },
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                                OutlinedTextField(
                                                    value = task.time,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { requiredLabel("执行时间") },
                                                    placeholder = { Text("请点击选择时间") },
                                                    trailingIcon = { Icon(Icons.Default.DateRange, "选择时间") },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { editingTimeIndex = index },
                                                    enabled = false,
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                        onItemSelected = { newActionType ->
                                                            timeTasks[index] = task.copy(actionType = newActionType)
                                                        }
                                                    )
                                                }

                                                OutlinedTextField(
                                                    value = task.actionValue,
                                                    onValueChange = { newValue ->
                                                        timeTasks[index] = task.copy(actionValue = newValue)
                                                    },
                                                    label = { requiredLabel("操作值") },
                                                    placeholder = { Text("请输入操作值") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                )
                                            }
                                        }
                                        val isAddEnabled = timeTasks.size < maxSize
                                        TextButton(
                                            onClick = { timeTasks.add(TimeTaskConfig(id = nextTaskId++)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = isAddEnabled
                                        ) {
                                            Text(
                                                text = if (isAddEnabled) "+ 添加配置项(最大${maxSize}条)" else "已达到最大配置数量 (${maxSize})",
                                                color = if (isAddEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }

                                "lngLatStrategies" -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "经纬度(日出日落)配置",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // --- 日出配置卡片 ---
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text(
                                                "日出配置",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            OutlinedTextField(
                                                value = sunriseOffset,
                                                onValueChange = { newValue ->
                                                    if (newValue.isEmpty() || newValue == "-" || newValue.toIntOrNull() != null) sunriseOffset = newValue
                                                },
                                                label = { requiredLabel("偏移量-分钟") },
                                                placeholder = { Text("正延后/负提前") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                suffix = { Text("min") }
                                            )

                                            if (policyActionTypes.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        CommonDropdownMenu(
                                                            items = policyActionTypes,
                                                            selectedItem = sunriseActionType,
                                                            itemLabel = { it.second.value },
                                                            label = "执行动作 *",
                                                            placeholder = "请选择",
                                                            onItemSelected = { sunriseActionType = it }
                                                        )
                                                    }
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        OutlinedTextField(
                                                            value = sunriseActionValue,
                                                            onValueChange = { sunriseActionValue = it },
                                                            label = { requiredLabel("操作值") },
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

                                        // --- 日落配置卡片 ---
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text(
                                                "日落配置",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            OutlinedTextField(
                                                value = sunsetOffset,
                                                onValueChange = { newValue ->
                                                    if (newValue.isEmpty() || newValue == "-" || newValue.toIntOrNull() != null) sunsetOffset = newValue
                                                },
                                                label = { requiredLabel("偏移量-分钟") },
                                                placeholder = { Text("正延后/负提前") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                suffix = { Text("min") }
                                            )

                                            if (policyActionTypes.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        CommonDropdownMenu(
                                                            items = policyActionTypes,
                                                            selectedItem = sunsetActionType,
                                                            itemLabel = { it.second.value },
                                                            label = "执行动作 *",
                                                            placeholder = "请选择",
                                                            onItemSelected = { sunsetActionType = it }
                                                        )
                                                    }
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        OutlinedTextField(
                                                            value = sunsetActionValue,
                                                            onValueChange = { sunsetActionValue = it },
                                                            label = { requiredLabel("操作值") },
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
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    val strategyContent = mutableListOf<JsonObject>()
                                    when (selectedPolicyType?.second?.key) {
                                        "timeStrategies" -> {
                                            val timeType = selectedPolicyPeriod?.first?.toInt()

                                            // 优化点 1：将相同策略下重复计算的值提取到循环外部，提升性能
                                            val weekString = if (timeType == 2) selectedDaysOfWeek.sorted().joinToString(",") else null
                                            val daysData = if (timeType == 3) {
                                                DayData(
                                                    startTime = startDateMillis?.let { dateFormatter.format(Date(it)) },
                                                    endTime = endDateMillis?.let { dateFormatter.format(Date(it)) }
                                                )
                                            } else null

                                            timeTasks.forEach { e ->
                                                // 优化点 2：将 var 替换为 val，符合规范
                                                val require = TimeStrategyCondition(
                                                    timeType = timeType,
                                                    priority = selectedPriority,
                                                    timePoint = e.time,
                                                    week = weekString,
                                                    days = daysData
                                                )
                                                val action = TimeStrategyAction(
                                                    actionType = e.actionType?.first?.toInt(),
                                                    actionValue = e.actionValue.toIntOrNull()
                                                )

                                                val timeStrategyContent = TimeStrategyContent(
                                                    action = action,
                                                    require = require
                                                )

                                                // 优化点 3：统一使用 let 作用域函数添加对象
                                                JsonUtils.toGsonJsonObject(timeStrategyContent)?.let {
                                                    strategyContent.add(it)
                                                }
                                            }
                                        }
                                        "lngLatStrategies" -> {
                                            // TODO: 组装经纬度参数
                                        }
                                    }
                                    val strategyDTO = StrategyDTO(
                                        name = strategyName,
                                        productId = selectedProduct?.productId,
                                        groupId = selectedGroups.map { it.groupId as Long },
                                        strategyClass = selectedPolicyType?.first?.toInt(),
                                        strategyType = selectedStrategyType?.first?.toInt(),
                                        content = strategyContent,
                                        description = remarkInfo,
                                        executeType = 0,
                                        subSystemType = 1
                                    )
                                    lampViewModel.saveStrategyAndSync(strategyDTO)
                                },
                                enabled = true,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("提交并同步", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // --- 抽屉与弹窗组件 ---
        if (showGroupBottomSheet) {
            StrategyGroupBottomSheet(
                sheetState = sheetState,
                lampStrategyGroupInfoFlow = lampStrategyGroupInfoFlow,
                selectedGroups = selectedGroups,
                onDismissRequest = { showGroupBottomSheet = false }
            )
        }

        // 日期选择弹窗
        if (showDateRangePicker) {
            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDateMillis = dateRangePickerState.selectedStartDateMillis
                            endDateMillis = dateRangePickerState.selectedEndDateMillis
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedEndDateMillis != null
                    ) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) { Text("取消") }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f),
                    title = {
                        Text(
                            text = "指定策略生效周期",
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                        )
                    },
                    headline = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = dateRangePickerState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "开始日期", style = MaterialTheme.typography.titleMedium)
                            Text(text = " - ", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
                            Text(text = dateRangePickerState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: "结束日期", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                )
            }
        }

        // 时间选择弹窗 (TimePicker)
        if (editingTimeIndex != null) {
            AlertDialog(
                onDismissRequest = { editingTimeIndex = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val hour = timePickerState.hour.toString().padStart(2, '0')
                            val minute = timePickerState.minute.toString().padStart(2, '0')
                            val newTime = "$hour:$minute"
                            val currentIndex = editingTimeIndex!!
                            timeTasks[currentIndex] = timeTasks[currentIndex].copy(time = newTime)
                            editingTimeIndex = null
                        }
                    ) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { editingTimeIndex = null }) { Text("取消") }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}
