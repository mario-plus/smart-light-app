package com.unilumin.smartapp.ui.screens.app.playBox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant.DeviceMenus
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_PLAY_BOX
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.PageBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlayBoxScreen(
    retrofitClient: RetrofitClient, onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CommonTopAppBar(
                        title = DeviceMenus.toMap()[SMART_PLAY_BOX].toString(),
                        onBack = { onBack() })
                }
            }
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