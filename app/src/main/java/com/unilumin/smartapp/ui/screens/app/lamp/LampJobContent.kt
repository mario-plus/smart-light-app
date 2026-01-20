package com.unilumin.smartapp.ui.screens.app.lamp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.twotone.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant.groupTypeOptions
import com.unilumin.smartapp.client.constant.DeviceConstant.jobStatusOptions
import com.unilumin.smartapp.client.data.LampGroupInfo
import com.unilumin.smartapp.ui.components.PagingList
import com.unilumin.smartapp.ui.theme.*
import com.unilumin.smartapp.ui.viewModel.LampViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {
    val executeStatus by lampViewModel.state.collectAsState()
    val searchQuery by lampViewModel.searchQuery.collectAsState()
    var statusExpanded by remember { mutableStateOf(false) }
    val totalCount = lampViewModel.totalCount.collectAsState()
    val isSwitching = lampViewModel.isSwitch.collectAsState()
    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBgColor)
    ) {
        // --- 顶部搜索区域 ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = SearchBarBg,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 状态筛选
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .clickable { statusExpanded = true }
                            .padding(start = 16.dp, end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = groupTypeOptions.find { it.first == executeStatus }?.second.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            jobStatusOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = if (value == executeStatus) BluePrimary else Color(
                                                0xFF333333
                                            ),
                                            fontWeight = if (value == executeStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        lampViewModel.updateState(value); statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = DividerColor
                    )
                    // 搜索框
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            "Search",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text("搜索业务对象名称", color = PlaceholderColor, fontSize = 14.sp)
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { lampViewModel.updateSearch(it) },
                                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                                singleLine = true,
                                cursorBrush = SolidColor(BluePrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // --- 列表区域 ---
        PagingList(
            totalCount = totalCount.value,
            lazyPagingItems = lampJobFlow,
            forceLoading = isSwitching.value,
            modifier = Modifier.weight(1f),
            itemKey = { lampJobInfo -> lampJobInfo.id },
            emptyMessage = "未找到任务信息",
            contentPadding = PaddingValues(top = 0.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) { lampJobInfo ->

        }
    }
}
