package com.unilumin.smartapp.ui.screens.application.lamp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.StrategyGroupListVO
import com.unilumin.smartapp.client.data.StrategyProductVO
import com.unilumin.smartapp.ui.components.CommonDropdownMenu
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.components.StepProgressIndicator
import com.unilumin.smartapp.ui.screens.dialog.StrategyGroupBottomSheet
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.viewModel.LampViewModel

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

    // 策略可选择的分组列表
    val lampStrategyGroupInfoFlow =
        lampViewModel.lampStrategyGroupInfoFlow.collectAsLazyPagingItems()
    // 策略可选择的分组产品列表
    val strategyGroupProductList by lampViewModel.strategyGroupProductList.collectAsState()

    // 策略类型列表 (Pair<Long, String>)
    val strategyTypeList by lampViewModel.strategyTypeList.collectAsState()
    // 策略模式列表 (Pair<Long, String>)
    val policyTypeList by lampViewModel.policyTypeList.collectAsState()

    // 步骤控制状态 (1: 基本信息, 2: 策略详情)
    var currentStep by remember { mutableIntStateOf(1) }

    // --- Step 1: 基本信息表单状态 ---
    var strategyName by remember { mutableStateOf("") }
    var remarkInfo by remember { mutableStateOf("") }
    // 已选择的分组产品
    var selectedProduct by remember { mutableStateOf<StrategyProductVO?>(null) }

    //策略类型
    var selectedStrategyType by remember { mutableStateOf<Pair<Long, String>?>(null) }
    //策略模式
    var selectedPolicyType by remember { mutableStateOf<Pair<Long, String>?>(null) }

    var showGroupBottomSheet by remember { mutableStateOf(false) }
    // 已选择的分组
    val selectedGroups = remember { mutableStateListOf<StrategyGroupListVO>() }


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 拦截系统返回键逻辑：如果在步骤2，则返回步骤1；否则退出页面
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
            // 顶部步骤进度条
            StepProgressIndicator(
                steps = listOf("基本信息", "策略详情"),
                currentStep = currentStep - 1
            )
            // 核心区域：带动画的步骤切换
            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(300)) { it } + fadeIn() togetherWith slideOutHorizontally(
                            tween(300)
                        ) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(tween(300)) { -it } + fadeIn() togetherWith slideOutHorizontally(
                            tween(300)
                        ) { it } + fadeOut()
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
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                    //TODO 更换了产品，需要重置数据
                                    selectedProduct = product
                                    selectedGroups.clear()
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
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowRight,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showGroupBottomSheet = true },
                                    enabled = false, // 禁用内部交互，将点击事件交给外层的 clickable
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
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            if (strategyTypeList.isNotEmpty()) {
                                CommonDropdownMenu(
                                    items = strategyTypeList,
                                    selectedItem = selectedStrategyType,
                                    // Pair 的 second 通常是名称/描述，用于展示
                                    itemLabel = { it.second },
                                    label = "策略类型",
                                    placeholder = "请选择策略类型",
                                    onItemSelected = { selectedStrategyType = it }
                                )
                            }
                            if (policyTypeList.isNotEmpty()) {
                                CommonDropdownMenu(
                                    items = policyTypeList,
                                    selectedItem = selectedPolicyType,
                                    itemLabel = { it.second },
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
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // 下一步按钮
                            Button(
                                onClick = {
                                    currentStep = 2
                                    // 可以在这里通过 selectedStrategyType?.first 拿到选中的 ID 传给下一步
                                },
                                // 基础表单验证，根据业务需求，如果模式和类型是必选项，可以在此加上 && selectedStrategyType != null && selectedPolicyType != null
                                enabled = strategyName.isNotBlank() && selectedProduct != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text(
                                    "下一步：配置策略详情",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 第二步内容...
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
                onDismissRequest = {
                    showGroupBottomSheet = false
                }
            )
        }
    }
}
