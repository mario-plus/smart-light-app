package com.unilumin.smartapp.ui.screens.application.lamp

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.twotone.GridView
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupTypeOptions
import com.unilumin.smartapp.client.data.CreateGroupDTO
import com.unilumin.smartapp.client.data.DevSimpleInfo
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.client.data.LampGroupProduct
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.screens.dialog.DeviceControlDialog
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.CardBgColor
import com.unilumin.smartapp.ui.theme.DataPanelBgColor
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.DividerGrey
import com.unilumin.smartapp.ui.theme.IconBgColor
import com.unilumin.smartapp.ui.theme.TextMain
import com.unilumin.smartapp.ui.theme.TextSub
import com.unilumin.smartapp.ui.theme.ThemeBlue
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampGroupContent(
    lampViewModel: LampViewModel, toNew: (LampViewModel) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }

    val lampGroupFlow = lampViewModel.lampGroupFlow.collectAsLazyPagingItems()

    val currentGroup = lampViewModel.currentGroupInfo.collectAsState()

    val productList by lampViewModel.groupProductList.collectAsState()

    val gatewayLIst by lampViewModel.gatewayDevSimpleInfo.collectAsState()

    val isLoading by lampViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }

    //当 showAddDialog 变为 true 时立即加载产品数据
    LaunchedEffect(showAddDialog) {
        if (showAddDialog) {
            lampViewModel.getGroupProduct()
        }
    }

    BaseLampListScreen(
        statusOptions = groupTypeOptions,
        viewModel = lampViewModel,
        pagingItems = lampGroupFlow,
        keySelector = { it.id },
        searchTitle = "搜索分组名称或产品名称",
        onAddClick = {
            showAddDialog = true
        },
        middleContent = {}) { item ->
        LampGroupCard(item = item, onClick = { e ->
            lampViewModel.updateCurrentGroupInfo(e)
            showDialog = true
        }, onMemberClick = { e ->
            lampViewModel.updateCurrentGroupInfo(e)
            toNew(lampViewModel)
        })
    }

    // 设备控制弹窗
    if (showDialog) {
        DeviceControlDialog(
            productId = currentGroup.value?.productId?.toString() ?: "",
            deviceName = currentGroup.value?.groupName ?: "未知分组",
            initialBrightness = 0,
            initColorT = 0,
            onDismiss = {
                lampViewModel.updateCurrentGroupInfo(null)
                showDialog = false
            },
            onClick = { a, b ->
                lampViewModel.groupCtl(currentGroup.value?.id ?: 0L, a, b)
                lampViewModel.updateCurrentGroupInfo(null)
                showDialog = false
            })
    }

    if (showAddDialog) {
        AddGroupBottomSheet(
            productList = productList,
            centralDeviceList = gatewayLIst,
            isCentralLoading = isLoading,
            //变更分组产品
            onProductChange = { productId ->
                lampViewModel.getGatewayList(productId)
            },
            //取消
            onDismissRequest = {
                showAddDialog = false
            },
            //提交分组信息
            onConfirm = { groupName, productInfo, simpleDevInfo, remark ->
                lampViewModel.createGroup(
                    CreateGroupDTO(
                        groupName = groupName,
                        productId = productInfo?.id,
                        deviceId = simpleDevInfo?.id,
                        description = remark
                    )
                )
                showAddDialog = false
                lampGroupFlow.refresh()
            })
    }
}

@Composable
fun LampGroupCard(
    item: LampGroupInfo,
    modifier: Modifier = Modifier,
    onClick: ((LampGroupInfo) -> Unit)? = null,
    onMemberClick: ((LampGroupInfo) -> Unit)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier),
        shape = RoundedCornerShape(16.dp), // 增加一点圆角更加现代
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 1. 头部：图标 + 名称/产品 + 状态 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // 图标容器
                    Surface(
                        color = IconBgColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.GridView,
                            contentDescription = "Group Icon",
                            tint = ThemeBlue,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // 分组名称
                        Text(
                            text = item.groupName ?: "未命名分组", style = TextStyle(
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain
                            ), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 产品名称
                        Text(
                            text = item.productName ?: "--",
                            style = TextStyle(fontSize = 12.sp, color = TextSub),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // 同步状态标签
                DeviceStatus(
                    item.syncState, mapOf(
                        1 to Triple(Color(0xFFE3F2FD), BluePrimary, "已同步"),
                        0 to Triple(Color(0xFFF5F5F5), Color(0xFFBDBDBD), "未同步")
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. 核心数据面板 ---
            GroupInfoPanel(item, onClick = {
                onMemberClick(item)
            })

            // --- 3. 底部备注 (如果有) ---
            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = TextSub,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.description ?: "",
                        fontSize = 12.sp,
                        color = TextSub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 分组核心信息面板
 */
@Composable
fun GroupInfoPanel(item: LampGroupInfo, onClick: ((LampGroupInfo) -> Unit)) {
    Surface(
        color = DataPanelBgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val typeName = when (item.groupType) {
                1 -> "单灯控制器"
                25 -> "集中控制器"
                56 -> "回路控制器"
                else -> "未知类型"
            }
            InfoColumnItem(
                label = "分组类型", value = typeName, modifier = Modifier.weight(1.2f)
            )
            VerticalDivider(
                modifier = Modifier.height(24.dp), color = DividerGrey
            )
            if (item.groupType != 1) {
                InfoColumnItem(
                    label = "所属集控",
                    value = item.deviceName ?: "--",
                    modifier = Modifier.weight(1.2f)
                )
                VerticalDivider(
                    modifier = Modifier.height(24.dp), color = DividerGrey
                )
            }

            InfoColumnItem(
                label = "成员数",
                value = "${item.deviceNum ?: 0}",
                isHighlight = true,
                modifier = Modifier.weight(0.8f),
                onClick = { onClick(item) })
        }
    }
}

@Composable
fun InfoColumnItem(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label, fontSize = 11.sp, color = TextSub
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = if (isHighlight) 16.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) ThemeBlue else TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupBottomSheet(
    productList: List<LampGroupProduct>,
    centralDeviceList: List<DevSimpleInfo>,
    isCentralLoading: Boolean = false,
    onProductChange: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: (groupName: String, productInfo: LampGroupProduct?, gateWayInfo: DevSimpleInfo?, remark: String) -> Unit
) {
    //分组信息
    var groupName by remember { mutableStateOf("") }
    //备注信息
    var remark by remember { mutableStateOf("") }
    //分组产品
    var selectedProduct by remember { mutableStateOf<LampGroupProduct?>(null) }
    //集中控制器
    var selectedCentralDevice by remember { mutableStateOf<DevSimpleInfo?>(null) }

    val currentProductId = selectedProduct?.id
    val currentProductTypeId = selectedProduct?.productTypeId
    //是否需要选择集控
    val requiresCentralDevice = currentProductTypeId != null && currentProductTypeId != 1L

    //校验输入信息是否完整
    val isFormValid =
        remember(groupName, selectedProduct, requiresCentralDevice, selectedCentralDevice) {
            val baseValid = groupName.isNotBlank() && selectedProduct != null
            if (requiresCentralDevice) {
                baseValid && selectedCentralDevice != null
            } else {
                baseValid
            }
        }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // 标题区
            Text(
                text = "新增分组",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // 1. 分组名称输入
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("分组名称") },
                placeholder = { Text("请输入分组名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = defaultOutlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 产品型号下拉框 (替换为无搜索版本)
            SimpleDropdown(
                label = "产品型号",
                placeholder = "请选择产品型号",
                items = productList,
                selectedItem = selectedProduct,
                itemLabel = { it.name ?: "" },
                focusManager = focusManager,
                onItemSelected = { product ->
                    if (product == null) {
                        // 触发了清空
                        selectedProduct = null
                        selectedCentralDevice = null
                    } else {
                        if (currentProductId != product.id) {
                            selectedProduct = product
                            selectedCentralDevice = null
                            if (product.productTypeId != 1L) {
                                onProductChange(product.id)
                            }
                        }
                    }
                })
            // 3. 集控设备下拉框 (动画条件渲染)
            AnimatedVisibility(
                visible = requiresCentralDevice,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleDropdown(
                        label = "集控设备",
                        placeholder = if (isCentralLoading) "正在加载..." else "请选择集控设备",
                        items = centralDeviceList,
                        selectedItem = selectedCentralDevice,
                        itemLabel = { it.deviceName ?: "" },
                        isLoading = isCentralLoading,
                        focusManager = focusManager,
                        noItemsText = if (isCentralLoading) "加载中..." else "暂无可用集控设备",
                        onItemSelected = { device ->
                            selectedCentralDevice = device
                        })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 4. 备注输入
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("备注信息 (选填)") },
                placeholder = { Text("添加相关的描述...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 4,
                colors = defaultOutlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 5. 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape
                ) {
                    Text("取消", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        onConfirm(
                            groupName,
                            selectedProduct,
                            if (requiresCentralDevice) selectedCentralDevice else null,
                            remark
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeBlue,
                        disabledContainerColor = ThemeBlue.copy(alpha = 0.5f)
                    )
                ) {
                    Text("确定", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleDropdown(
    label: String,
    placeholder: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    focusManager: FocusManager,
    onItemSelected: (T?) -> Unit,
    isLoading: Boolean = false,
    noItemsText: String = "暂无数据"
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedItem?.let { itemLabel(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {}, // 只读状态下不需要处理输入
            readOnly = true,    // 核心修改：设置为只读，键盘不会弹出
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ThemeBlue
                        )
                    } else if (selectedText.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    onItemSelected(null)
                                    expanded = true
                                },
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = defaultOutlinedTextFieldColors(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
            if (items.isEmpty()) {
                DropdownMenuItem(text = { Text(noItemsText, color = Color.Gray) }, onClick = {
                    expanded = false
                    focusManager.clearFocus()
                })
            } else {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(item)) }, onClick = {
                            onItemSelected(item)
                            expanded = false
                            focusManager.clearFocus()
                        }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

// 统一输入框颜色
@Composable
fun defaultOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ThemeBlue,
    focusedLabelColor = ThemeBlue,
    unfocusedBorderColor = DividerGrey,
)