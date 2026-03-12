package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.ControlRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlBottomSheet(
    title: String? = "单灯控制",
    productId: String,
    deviceName: String,
    initialBrightness: Int?,
    initColorT: Int?,
    onDismiss: () -> Unit,
    onClick: (Int, Int) -> Unit,
) {
    // 使用 Material 3 的 BottomSheet 状态，允许跳过半展开状态直接全展开
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 本地状态：亮度 & 色温
    var bright by remember { mutableIntStateOf(initialBrightness ?: 0) }
    var colorT by remember { mutableIntStateOf(initColorT ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        // 删除了容易导致版本冲突的 dragHandle 和 windowInsets，系统会自动使用默认的完美样式
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // 预留底部导航栏的高度，防止全面屏手势条遮挡内容
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 顶部标题栏 ---
            Text(
                text = deviceName, // 突出显示设备名称
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1D1D),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title.toString(),
                fontSize = 13.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 开关控制按钮组 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PowerButton(text = "开启", color = ControlBlue, onClick = { onClick(1, 1) })
                PowerButton(text = "关闭", color = ControlRed, onClick = { onClick(1, 0) })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 亮度控制 ---
            InteractiveControlCard(
                title = "亮度",
                value = bright,
                unit = "%",
                accentColor = Color(0xFFFFB300), // 金黄色
                onValueChange = { bright = it },
                onCommit = { finalValue ->
                    bright = finalValue
                    onClick(2, finalValue)
                }
            )

            // --- 色温控制 ---
            if (DeviceConstant.colorTempSupportedList.contains(productId)) {
                Spacer(modifier = Modifier.height(24.dp))
                InteractiveControlCard(
                    title = "色温",
                    value = colorT,
                    unit = "%",
                    accentColor = Color(0xFF42A5F5), // 蓝色
                    onValueChange = { colorT = it },
                    onCommit = { finalValue ->
                        colorT = finalValue
                        onClick(3, finalValue)
                    }
                )
            }
        }
    }
}

// 按钮组件
@Composable
fun RowScope.PowerButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.08f),
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Icon(
            Icons.Default.PowerSettingsNew,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

// 交互式控制卡片（带滑动条和输入框）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveControlCard(
    title: String,
    value: Int,
    unit: String = "",
    accentColor: Color = Color(0xFF2F78FF),
    onValueChange: (Int) -> Unit,
    onCommit: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var textValue by remember(value) { mutableStateOf(value.toString()) }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 标题 ---
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.widthIn(min = 40.dp)
        )

        // --- Slider ---
        Slider(
            value = value.toFloat(),
            onValueChange = { floatVal ->
                val intVal = floatVal.toInt()
                textValue = intVal.toString()
                onValueChange(intVal)
            },
            onValueChangeFinished = { onCommit(value) },
            valueRange = 0f..100f,
            interactionSource = interactionSource,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .shadow(4.dp, CircleShape, spotColor = accentColor)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, accentColor.copy(alpha = 0.2f), CircleShape)
                )
            },
            track = { sliderState ->
                val fraction = (sliderState.value - sliderState.valueRange.start) /
                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(7.dp))
                            .background(accentColor)
                    )
                }
            }
        )

        // --- 输入框 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(68.dp)
                .height(34.dp)
                .background(Color(0xFFF5F7FA), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE8ECEF), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp)
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = { newText ->
                    if (newText.all { it.isDigit() }) {
                        textValue = newText
                        val intVal = newText.toIntOrNull()
                        if (intVal != null && intVal in 0..100) {
                            onValueChange(intVal)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    val finalVal = textValue.toIntOrNull()?.coerceIn(0, 100) ?: 0
                    textValue = finalVal.toString()
                    onCommit(finalVal)
                    focusManager.clearFocus()
                }),
                textStyle = TextStyle(
                    color = Color(0xFF333333),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val finalVal = textValue.toIntOrNull()?.coerceIn(0, 100) ?: 0
                            if (finalVal != value) onCommit(finalVal)
                            textValue = finalVal.toString()
                        }
                    }
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 1.dp)
                )
            }
        }
    }
}