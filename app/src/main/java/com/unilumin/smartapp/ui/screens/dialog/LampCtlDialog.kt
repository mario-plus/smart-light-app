//package com.unilumin.smartapp.ui.screens.dialog
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.PowerSettingsNew
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import com.unilumin.smartapp.client.constant.DeviceType
//import com.unilumin.smartapp.ui.components.BrightnessControlCard
//import com.unilumin.smartapp.ui.theme.ControlBlue
//import com.unilumin.smartapp.ui.theme.ControlRed
//
//
//@Composable
//fun DeviceControlDialog(
//    productId: String,
//    deviceName: String,
//    initialBrightness: Int?,
//    initColorT: Int?,
//    onDismiss: () -> Unit,
//    onClick: (Int, Int) -> Unit,
//) {
//    var bright by remember { mutableIntStateOf(initialBrightness ?: 0) }
//    var colorT by remember { mutableIntStateOf(initColorT ?: 0) }
//
//    Dialog(onDismissRequest = onDismiss) {
//        Surface(
//            shape = RoundedCornerShape(24.dp), // 更圆润的弹窗圆角
//            color = Color.White,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp), // 防止贴边
//            tonalElevation = 8.dp
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(24.dp)
//                    .fillMaxWidth(),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    Text(
//                        text = "设备[${deviceName}]控制",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black,
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                    // 右上角关闭小图标 (可选，为了体验更好)
//                    IconButton(
//                        onClick = onDismiss,
//                        modifier = Modifier
//                            .align(Alignment.CenterEnd)
//                            .size(24.dp)
//                    ) {
//                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // --- 2. 开关控制按钮组 (你的代码) ---
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Button(
//                        onClick = {
//                            onClick(1, 1)//开灯
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = ControlBlue.copy(alpha = 0.1f),
//                            contentColor = ControlBlue
//                        ),
//                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.PowerSettingsNew,
//                            contentDescription = null,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("开启", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                    }
//                    Button(
//                        onClick = {
//                            onClick(1, 0)//关灯
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = ControlRed.copy(alpha = 0.1f),
//                            contentColor = ControlRed
//                        ),
//                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.PowerSettingsNew,
//                            contentDescription = null,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("关闭", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                    }
//                }
//                Spacer(modifier = Modifier.height(24.dp))
//                BrightnessControlCard(
//                    "亮度",
//                    initValue = bright,
//                    onValueChange = { newValue -> bright = newValue },
//                    onValueChangeFinished = { value ->
//                        onClick(2, value)
//                    })
//                Spacer(modifier = Modifier.height(24.dp))
//                if (DeviceType.colorTempSupportedList.contains(productId)) {
//                    BrightnessControlCard(
//                        "色温",
//                        initValue = colorT,
//                        onValueChange = { newValue -> colorT = newValue },
//                        onValueChangeFinished = { value ->
//                            onClick(3, value)
//                        })
//                }
//            }
//        }
//    }
//}
//
//
