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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.KeyValue
import com.unilumin.smartapp.client.data.StrategyGroupListVO
import com.unilumin.smartapp.client.data.StrategyProductVO
import com.unilumin.smartapp.ui.components.CommonDropdownMenu
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.StepProgressIndicator
import com.unilumin.smartapp.ui.screens.dialog.StrategyGroupBottomSheet
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.util.StrategyContentUtil
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

    val lampStrategyGroupInfoFlow = lampViewModel.lampStrategyGroupInfoFlow.collectAsLazyPagingItems()
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

    // --- Step 2: 策略详情表单状态 ---
    var selectedPolicyPeriod by remember { mutableStateOf<Pair<Long, KeyValue>?>(null) }
    val selectedDaysOfWeek = remember { mutableStateListOf<Int>() }
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedPriority by remember { mutableStateOf<Int?>(null) }

    // 统一的日期范围选择器弹窗控制
    var showDateRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDateMillis,
        initialSelectedEndDateMillis = endDateMillis
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

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
                                .padding(horizontal = 16.dp, vertical = 12.dp), // 优化：减小外边距，使页面更紧凑
                            verticalArrangement = Arrangement.spacedBy(14.dp) // 优化：减小控件间距 (原20.dp)
                        ) {
                            OutlinedTextField(
                                value = strategyName,
                                onValueChange = { strategyName = it },
                                label = { Text("策略名称") },
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showGroupBottomSheet = true },
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
                                                trailingIcon = { Icon(Icons.Default.Close, "移除该分组", Modifier.size(16.dp)) }
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp), // 优化：稍微减小多行输入框的高度
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = { currentStep = 2 },
                                enabled = strategyName.isNotBlank() && selectedProduct != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp), // 优化：采用标准 48dp 按钮高度
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("下一步：配置策略详情", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    2 -> {
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
                                                val weekDays = listOf(
                                                    1 to "一", 2 to "二", 3 to "三",
                                                    4 to "四", 5 to "五", 6 to "六", 7 to "日"
                                                )
                                                weekDays.forEach { (dayValue, dayName) ->
                                                    val isSelected = selectedDaysOfWeek.contains(dayValue)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp) // 优化：稍微减小圆形，看起来更精致
                                                            .clip(CircleShape)
                                                            .background(
                                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                            )
                                                            .clickable {
                                                                if (isSelected) selectedDaysOfWeek.remove(dayValue)
                                                                else selectedDaysOfWeek.add(dayValue)
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = dayName,
                                                            style = MaterialTheme.typography.bodyMedium, // 字体微调配合小圆
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
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) { showDateRangePicker = true },
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = startDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("开始日期") },
                                                    placeholder = { Text("请选择") },
                                                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                                                    modifier = Modifier.weight(1f),
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
                                                OutlinedTextField(
                                                    value = endDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("结束日期") },
                                                    placeholder = { Text("请选择") },
                                                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                                                    modifier = Modifier.weight(1f),
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
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            val isSubmitEnabled = when (selectedPolicyPeriod?.first) {
                                2L -> selectedDaysOfWeek.isNotEmpty() && selectedPriority != null
                                3L -> startDateMillis != null && endDateMillis != null && selectedPriority != null
                                else -> selectedPolicyPeriod != null && selectedPriority != null
                            }

                            Button(
                                onClick = {
                                    // TODO: 提交逻辑
                                },
                                enabled = isSubmitEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("完成并保存", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        if (showGroupBottomSheet) {
            StrategyGroupBottomSheet(
                sheetState = sheetState,
                lampStrategyGroupInfoFlow = lampStrategyGroupInfoFlow,
                selectedGroups = selectedGroups,
                onDismissRequest = { showGroupBottomSheet = false }
            )
        }

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
                    ) {
                        Text("确定")
                    }
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
                            text = "选择日期范围",
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                        )
                    },
                    headline = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 优化：修改了此处的字体大小为 titleMedium，防止换行
                            Text(
                                text = dateRangePickerState.selectedStartDateMillis?.let {
                                    dateFormatter.format(Date(it))
                                } ?: "开始日期",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = " - ",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 4.dp) // 减小连接符两边的留白
                            )
                            Text(
                                text = dateRangePickerState.selectedEndDateMillis?.let {
                                    dateFormatter.format(Date(it))
                                } ?: "结束日期",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                )
            }
        }
    }
}