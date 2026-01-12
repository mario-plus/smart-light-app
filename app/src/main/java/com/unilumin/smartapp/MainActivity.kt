package com.unilumin.smartapp

import SystemConfigScreen
import SystemInfoScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amap.api.maps.MapsInitializer
import com.unilumin.smartapp.auth.TokenManagerFactory
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.mock.ServerConfig
import com.unilumin.smartapp.ui.components.BottomNavBar
import com.unilumin.smartapp.ui.screens.DashboardScreen
import com.unilumin.smartapp.ui.screens.device.DeviceDetailScreen
import com.unilumin.smartapp.ui.screens.device.DeviceStatusChartScreen
import com.unilumin.smartapp.ui.screens.device.DevicesScreen
import com.unilumin.smartapp.ui.screens.login.LoginScreen
import com.unilumin.smartapp.ui.screens.profile.ProfileScreen
import com.unilumin.smartapp.ui.screens.site.SitesScreen
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory


class MainActivity : ComponentActivity() {
    val retrofitClient = RetrofitClient()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. 确认已展示隐私政策 (第一个参数 context, 第二个 isShow: true, 第三个 isContain: true)
        MapsInitializer.updatePrivacyShow(this, true, true)
        // 2. 确认用户已同意隐私政策 (第二个 isAgree: true)
        MapsInitializer.updatePrivacyAgree(this, true)

        retrofitClient.initClient(this)
        retrofitClient.initRetrofit(ServerConfig.getBaseUrl())
        super.onCreate(savedInstanceState)
        setContent {
            SmartStreetLightApp(retrofitClient)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmartStreetLightApp(retrofitClient: RetrofitClient) {
    val context = LocalContext.current
    // 使用 sessionKey 来强制重置整个 App 的状态
    var sessionKey by remember { mutableIntStateOf(0) }
    var isLoggedIn by remember { mutableStateOf(false) }

    // 配置的设备产品list，设备页面需要使用，待优化
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory {
            ProfileViewModel(retrofitClient, context)
        }
    )



    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue600, background = Gray50, surface = Color.White, onSurface = Gray900
        )
    ) {
        // 使用 key 包装，当 sessionKey 变化时，内部所有状态都会被销毁重置
        key(sessionKey) {
            val navController = rememberNavController()
            if (!isLoggedIn) {
                LoginScreen(
                    retrofitClient = retrofitClient, onLogin = {
                        isLoggedIn = true
                        sessionKey++
                    })
            } else {
                Scaffold(
                    bottomBar = { BottomNavBar(navController) }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") { DashboardScreen(retrofitClient) }
                        composable("devices") {
                            DevicesScreen(
                                retrofitClient = retrofitClient,
                                profileViewModel = profileViewModel,
                                onDetailClick = { lightDevice ->
                                    val deviceJson = com.google.gson.Gson().toJson(lightDevice)
                                    val encodedJson =
                                        java.net.URLEncoder.encode(deviceJson, "UTF-8")
                                    navController.navigate("deviceDetail/$encodedJson")
                                },
                                onMenuClick = {
                                    navController.navigate("deviceStatusChart")
                                })
                        }
                        //离线报表
                        composable("deviceStatusChart") { e ->
                            DeviceStatusChartScreen(
                                retrofitClient, onBack = { navController.popBackStack() })
                        }

                        //设备详情页面
                        composable("deviceDetail/{deviceJson}") { backStackEntry ->
                            //如果此处的json过大，可以改成deviceId，deviceName进行传递
                            val encodedJson = backStackEntry.arguments?.getString("deviceJson")
                            val deviceJson = java.net.URLDecoder.decode(encodedJson, "UTF-8")
                            val iotDevice =
                                com.google.gson.Gson().fromJson(deviceJson, IotDevice::class.java)
                            DeviceDetailScreen(
                                iotDevice = iotDevice,
                                retrofitClient = retrofitClient,
                                onBack = { navController.popBackStack() })
                        }
                        composable("sites") { SitesScreen(retrofitClient) }
                        composable("profile") {
                            ProfileScreen(retrofitClient = retrofitClient, onLogout = {
                                TokenManagerFactory.getInstance(context).clear()
                                isLoggedIn = false
                                sessionKey++
                            }, onItemClick = { name, profileViewModel ->
                                if (name == DeviceConstant.SYSTEM_INFO) {
                                    navController.navigate("systemInfo")
                                } else if (name == DeviceConstant.SYSTEM_CONFIG) {
                                    navController.navigate("systemConfig")
                                }
                            })
                        }
                        //系统信息
                        composable("systemInfo") { e ->
                            SystemInfoScreen(
                                retrofitClient = retrofitClient,
                                onBack = { navController.popBackStack() })
                        }
                        //系统配置
                        composable("systemConfig") { e ->
                            SystemConfigScreen(
                                retrofitClient = retrofitClient,
                                onBack = { navController.popBackStack() })
                        }

                    }
                }
            }
        }
    }
}