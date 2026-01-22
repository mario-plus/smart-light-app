package com.unilumin.smartapp.ui.screens.app.lamp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.ui.components.BaseLampListScreen
import com.unilumin.smartapp.ui.viewModel.LampViewModel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.DpOffset

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.ui.components.SearchStyleMultiSelectBar
import com.unilumin.smartapp.ui.theme.BluePrimary
import com.unilumin.smartapp.ui.theme.ColorDivider
import com.unilumin.smartapp.ui.theme.ColorIcon
import com.unilumin.smartapp.ui.theme.ColorIconLight
import com.unilumin.smartapp.ui.theme.ColorPlaceholder
import com.unilumin.smartapp.ui.theme.ColorTextPrimary
import com.unilumin.smartapp.ui.theme.SearchBarBg


val sceneList = listOf("公园场景", "高速路场景", "住宅区", "商业街")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampJobContent(
    lampViewModel: LampViewModel
) {

    var selectedScenes by remember { mutableStateOf(setOf<String>()) }

    val lampJobFlow = lampViewModel.lampJobFlow.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        lampViewModel.updateSearch("")
        lampViewModel.updateState(-1)
        lampViewModel.getJobScene()
    }



    BaseLampListScreen(
        statusOptions = DeviceConstant.jobStatusOptions,
        viewModel = lampViewModel,
        pagingItems = lampJobFlow,
        keySelector = { it.id },
        searchTitle = "搜索分组名称或产品名称",
        middleContent = {
            SearchStyleMultiSelectBar(
                label = "场景",
                options = sceneList,
                selectedOptions = selectedScenes,
                onSelectionChanged = { newSelection ->
                    selectedScenes = newSelection
                    // 如果需要联动搜索，可以在这里调用 viewModel.updateSceneFilter(newSelection)
                },
                placeholder = "全部场景"
            )
        }
    ) { item ->
        Text("ssss")
    }


}







