package com.unilumin.smartapp.ui.screens.device

import android.annotation.SuppressLint
import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.AddDevice
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.client.data.SimpleProduct
import com.unilumin.smartapp.client.data.SystemConfig
import com.unilumin.smartapp.client.data.UpdateDevice
import com.unilumin.smartapp.ui.components.CommonDropdownMenu
import com.unilumin.smartapp.ui.components.FilterChip
import com.unilumin.smartapp.ui.components.LoadingContent
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.ReferenceStyleDropdownMenu
import com.unilumin.smartapp.ui.components.SearchBar
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.theme.White
import com.unilumin.smartapp.ui.viewModel.DeviceViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DevicesScreen(
    retrofitClient: RetrofitClient,
    onDetailClick: (IotDevice) -> Unit,
    onMenuClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // --- 核心：设备管理 BottomSheet 状态控制 ---
    var showManageDeviceSheet by remember { mutableStateOf(false) }
    var editingDevice by remember { mutableStateOf<IotDevice?>(null) } // null 表示新增，非 null 表示编辑
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val deviceViewModel: DeviceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceViewModel(retrofitClient, application) as T
        }
    })

    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, application) as T
        }
    })

    var pendingScanCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

    // 二维码扫码结果
    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedText = result.content.rawValue ?: ""
                pendingScanCallback?.invoke(scannedText)
            }
            is QRResult.QRUserCanceled -> Toast.makeText(context, "已取消扫码", Toast.LENGTH_SHORT).show()
            is QRResult.QRMissingPermission -> Toast.makeText(context, "需要相机权限才能扫码", Toast.LENGTH_LONG).show()
            is QRResult.QRError -> Toast.makeText(context, "扫码出错: ${result.exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        deviceViewModel.updateFilter("1")
    }

    val smartApps by systemViewModel.smartApps.collectAsState()
    val productTypes by systemViewModel.productTypes.collectAsState()
    val activeTypes = remember(productTypes) { productTypes.filter { it.isSelected } }

    val productType by deviceViewModel.productType.collectAsState()
    val totalCount = deviceViewModel.totalCount.collectAsState()
    val searchQuery by deviceViewModel.searchQuery.collectAsState()
    val deviceState by deviceViewModel.state.collectAsState()
    val lazyPagingItems = deviceViewModel.devicePagingFlow.collectAsLazyPagingItems()
    val simpleProductList by deviceViewModel.simpleProductList.collectAsState()
    val isLoading by deviceViewModel.isLoading.collectAsState()

    var statusExpanded by remember { mutableStateOf(false) }

    // --- 底部弹出层 (新增与编辑共用) ---
    if (showManageDeviceSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showManageDeviceSheet = false
                editingDevice = null
            },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            DeviceManageBottomSheetContent(
                editDevice = editingDevice,
                productList = simpleProductList,
                onDismiss = {
                    showManageDeviceSheet = false
                    editingDevice = null
                },
                onSubmit = { deviceName, productId, serial, remark ->
                    if (editingDevice != null) {
                        val updateDevice = UpdateDevice(
                            deviceName = deviceName,
                            productId = productId ?: 0L,
                            serialNum = serial,
                            description = remark,
                            productTypeId = productType.toLong(),
                            id = editingDevice!!.id
                        )
                        deviceViewModel.updateDevice(updateDevice)
                    } else {
                        val addDevice = AddDevice(
                            deviceName = deviceName,
                            productId = productId ?: 0L,
                            serialNum = serial,
                            description = remark,
                            productTypeId = productType.toLong()
                        )
                        deviceViewModel.createDevice(addDevice)
                    }
                    showManageDeviceSheet = false
                    editingDevice = null
                    lazyPagingItems.refresh()
                },
                onScanClick = { onScanResult ->
                    pendingScanCallback = onScanResult
                    scanQrCodeLauncher.launch(null)
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        Surface(
            color = White, shadowElevation = 2.dp, modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                // 1. 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "设备列表",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.FilterList, null, tint = Gray500)

                        ReferenceStyleDropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            items = smartApps,
                            onItemClick = { systemConfig ->
                                showMenu = false
                                onMenuClick(systemConfig.id)
                            })
                    }
                }

                // 2. 状态筛选 + 搜索框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Gray100)
                                .clickable { statusExpanded = true }
                                .padding(start = 12.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DeviceConstant.statusOptions.find { it.first == deviceState }?.second ?: "状态",
                                fontSize = 14.sp,
                                color = Gray900,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DeviceConstant.statusOptions.forEach { (value, label) ->
                                DropdownMenuItem(text = {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        color = if (value == deviceState) Blue600 else Gray900,
                                        fontWeight = if (value == deviceState) FontWeight.Bold else FontWeight.Normal
                                    )
                                }, onClick = {
                                    deviceViewModel.updateState(value)
                                    statusExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { deviceViewModel.updateSearch(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. 筛选区域
                DeviceFilterSection(
                    activeTypes = activeTypes,
                    selectedId = productType.toLong(),
                    onSelect = { id -> deviceViewModel.updateFilter(id) })
            }
        }
        LoadingContent(isLoading) {
            PagingList(
                totalCount = totalCount.value,
                lazyPagingItems = lazyPagingItems,
                forceLoading = false,
                modifier = Modifier.weight(1f),
                itemKey = { device -> device.id },
                emptyMessage = "未找到相关设备",
                onAddClick = {
                    editingDevice = null // 确保是新增模式
                    deviceViewModel.getSimpleProductList()
                    showManageDeviceSheet = true
                },
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
            ) { device ->
                DeviceCardItem(
                    iotDevice = device,
                    productType = productType.toLong(),
                    onDetailClick = { onDetailClick(device) },
                    onEditClick = {
                        editingDevice = device // 存入当前点击的设备，触发编辑模式
                        deviceViewModel.getSimpleProductList() // 同样获取产品列表用于回显名称（如需要）
                        showManageDeviceSheet = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceFilterSection(
    activeTypes: List<SystemConfig>, selectedId: Long, onSelect: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedId, isExpanded) {
        if (!isExpanded) {
            val index = activeTypes.indexOfFirst { it.id == selectedId.toString() }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        AnimatedContent(targetState = isExpanded, label = "FilterAnimation") { expanded ->
            if (expanded) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("", fontSize = 13.sp, color = Gray500, fontWeight = FontWeight.Medium)
                        IconButton(
                            onClick = { isExpanded = false }, modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Blue600)
                        }
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        maxItemsInEachRow = 4
                    ) {
                        activeTypes.forEach { type ->
                            GridFilterItem(
                                type = type,
                                isSelected = type.id == selectedId.toString(),
                                onClick = {
                                    onSelect(type.id)
                                    isExpanded = false
                                })
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeTypes) { type ->
                            FilterChip(
                                label = type.name,
                                isActive = type.id == selectedId.toString(),
                                onClick = { onSelect(type.id) })
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clickable { isExpanded = true }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "展开",
                            tint = Gray500,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridFilterItem(type: SystemConfig, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isSelected) Blue600 else Gray100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) Color.White else Gray900.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = type.name,
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = if (isSelected) Blue600 else Gray900,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManageBottomSheetContent(
    editDevice: IotDevice? = null,
    productList: List<SimpleProduct>,
    onDismiss: () -> Unit,
    onSubmit: (name: String, productId: Long?, serial: String, remark: String) -> Unit,
    onScanClick: ((String) -> Unit) -> Unit
) {
    val isEditMode = editDevice != null

    var deviceName by remember(editDevice) { mutableStateOf(editDevice?.deviceName ?: "") }
    var serialNumber by remember(editDevice) { mutableStateOf(editDevice?.serialNum ?: "") }
    var remark by remember(editDevice) { mutableStateOf(editDevice?.description ?: "") }
    var selectedProduct by remember(editDevice) {
        mutableStateOf(productList.find { it.id == editDevice?.productId })
    }

    // 表单校验：新增需全填，编辑仅需名称非空 (备注本身是可选的，所以校验逻辑无需修改)
    val isFormValid = if (isEditMode) {
        deviceName.isNotBlank()
    } else {
        deviceName.isNotBlank() && serialNumber.isNotBlank() && selectedProduct != null
    }

    // 禁用状态下的统一样式
    val disabledColors = OutlinedTextFieldDefaults.colors(
        disabledContainerColor = Color(0xFFF9FAFB), // 非常淡的灰色，类似 Gray50
        disabledBorderColor = Color.Transparent,
        disabledTextColor = Gray500,
        disabledLabelColor = Gray500
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp, top = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "编辑设备" else "添加新设备",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "关闭", tint = Gray500)
            }
        }

        // 1. 设备名称 (均可编辑)
        OutlinedTextField(
            value = deviceName,
            onValueChange = { deviceName = it },
            label = { Text("设备名称") },
            placeholder = { Text("请输入设备名称", color = Gray400) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue600,
                unfocusedBorderColor = Gray100,
                focusedLabelColor = Blue600
            )
        )

        // 2. 产品型号
        if (isEditMode) {
            OutlinedTextField(
                value = editDevice?.productName ?: "--",
                onValueChange = {},
                label = { Text("产品型号") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = disabledColors
            )
        } else {
            CommonDropdownMenu(
                items = productList,
                selectedItem = selectedProduct,
                itemLabel = { product -> product.name },
                onItemSelected = { product -> selectedProduct = product },
                modifier = Modifier.fillMaxWidth(),
                label = "产品型号",
                placeholder = "请选择产品"
            )
        }

        // 3. 设备序列号 (编辑时不可修改)
        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            label = { Text("设备序列码 (SN)") },
            placeholder = { Text("手动输入或扫码读取", color = Gray400) },
            enabled = !isEditMode,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                if (!isEditMode) {
                    IconButton(onClick = { onScanClick { scannedCode -> serialNumber = scannedCode } }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "扫码", tint = Blue600)
                    }
                }
            },
            colors = if (isEditMode) disabledColors else OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue600,
                unfocusedBorderColor = Gray100,
            )
        )

        // 4. 备注信息 (修改点：放开限制，无论新增还是编辑均可修改)
        OutlinedTextField(
            value = remark,
            onValueChange = { remark = it },
            label = { Text("备注信息 (可选)") },
            placeholder = { Text("请输入设备部署位置或其他备注", color = Gray400) },
            // 移除了 enabled = !isEditMode，默认为 true
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 5,
            // 移除了 if (isEditMode) 判断，始终使用可编辑状态的颜色
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue600,
                unfocusedBorderColor = Gray100,
                focusedLabelColor = Blue600 // 保持与设备名称一致的选中高亮色
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 提交按钮
        Button(
            onClick = {
                if (isFormValid) {
                    onSubmit(
                        deviceName,
                        selectedProduct?.id ?: editDevice?.productId,
                        serialNumber,
                        remark
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue600,
                disabledContainerColor = Gray100,
                disabledContentColor = Gray400
            ),
            enabled = isFormValid
        ) {
            Text(
                text = if (isEditMode) "保存修改" else "确认添加",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}