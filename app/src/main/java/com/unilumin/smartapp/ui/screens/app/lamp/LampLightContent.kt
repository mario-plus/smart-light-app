package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.constant.DeviceConstant.lampModelOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.statusOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.syncStrategyOptions
import com.unilumin.smartapp.client.data.LampLightInfo
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.components.DeviceStatus
import com.unilumin.smartapp.ui.components.DeviceStatusRow
import com.unilumin.smartapp.ui.components.ModernStateSelector
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.components.SearchHeader

import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.DividerColor
import com.unilumin.smartapp.ui.theme.PageBgColor
import com.unilumin.smartapp.ui.theme.PlaceholderColor
import com.unilumin.smartapp.ui.theme.SearchBarBg

import com.unilumin.smartapp.ui.viewModel.LampViewModel
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampLightContent(
    lampViewModel: LampViewModel
) {


    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }
    // 分页数据
    val lampLightFlow = lampViewModel.lampLightFlow.collectAsLazyPagingItems()


    val model = lampViewModel.lampModel.collectAsState()
    BaseLampListScreen(
        viewModel = lampViewModel,
        pagingItems = lampLightFlow,
        keySelector = { it.id },
        searchTitle = "搜索设备名称或序列码",
        middleContent = {
            ModernStateSelector(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                options = lampModelOptions,
                selectedValue = model.value,
                onValueChange = { newValue ->
                    lampViewModel.updateLampModel(newValue)
                })
        }) { item ->
        LampLightCard(item = item, onDetailClick = {})
    }

}


@Composable
fun LampLightCard(
    item: LampLightInfo, onDetailClick: (LampLightInfo) -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp) // 减小外部缩进，增加屏幕利用率
            .clickable { onDetailClick(item) }, // 增加点击反馈
        shape = RoundedCornerShape(16.dp), // 更圆润的角，符合现代审美
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 略微增加阴影增强层次
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- 第一部分：头部基本信息 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // 图标容器
                    Surface(
                        color = Color(0xFFEBF2FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF2F78FF),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.name ?: "未知设备", style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF333333)
                            ), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SN: ${item.serialNum ?: "--"}",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.productName ?: "--",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF999999))
                        )
                    }
                }

                // 在线/离线 状态标签
                DeviceStatus(item.state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 第二部分：实时参数面板 (参考图片中的灰色块) ---
            LampRealTimeDataPanel(item)

            Spacer(modifier = Modifier.height(16.dp))


            DeviceStatusRow(
                isDisable = item.deviceState == 0,
                hasAlarm = item.alarmType == 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LampRealTimeDataPanel(item: LampLightInfo) {
    Surface(
        color = Color(0xFFF7F8FA), // 浅灰色背景分区
        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            val dataItems = mutableListOf(
                "开关" to if (item.onOff == 1) "开" else "关",
                "亮度" to "${item.bright1?.toInt() ?: "--"}%"
            )
            if (DeviceConstant.colorTempSupportedList.contains(item.productId.toString())) {
                dataItems.add("色温" to "${item.bright2?.toInt() ?: "--"}%")
            }
            dataItems.add("电压" to (item.voltage?.let { "${it}V" } ?: "--"))
            dataItems.add("电流" to (item.current?.let { "${it}mA" } ?: "--"))
            dataItems.add("功率" to (item.power?.let { "${it}W" } ?: "--"))


            dataItems.forEachIndexed { index, pair ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = pair.first, fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pair.second,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pair.first == "开关" || pair.first == "亮度") Color(0xFF2F78FF) else Color(
                            0xFF333333
                        )
                    )
                }
                // 只有不是最后一项时才显示分割线
                if (index < dataItems.size - 1) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}


