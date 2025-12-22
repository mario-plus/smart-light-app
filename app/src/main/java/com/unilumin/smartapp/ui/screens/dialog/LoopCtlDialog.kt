package com.unilumin.smartapp.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.unilumin.smartapp.client.data.LoopInfo


@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun LoopControlDialog(
    deviceName: String,
    loopInfos: List<LoopInfo>?,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<Int>) -> Unit // action: 1开/0关
) {
    // 状态管理
    var selectedAction by remember { mutableIntStateOf(1) } // 1: 开灯, 0: 关灯
    val selectedLoops = remember { mutableStateListOf<Int>() }

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
                // 1. 标题：显示回路控制器名称
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp) // 稍微加高，视觉更饱满
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(14.dp)) // 底色稍深
                        .padding(4.dp)
                ) {
                    val actions = listOf("闭合回路" to 1, "断开回路" to 0)
                    actions.forEach { (label, value) ->
                        val isSelected = selectedAction == value

                        // 动态定义颜色
                        val containerColor =
                            if (isSelected) Color(0xFF2E66FF) else Color.Transparent
                        val textColor = if (isSelected) Color.White else Color(0xFF666666)
                        val fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(containerColor, RoundedCornerShape(11.dp)) // 选中块背景
                                .clickable { selectedAction = value },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = fontWeight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. 选择需要操作的回路编号
                Text(
                    text = "选择回路编号",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 回路编号网格排列
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (loopInfos != null) {
                        for (loopInfo in loopInfos) {
                            val isSelected = selectedLoops.contains(loopInfo.loopNum)
                            val bgColor =
                                if (isSelected) Color(0xFF2E66FF).copy(alpha = 0.1f) else Color.White
                            val borderColor =
                                if (isSelected) Color(0xFF2E66FF) else Color(0xFFEEEEEE)
                            val textColor = if (isSelected) Color(0xFF2E66FF) else Color.Black
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(bgColor, CircleShape)
                                    .border(1.dp, borderColor, CircleShape)
                                    .clickable {
                                        if (isSelected) selectedLoops.remove(loopInfo.loopNum) else selectedLoops.add(
                                            loopInfo.loopNum
                                        )
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${loopInfo.loopNum}",
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = { onConfirm(selectedAction, selectedLoops.toList()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E66FF)),
                        enabled = selectedLoops.isNotEmpty()
                    ) {
                        Text("确定执行", color = Color.White)
                    }
                }
            }
        }
    }
}


