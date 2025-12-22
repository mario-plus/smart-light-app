package com.unilumin.smartapp.ui.screens.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.LightDevice
import com.unilumin.smartapp.ui.components.LoopCircleItem
import com.unilumin.smartapp.ui.theme.Gray400

/**
 * 回路 (Loop) 特有内容
 */
@Composable
fun LoopFeatureContent(lightDevice: LightDevice) {
    FeatureContentContainer {
        val loops = lightDevice.loops ?: emptyList()
        if (loops.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 8
            ) {
                loops.forEach { loop ->
                    LoopCircleItem(loop)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无回路信息", fontSize = 12.sp, color = Gray400)
            }
        }
    }
}