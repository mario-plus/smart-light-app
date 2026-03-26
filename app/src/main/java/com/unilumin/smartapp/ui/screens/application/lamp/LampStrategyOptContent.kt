package com.unilumin.smartapp.ui.screens.application.lamp

import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.unilumin.smartapp.client.data.LampStrategyInfo
import com.unilumin.smartapp.client.data.LngLatStrategyCondition
import com.unilumin.smartapp.client.data.LngLatStrategyContent
import com.unilumin.smartapp.client.data.PolicyConfig
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
import com.unilumin.smartapp.util.StrategyContentUtil.formatLngLatStrategy
import com.unilumin.smartapp.util.StrategyContentUtil.formatTimeStrategy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "LampStrategyForm"

class LampStrategyFormState {
    var currentStep by mutableIntStateOf(1)

    //策略名称
    var strategyName by mutableStateOf("")

    //备注信息
    var remarkInfo by mutableStateOf("")

    //所属产品
    var selectedProduct by mutableStateOf<StrategyProductVO?>(null)

    //策略类型
    var selectedStrategyType by mutableStateOf<Pair<Long, KeyValue>?>(null)

    //策略模式(时间，经纬度)
    var selectedPolicyType by mutableStateOf<Pair<Long, KeyValue>?>(null)

    //分组选择控制标识
    var showGroupBottomSheet by mutableStateOf(false)

    //所选产品
    val selectedGroups = mutableStateListOf<StrategyGroupListVO>()

    //策略周期(1每天,2星期,3指定日期)
    var selectedPolicyPeriod by mutableStateOf<Pair<Long?, KeyValue>?>(null)

    //所选星期
    val selectedDaysOfWeek = mutableStateListOf<Int>()

    //指定开始日期
    var startDateMillis by mutableStateOf<Long?>(null)

    //指定结束日期
    var endDateMillis by mutableStateOf<Long?>(null)

    //策略优先级
    var selectedPriority by mutableStateOf<Int?>(null)

    //策略编号
    var nextTaskId by mutableIntStateOf(1)

    //时间任务
    val timeTasks = mutableStateListOf(TimeTaskConfig(id = 0))
    var editingTimeIndex by mutableStateOf<Int?>(null)

    //日出便宜量
    var sunriseOffset by mutableStateOf("")

    //动作类型
    var sunriseActionType by mutableStateOf<Pair<Long, KeyValue>?>(null)

    //动作值
    var sunriseActionValue by mutableStateOf("")

    //日落偏移量
    var sunsetOffset by mutableStateOf("")

    //动作类型
    var sunsetActionType by mutableStateOf<Pair<Long, KeyValue>?>(null)

    //动作值
    var sunsetActionValue by mutableStateOf("")

    //时间选择控制标识
    var showDateRangePicker by mutableStateOf(false)

    //初始化信息的策略id
    var initializedStrategyId by mutableStateOf<Long?>(null)

    //是否已选产品类型
    var hasBoundProduct by mutableStateOf(false)

    //是否已选策略类型
    var hasBoundStrategyType by mutableStateOf(false)

    //是否已选策略模式
    var hasBoundPolicyType by mutableStateOf(false)

    // 重置，避免脏数据
    fun reset() {
        currentStep = 1
        strategyName = ""
        remarkInfo = ""
        selectedProduct = null
        selectedStrategyType = null
        selectedPolicyType = null
        showGroupBottomSheet = false
        selectedGroups.clear()

        selectedPolicyPeriod = null
        selectedDaysOfWeek.clear()
        startDateMillis = null
        endDateMillis = null
        selectedPriority = null

        nextTaskId = 1
        timeTasks.clear()
        timeTasks.add(TimeTaskConfig(id = 0))
        editingTimeIndex = null

        sunriseOffset = ""
        sunriseActionType = null
        sunriseActionValue = ""

        sunsetOffset = ""
        sunsetActionType = null
        sunsetActionValue = ""

        showDateRangePicker = false

        initializedStrategyId = null
        hasBoundProduct = false
        hasBoundStrategyType = false
        hasBoundPolicyType = false
    }

    fun initStaticData(info: LampStrategyInfo, dateFormatter: SimpleDateFormat) {
        if (initializedStrategyId == info.id) return
        Log.d(TAG, "initStaticData: Start initializing strategy ID = ${info.id}")
        hasBoundProduct = false
        hasBoundStrategyType = false
        hasBoundPolicyType = false
        strategyName = info.name ?: ""
        remarkInfo = info.description ?: ""
        selectedGroups.clear()
        info.groups?.forEach { group ->
            selectedGroups.add(StrategyGroupListVO(groupId = group.id, groupName = group.name))
        }
        if (selectedPolicyType == null) {
            val fallbackKey = when (info.strategyClass) {
                2 -> "timeStrategies"
                1 -> "lngLatStrategies"
                else -> ""
            }
            if (fallbackKey.isNotEmpty()) {
                selectedPolicyType = Pair(
                    info.strategyClass?.toLong() ?: 0L,
                    KeyValue(fallbackKey, "模式(${info.strategyClass})")
                )
            }
        }

        when (info.strategyClass) {
            2 -> { // 时间策略
                val timeStrategies = formatTimeStrategy(info.contents)
                if (timeStrategies.isNotEmpty()) {
                    val firstReq = timeStrategies.first().require
                    selectedPriority = firstReq.priority

                    val timeType = firstReq.timeType?.toLong()
                    val periodName = when (timeType) {
                        1L -> "每天"
                        2L -> "星期"
                        3L -> "时间区间"
                        else -> "类型($timeType)"
                    }
                    selectedPolicyPeriod = Pair(timeType, KeyValue(timeType.toString(), periodName))

                    if (timeType == 2L) {
                        selectedDaysOfWeek.clear()
                        firstReq.week?.split(",")?.mapNotNull { it.toIntOrNull() }
                            ?.let { selectedDaysOfWeek.addAll(it) }
                    } else if (timeType == 3L) {
                        startDateMillis =
                            firstReq.days?.startTime?.let { runCatching { dateFormatter.parse(it)?.time }.getOrNull() }
                        endDateMillis =
                            firstReq.days?.endTime?.let { runCatching { dateFormatter.parse(it)?.time }.getOrNull() }
                    }

                    timeTasks.clear()
                    timeStrategies.forEachIndexed { index, ts ->
                        val rawActionType = ts.action.actionType
                        val fallbackActionPair = rawActionType?.let {
                            Pair(
                                it.toLongOrNull() ?: 0L,
                                KeyValue(it, "动作($it)")
                            )
                        }

                        timeTasks.add(
                            TimeTaskConfig(
                                id = index + 1,
                                time = ts.require.timePoint ?: "",
                                actionValue = ts.action.actionValue?.toString() ?: "",
                                actionType = fallbackActionPair
                            )
                        )
                    }
                    nextTaskId = timeTasks.size + 1
                }
            }

            1 -> { // 经纬度策略
                val lngLatStrategies = formatLngLatStrategy(info.contents)
                if (lngLatStrategies.isNotEmpty()) {
                    val firstReq = lngLatStrategies.first().require
                    selectedPriority = firstReq.priority

                    val timeType = firstReq.timeType?.toLong()
                    val periodName = when (timeType) {
                        1L -> "每天"
                        2L -> "星期"
                        3L -> "时间区间"
                        else -> "类型($timeType)"
                    }
                    selectedPolicyPeriod = Pair(timeType, KeyValue(timeType.toString(), periodName))
                    if (timeType == 2L) {
                        selectedDaysOfWeek.clear()
                        firstReq.week?.split(",")?.mapNotNull { it.toIntOrNull() }
                            ?.let { selectedDaysOfWeek.addAll(it) }
                    }
                    lngLatStrategies.forEach { ts ->
                        val isSunrise = ts.require.riseDown?.riseType == "1"
                        val rawActionType = ts.action.actionType
                        val fallbackActionPair = rawActionType?.let {
                            Pair(
                                it.toLongOrNull() ?: 0L,
                                KeyValue(it, "动作($it)")
                            )
                        }
                        if (isSunrise) {
                            sunriseOffset = ts.require.riseDown.sunrise.toString()
                            sunriseActionValue = ts.action.actionValue?.toString() ?: ""
                            sunriseActionType = fallbackActionPair
                        } else {
                            sunsetOffset = ts.require.riseDown?.sundown.toString()
                            sunsetActionValue = ts.action.actionValue?.toString() ?: ""
                            sunsetActionType = fallbackActionPair
                        }
                    }
                }
            }
        }

        initializedStrategyId = info.id
    }

    fun tryBindProduct(info: LampStrategyInfo, productList: List<StrategyProductVO>) {
        if (!hasBoundProduct && productList.isNotEmpty()) {
            selectedProduct = productList.find { it.productId == info.productId }
            hasBoundProduct = true
        }
    }

    fun tryBindTypes(
        info: LampStrategyInfo,
        strategyTypes: List<Pair<Long, KeyValue>>,
        policyTypes: List<Pair<Long, KeyValue>>
    ) {
        if (!hasBoundStrategyType && strategyTypes.isNotEmpty()) {
            selectedStrategyType = strategyTypes.find { it.first.toInt() == info.strategyType }
            hasBoundStrategyType = true
        }
        if (!hasBoundPolicyType && policyTypes.isNotEmpty()) {
            val match = policyTypes.find { it.first.toInt() == info.strategyClass }
            if (match != null) selectedPolicyType = match
            hasBoundPolicyType = true
        }
    }

    fun bindActions(policyActionTypes: List<Pair<Long, KeyValue>>) {
        if (policyActionTypes.isEmpty()) return
        Log.d(TAG, "bindActions: Binding with ${policyActionTypes.size} available actions.")
        timeTasks.forEachIndexed { index, task ->
            val rawKey = task.actionType?.second?.key ?: task.actionType?.first?.toString()
            if (rawKey != null) {
                val match = policyActionTypes.find {
                    it.first.toString() == rawKey || it.second.key == rawKey
                }
                if (match != null && task.actionType?.second?.value != match.second.value) {
                    timeTasks[index] = task.copy(actionType = match)
                }
            }
        }
        val srRawKey = sunriseActionType?.second?.key ?: sunriseActionType?.first?.toString()
        if (srRawKey != null) {
            val match =
                policyActionTypes.find { it.first.toString() == srRawKey || it.second.key == srRawKey }
            if (match != null && sunriseActionType?.second?.value != match.second.value) {
                sunriseActionType = match
            }
        }
        val ssRawKey = sunsetActionType?.second?.key ?: sunsetActionType?.first?.toString()
        if (ssRawKey != null) {
            val match =
                policyActionTypes.find { it.first.toString() == ssRawKey || it.second.key == ssRawKey }
            if (match != null && sunsetActionType?.second?.value != match.second.value) {
                sunsetActionType = match
            }
        }
    }
}

@Composable
fun rememberLampStrategyFormState() = remember { LampStrategyFormState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampStrategyOptContent(
    lampViewModel: LampViewModel,
    initialStrategy: LampStrategyInfo? = null,
    onBack: () -> Unit
) {
    val formState = rememberLampStrategyFormState()

    val dateFormatter = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }

    val strategyGroupProductList by lampViewModel.strategyGroupProductList.collectAsState()
    val strategyTypeList by lampViewModel.strategyTypeList.collectAsState()
    val policyTypeList by lampViewModel.policyTypeList.collectAsState()
    val policyContent by lampViewModel.policyContent.collectAsState()

    val currentProductId by lampViewModel.currentProductId.collectAsState()

    //产品变化，需要变更产品规则
    LaunchedEffect(currentProductId) {
        if (currentProductId != -1L) {
            Log.d(TAG, "Fetching product rule for productId: $currentProductId")
            lampViewModel.getProductRule(currentProductId)
        }
    }

    //页面初始化，需要加载产品列表
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.getGroupProductList()
    }

    // 处理初始化或重置逻辑
    LaunchedEffect(initialStrategy) {
        if (initialStrategy != null) {
            initialStrategy.productId?.let {
                lampViewModel.updateCurrentProductId(it)
            }
            formState.initStaticData(initialStrategy, dateFormatter)
        } else {
            formState.reset()
            lampViewModel.updateCurrentProductId(-1L)
        }
    }

    LaunchedEffect(initialStrategy, strategyGroupProductList) {
        if (initialStrategy != null) formState.tryBindProduct(
            initialStrategy,
            strategyGroupProductList
        )
    }

    LaunchedEffect(initialStrategy, strategyTypeList, policyTypeList) {
        if (initialStrategy != null) formState.tryBindTypes(
            initialStrategy,
            strategyTypeList,
            policyTypeList
        )
    }

    // === 【增加核心日志块：追踪 policyConfig 的生成与获取过程】 ===
    val policyConfig =
        remember(formState.selectedProduct, formState.selectedPolicyType, policyContent) {
            val product = formState.selectedProduct
            val policy = formState.selectedPolicyType

            Log.d(TAG, "=> policyConfig Recomposition triggered.")
            Log.d(TAG, "Product Name/ID: ${product?.productName} / ${product?.productId}")
            Log.d(TAG, "Policy Type: ${policy?.second?.value} (classId: ${policy?.first})")
            Log.d(TAG, "Is PolicyContent null? ${policyContent == null}")

            if (product != null && policy != null && policyContent != null) {
                val classId = policy.first.toInt()
                val realKey = policy.second.key.takeIf { !it.isNullOrBlank() } ?: when (classId) {
                    2 -> "timeStrategies"
                    1 -> "lngLatStrategies"
                    else -> ""
                }

                Log.d(TAG, "Extracting PolicyConfig with realKey: '$realKey'")

                val config = StrategyContentUtil.getPolicyConfig(
                    productId = product.productId,
                    jsonObject = policyContent,
                    key = realKey
                )

                Log.d(TAG, "Parsed PolicyConfig result -> ActionTypes Size: ${config.actionTypes.size}, PeriodTypes Size: ${config.periodTypes.size}")

                if (config.actionTypes.isEmpty()) {
                    Log.w(TAG, "⚠️ 警告: 该产品 (${product.productName}) 获取到的动作类型为空！请检查下发的 JSON 是否包含正确的节点 [$realKey]。")
                    // 如果数据不大，可以把获取到的完整 Json 打印一部分出来观察
                    Log.w(TAG, "JSON 截断内容: ${policyContent.toString().take(1000)}")
                }

                config
            } else {
                Log.d(TAG, "Returning empty PolicyConfig because dependent state is incomplete.")
                PolicyConfig()
            }
        }

    LaunchedEffect(policyConfig, formState.initializedStrategyId) {
        Log.d(TAG, "LaunchedEffect(policyConfig check): formId=${formState.initializedStrategyId}, actionSize=${policyConfig.actionTypes.size}")
        if (formState.initializedStrategyId != null && policyConfig.actionTypes.isNotEmpty()) {
            formState.bindActions(policyConfig.actionTypes)
        }
    }

    BackHandler(enabled = true) {
        if (formState.currentStep == 2) formState.currentStep = 1 else onBack()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = if (initialStrategy != null) {
                    if (formState.currentStep == 1) "编辑策略 (1/2)" else "编辑详情 (2/2)"
                } else {
                    if (formState.currentStep == 1) "新建策略 (1/2)" else "策略详情 (2/2)"
                },
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
                    2 -> StepTwoStrategyDetails(
                        formState,
                        lampViewModel,
                        dateFormatter,
                        policyConfig,
                        policyContent == null,
                        initialStrategy,
                        onBack
                    )
                }
            }
        }
        DialogAndBottomSheetHandler(formState, lampViewModel)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepOneBasicInfo(state: LampStrategyFormState, viewModel: LampViewModel) {
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
        if (state.selectedProduct != null) {
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

@Composable
private fun StepTwoStrategyDetails(
    state: LampStrategyFormState,
    viewModel: LampViewModel,
    dateFormatter: SimpleDateFormat,
    policyConfig: PolicyConfig,
    isLoading: Boolean,
    initialStrategy: LampStrategyInfo?,
    onBack: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "正在加载产品策略配置，请稍候...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PriorityAndPeriodSection(state, policyConfig)

        val renderKey = when (state.selectedPolicyType?.first?.toInt()) {
            2 -> "timeStrategies"
            1 -> "lngLatStrategies"
            else -> state.selectedPolicyType?.second?.key
        }

        Log.d(TAG, "StepTwo rendering logic -> renderKey determined as: $renderKey")

        when (renderKey) {
            "timeStrategies" -> TimeStrategySection(state, policyConfig)
            "lngLatStrategies" -> LngLatStrategySection(state, policyConfig)
            else -> {
                OutlinedCard(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "未知的策略模式配置。\n当前 Key: $renderKey",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { submitStrategy(state, viewModel, dateFormatter, initialStrategy?.id, onBack) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                if (initialStrategy != null) "保存并同步" else "提交并同步",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GroupSelectionField(state: LampStrategyFormState) {
    @OptIn(ExperimentalLayoutApi::class)
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
private fun PriorityAndPeriodSection(state: LampStrategyFormState, policyConfig: PolicyConfig) {
    val policyPriorityRange = policyConfig.priorityRange
    val policyPeriodTypes = policyConfig.periodTypes

    LaunchedEffect(policyPriorityRange) {
        if (policyPriorityRange != null) {
            if (state.selectedPriority != null && state.selectedPriority!! !in policyPriorityRange.min..policyPriorityRange.max) {
                Log.w(TAG, "⚠️ 优先级 ${state.selectedPriority} 超出限制范围，已重置为空以供重选")
                state.selectedPriority = null
            }
        } else {
            // 产品/策略未配置优先级范围时，强制清空可能回显或残留的优先级值
            state.selectedPriority = null
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 只有获取到有效的 priorityRange 才会显示优先级下拉框
        if (policyPriorityRange != null) {
            val displayPriorities = (policyPriorityRange.min..policyPriorityRange.max).toList()
            Box(modifier = Modifier.weight(1f)) {
                CommonDropdownMenu(
                    items = displayPriorities,
                    selectedItem = state.selectedPriority,
                    itemLabel = { it.toString() },
                    label = "策略优先级",
                    placeholder = "请选择",
                    onItemSelected = { state.selectedPriority = it }
                )
            }
        }

        val displayPeriods = if (policyPeriodTypes.isNotEmpty()) {
            policyPeriodTypes
        } else {
            listOf(
                Pair(1L, KeyValue("1", "每天")),
                Pair(2L, KeyValue("2", "星期")),
                Pair(3L, KeyValue("3", "时间区间"))
            )
        }
        Box(modifier = Modifier.weight(if (policyPriorityRange != null) 2f else 1f)) {
            CommonDropdownMenu(
                items = displayPeriods,
                selectedItem = state.selectedPolicyPeriod?.let { current -> displayPeriods.find { it.first == current.first } },
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val weekDays =
            listOf(1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日")
        weekDays.forEach { (dayValue, dayName) ->
            val isSelected = state.selectedDaysOfWeek.contains(dayValue)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (isSelected) state.selectedDaysOfWeek.remove(dayValue) else state.selectedDaysOfWeek.add(
                            dayValue
                        )
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
            onValueChange = {}, readOnly = true, label = { Text("开始日期") },
            trailingIcon = { Icon(Icons.Default.DateRange, null) },
            modifier = Modifier.weight(1f), enabled = false, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )
        OutlinedTextField(
            value = state.endDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {}, readOnly = true, label = { Text("结束日期") },
            trailingIcon = { Icon(Icons.Default.DateRange, null) },
            modifier = Modifier.weight(1f), enabled = false, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun TimeStrategySection(
    state: LampStrategyFormState,
    policyConfig: PolicyConfig
) {
    Text(
        "任务配置",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    val parsedMaxSize = policyConfig.maxSize
    val maxSize = if (parsedMaxSize > 0) parsedMaxSize else 10

    state.timeTasks.forEachIndexed { index, task ->
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
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
                        "时间节点 ${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.timeTasks.size > 1) {
                        IconButton(
                            onClick = { state.timeTasks.removeAt(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
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
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                val displayActionTypes =
                    if (policyConfig.actionTypes.isNotEmpty()) policyConfig.actionTypes else {
                        task.actionType?.let { listOf(it) } ?: emptyList()
                    }

                CommonDropdownMenu(
                    items = displayActionTypes, selectedItem = task.actionType,
                    itemLabel = { it.second.value }, label = "执行动作", placeholder = "请选择",
                    onItemSelected = { state.timeTasks[index] = task.copy(actionType = it) }
                )

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
    state: LampStrategyFormState, policyConfig: PolicyConfig
) {
    Text(
        "经纬度(日出日落)配置",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "🌅 日出配置",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            LngLatConfigRow(
                offset = state.sunriseOffset,
                onOffsetChange = { state.sunriseOffset = it },
                actionType = state.sunriseActionType,
                onActionTypeChange = { state.sunriseActionType = it },
                actionValue = state.sunriseActionValue,
                onActionValueChange = { state.sunriseActionValue = it },
                policyActionTypes = policyConfig.actionTypes
            )
        }
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "🌇 日落配置",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            LngLatConfigRow(
                offset = state.sunsetOffset,
                onOffsetChange = { state.sunsetOffset = it },
                actionType = state.sunsetActionType,
                onActionTypeChange = { state.sunsetActionType = it },
                actionValue = state.sunsetActionValue,
                onActionValueChange = { state.sunsetActionValue = it },
                policyActionTypes = policyConfig.actionTypes
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
        onValueChange = {
            if (it.isEmpty() || it == "-" || it.toIntOrNull() != null) onOffsetChange(it)
        },
        label = { RequiredLabel("偏移量 (分钟)") },
        placeholder = { Text("正为延后，负为提前") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text("min") }
    )

    val displayActionTypes = if (policyActionTypes.isNotEmpty()) policyActionTypes else {
        actionType?.let { listOf(it) } ?: emptyList()
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            CommonDropdownMenu(
                items = displayActionTypes,
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

@Composable
private fun RequiredLabel(text: String) {
    Text(buildAnnotatedString {
        append(text); withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
        append(
            " *"
        )
    }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogAndBottomSheetHandler(state: LampStrategyFormState, viewModel: LampViewModel) {
    if (state.showGroupBottomSheet) {
        val lampStrategyGroupInfoFlow =
            viewModel.lampStrategyGroupInfoFlow.collectAsLazyPagingItems()
        StrategyGroupBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            lampStrategyGroupInfoFlow = lampStrategyGroupInfoFlow,
            selectedGroups = state.selectedGroups,
            onDismissRequest = { state.showGroupBottomSheet = false }
        )
    }

    if (state.showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.startDateMillis,
            initialSelectedEndDateMillis = state.endDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { state.showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.startDateMillis =
                        dateRangePickerState.selectedStartDateMillis; state.endDateMillis =
                    dateRangePickerState.selectedEndDateMillis; state.showDateRangePicker = false
                }, enabled = dateRangePickerState.selectedEndDateMillis != null) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    state.showDateRangePicker = false
                }) { Text("取消") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f),
                title = { Text("指定策略生效周期", Modifier.padding(start = 24.dp, top = 16.dp)) })
        }
    }

    if (state.editingTimeIndex != null) {
        val timePickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { state.editingTimeIndex = null },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = "${
                        timePickerState.hour.toString().padStart(2, '0')
                    }:${
                        timePickerState.minute.toString().padStart(2, '0')
                    }"; state.timeTasks[state.editingTimeIndex!!] =
                    state.timeTasks[state.editingTimeIndex!!].copy(time = newTime); state.editingTimeIndex =
                    null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    state.editingTimeIndex = null
                }) { Text("取消") }
            },
            text = { TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth()) }
        )
    }
}

//组装策略数据
private fun submitStrategy(
    state: LampStrategyFormState,
    viewModel: LampViewModel,
    dateFormatter: SimpleDateFormat,
    editId: Long?,
    onBack: () -> Unit
) {
    val strategyContent = mutableListOf<JsonObject>()
    val timeTypeStr = state.selectedPolicyPeriod?.first?.toString() ?: "1"
    val timeTypeInt = state.selectedPolicyPeriod?.first?.toInt() ?: 1

    val weekString =
        if (timeTypeInt == 2) state.selectedDaysOfWeek.sorted().joinToString(",") else ""
    val daysData = if (timeTypeInt == 3) DayData(startTime = state.startDateMillis?.let {
        dateFormatter.format(Date(it))
    }, endTime = state.endDateMillis?.let { dateFormatter.format(Date(it)) }) else DayData()

    val submitKey = when (state.selectedPolicyType?.first?.toInt()) {
        2 -> "timeStrategies"
        1 -> "lngLatStrategies"
        else -> state.selectedPolicyType?.second?.key
    }

    when (submitKey) {
        "timeStrategies" -> {
            state.timeTasks.forEach { e ->
                val require = TimeStrategyCondition(
                    timeType = timeTypeStr,
                    priority = state.selectedPriority,
                    timePoint = e.time,
                    week = weekString,
                    days = daysData
                )
                val actionTypeStr =  e.actionType?.first?.toString()
                val action = StrategyAction(
                    actionType = actionTypeStr,
                    actionValue = e.actionValue.toIntOrNull()
                )
                JsonUtils.toGsonJsonObject(
                    TimeStrategyContent(
                        id = e.id.toLong(),
                        action = action,
                        require = require
                    )
                )?.let { strategyContent.add(it) }
            }
        }

        "lngLatStrategies" -> {
            val sunRiseActionStr = state.sunriseActionType?.first?.toString()
            val sunRiseRequire = LngLatStrategyCondition(
                timeType = timeTypeStr,
                priority = state.selectedPriority,
                week = weekString,
                days = daysData,
                riseDown = RiseDown(
                    riseType = "1",
                    sundown = 0,
                    sunrise = state.sunriseOffset.toIntOrNull() ?: 0
                )
            )
            JsonUtils.toGsonJsonObject(
                LngLatStrategyContent(
                    require = sunRiseRequire,
                    action = StrategyAction(
                        actionType = sunRiseActionStr,
                        actionValue = state.sunriseActionValue.toIntOrNull()
                    )
                )
            )?.let { strategyContent.add(it) }
            val sunSetActionStr = state.sunsetActionType?.first?.toString()
            val sunSetRequire = LngLatStrategyCondition(
                timeType = timeTypeStr,
                priority = state.selectedPriority,
                week = weekString,
                days = daysData,
                riseDown = RiseDown(
                    riseType = "2",
                    sundown = state.sunsetOffset.toIntOrNull() ?: 0,
                    sunrise = 0
                )
            )
            JsonUtils.toGsonJsonObject(
                LngLatStrategyContent(
                    require = sunSetRequire,
                    action = StrategyAction(
                        actionType = sunSetActionStr,
                        actionValue = state.sunsetActionValue.toIntOrNull()
                    )
                )
            )?.let { strategyContent.add(it) }
        }
    }
    if (strategyContent.isNotEmpty()) {
        val strategyDTO = StrategyDTO(
            id = editId,
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
        if (editId == null) {
            viewModel.saveStrategyAndSync(strategyDTO)
        } else {
            viewModel.updateStrategyAndSync(strategyDTO)
        }

        onBack()
    }
}