package com.unilumin.smartapp.ui.screens.app.broadcast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_BROAD
import com.unilumin.smartapp.client.constant.DeviceConstant.getSmartAppName
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.theme.PageBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBroadScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = getSmartAppName(SMART_BROAD), onBack = { onBack() })
        }, containerColor = PageBackground
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {}
    }
}