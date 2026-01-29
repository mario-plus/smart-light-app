package com.unilumin.smartapp.ui.screens.app.lamp


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.data.LampLoopCtlInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.LoopCircleItem
import com.unilumin.smartapp.ui.screens.dialog.LoopControlDialog
import com.unilumin.smartapp.ui.theme.CardBgColor
import com.unilumin.smartapp.ui.theme.IconBgColor
import com.unilumin.smartapp.ui.theme.TextMain
import com.unilumin.smartapp.ui.theme.TextSub
import com.unilumin.smartapp.ui.theme.ThemeBlue
import com.unilumin.smartapp.ui.viewModel.LampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampLoopCtlContent(
    lampViewModel: LampViewModel
) {


    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }

    var selectedLoopCtl by remember { mutableStateOf<LampLoopCtlInfo?>(null) }

    // 分页数据
    val loopCtlFlow = lampViewModel.lampLoopCtlFlow.collectAsLazyPagingItems()
    BaseLampListScreen(
        viewModel = lampViewModel,
        pagingItems = loopCtlFlow,
        keySelector = { it.id },
        searchTitle = "搜索设备名称或序列码"
    ) { item ->
        LampLoopCtlCard(loopCtlInfo = item, onDetailClick = { e ->
            selectedLoopCtl = e
        })
    }

    selectedLoopCtl?.let { loopCtl ->
        LoopControlDialog(
            deviceName = loopCtl.loopControllerName.toString(),
            loopInfos = loopCtl.loops,
            onDismiss = { selectedLoopCtl = null },
            onConfirm = { action, loops ->
                lampViewModel.loopCtl(loopCtl.id, loops, action)
                selectedLoopCtl = null
            })
    }

}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LampLoopCtlCard(
    loopCtlInfo: LampLoopCtlInfo,
    modifier: Modifier = Modifier,
    onDetailClick: ((LampLoopCtlInfo) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .then(if (onDetailClick != null) Modifier.clickable { onDetailClick(loopCtlInfo) } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 1. 头部信息区 (保持不变) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // 图标容器
                    Surface(
                        color = IconBgColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "Loop Controller",
                            tint = ThemeBlue,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // 名称
                        Text(
                            text = loopCtlInfo.loopControllerName ?: "未知控制器",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextMain
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 序列号
                        Text(
                            text = "SN: ${loopCtlInfo.serialNum ?: "--"}",
                            style = TextStyle(fontSize = 13.sp, color = TextSub),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 所属网关
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = null,
                                tint = TextSub,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = loopCtlInfo.gatewayName ?: "未绑定网关",
                                style = TextStyle(fontSize = 13.sp, color = TextSub),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                DeviceStatus(loopCtlInfo.networkState)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. 回路状态区 (修改为自动换行) ---
            if (!loopCtlInfo.loops.isNullOrEmpty()) {
                // 使用 FlowRow 替代 Row + horizontalScroll
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // 水平间距
                    verticalArrangement = Arrangement.spacedBy(12.dp)    // 垂直间距 (换行后的行间距)
                ) {
                    loopCtlInfo.loops!!.forEach { loop ->
                        LoopCircleItem(loop = loop)
                    }
                }
            }
        }
    }
}











