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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.LoopInfo

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoopControlBottomSheet( // 建议将名字由 Dialog 改为 BottomSheet 以符合其实际形态
    deviceName: String,
    loopInfos: List<LoopInfo>?,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<Int>) -> Unit // action: 1开/0关
) {
    // 使用 Material 3 的 BottomSheet 状态
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 状态管理
    var selectedAction by remember { mutableIntStateOf(1) } // 1: 开灯, 0: 关灯
    val selectedLoops = remember { mutableStateListOf<Int>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // 预留底部导航栏高度
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. 顶部标题 ---
            Text(
                text = deviceName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1D1D),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "回路控制",
                fontSize = 13.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. 闭合/断开 切换器 (Segmented Control) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFFF5F7FA), RoundedCornerShape(14.dp)) // 稍微柔和一点的灰色底色
                    .padding(4.dp)
            ) {
                val actions = listOf("闭合回路" to 1, "断开回路" to 0)
                actions.forEach { (label, value) ->
                    val isSelected = selectedAction == value

                    val containerColor = if (isSelected) Color(0xFF2F78FF) else Color.Transparent
                    val textColor = if (isSelected) Color.White else Color(0xFF666666)
                    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(containerColor)
                            .clickable { selectedAction = value },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = fontWeight
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (loopInfos != null) {
                    for (loopInfo in loopInfos) {
                        val isSelected = selectedLoops.contains(loopInfo.loopNum)
                        val bgColor = if (isSelected) Color(0xFF2F78FF).copy(alpha = 0.1f) else Color.White
                        val borderColor = if (isSelected) Color(0xFF2F78FF) else Color(0xFFE5E5E5)
                        val textColor = if (isSelected) Color(0xFF2F78FF) else Color(0xFF333333)

                        Box(
                            modifier = Modifier
                                .size(48.dp) // 稍微加大触控区域，防误触
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.5.dp, borderColor, CircleShape)
                                .clickable {
                                    if (isSelected) selectedLoops.remove(loopInfo.loopNum)
                                    else selectedLoops.add(loopInfo.loopNum)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${loopInfo.loopNum}",
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- 4. 底部按钮 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    // 给边框稍微加深一点点颜色
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCDC))
                ) {
                    Text("取消", color = Color(0xFF666666), fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        onConfirm(selectedAction, selectedLoops.toList())
                        onDismiss() // 确认后自动收起弹窗（可选）
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F78FF),
                        disabledContainerColor = Color(0xFF2F78FF).copy(alpha = 0.3f)
                    ),
                    enabled = selectedLoops.isNotEmpty()
                ) {
                    Text("确定执行", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}