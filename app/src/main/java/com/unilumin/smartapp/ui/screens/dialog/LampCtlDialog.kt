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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.ui.components.BrightnessControlCard
import com.unilumin.smartapp.ui.theme.ControlBlue
import com.unilumin.smartapp.ui.theme.ControlRed



@Composable
fun DeviceControlDialog(
    productId: String,
    deviceName: String,
    initialBrightness: Int?,
    initColorT: Int?,
    onDismiss: () -> Unit,
    onClick: (Int, Int) -> Unit,
) {
    // 本地状态：亮度 & 色温
    var bright by remember { mutableIntStateOf(initialBrightness ?: 0) }
    var colorT by remember { mutableIntStateOf(initColorT ?: 0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- 顶部标题栏 ---
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "单灯[${deviceName}]控制", // 简化标题，更干净
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 开关控制按钮组 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 开启按钮
                    PowerButton(
                        text = "开启",
                        color = ControlBlue,
                        onClick = { onClick(1, 1) }
                    )
                    // 关闭按钮
                    PowerButton(
                        text = "关闭",
                        color = ControlRed,
                        onClick = { onClick(1, 0) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 亮度控制 (带输入框) ---
                InteractiveControlCard(
                    title = "亮度",
                    value = bright,
                    unit = "%",
                    accentColor = Color(0xFFFFB300), // 金黄色代表亮度
                    onValueChange = { bright = it },
                    onCommit = { finalValue ->
                        bright = finalValue
                        onClick(2, finalValue)
                    }
                )

                // --- 色温控制 (带输入框) ---
                if (DeviceConstant.colorTempSupportedList.contains(productId)) {
                    Spacer(modifier = Modifier.height(20.dp))
                    InteractiveControlCard(
                        title = "色温",
                        value = colorT,
                        unit = "%",
                        accentColor = Color(0xFF42A5F5), // 蓝色代表冷暖色温
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
}

// 抽取简单的开关按钮，减少重复代码
@Composable
fun RowScope.PowerButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.08f),
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}


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

        // --- 修复后的 Slider ---
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
                .padding(horizontal = 6.dp),

            // 1. 自定义滑块头 (保持原样，很漂亮)
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(4.dp, CircleShape, spotColor = accentColor)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, accentColor.copy(alpha = 0.2f), CircleShape)
                )
            },

            // 2. 【关键修复】自定义粗轨道
            // 这里不再使用 SliderDefaults.Track，因为参数类型不匹配且难以变粗
            track = { sliderState ->
                // 获取当前进度的比例 (0.0 - 1.0)
                // 注意：Material3 1.2+ 中，track lambda 提供的是 sliderState
                val fraction = (sliderState.value - sliderState.valueRange.start) /
                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                // 使用 Box 手动绘制轨道，高度设为 12dp，实现“胶囊”效果
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp) // 设定轨道厚度
                        .clip(RoundedCornerShape(6.dp)) // 设为半圆角
                        .background(accentColor.copy(alpha = 0.15f)) // 底部灰色背景
                ) {
                    // 绘制顶层的进度条
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction) // 宽度根据进度比例填充
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor) // 激活颜色
                    )
                }
            }
        )

        // --- 输入框 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(64.dp)
                .height(30.dp)
                .background(Color(0xFFF5F7FA), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val finalVal = textValue.toIntOrNull()?.coerceIn(0, 100) ?: 0
                    textValue = finalVal.toString()
                    onCommit(finalVal)
                    focusManager.clearFocus()
                }),
                textStyle = TextStyle(
                    color = Color(0xFF333333),
                    fontSize = 14.sp,
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
                Text(text = unit, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(start = 1.dp))
            }
        }
    }
}