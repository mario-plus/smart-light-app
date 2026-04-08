package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.ui.components.InteractiveControlCard
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

