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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import com.amap.api.maps.MapsInitializer
import com.google.gson.Gson
import com.unilumin.smartapp.auth.TokenManagerFactory
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.constant.DeviceConstant
import com.unilumin.smartapp.client.constant.DeviceConstant.OFFLINE_ANALYSIS
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_BROAD
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_ENV
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_LAMP
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_MONITOR
import com.unilumin.smartapp.client.constant.DeviceConstant.SMART_PLAY_BOX
import com.unilumin.smartapp.client.data.IotDevice
import com.unilumin.smartapp.mock.ServerConfig
import com.unilumin.smartapp.ui.components.BottomNavBar
import com.unilumin.smartapp.ui.screens.app.broadcast.SmartBroadScreen
import com.unilumin.smartapp.ui.screens.app.env.SmartEnvScreen
import com.unilumin.smartapp.ui.screens.app.lamp.LampGroupMemberContent
import com.unilumin.smartapp.ui.screens.app.lamp.SmartLampScreen
import com.unilumin.smartapp.ui.screens.app.monitor.SmartMonitorScreen
import com.unilumin.smartapp.ui.screens.app.playBox.SmartPlayBoxScreen
import com.unilumin.smartapp.ui.screens.dashboard.DashboardScreen
import com.unilumin.smartapp.ui.screens.dashboard.DeviceAlarmScreen
import com.unilumin.smartapp.ui.screens.device.DeviceDetailScreen
import com.unilumin.smartapp.ui.screens.device.DeviceStatusChartScreen
import com.unilumin.smartapp.ui.screens.device.DevicesScreen
import com.unilumin.smartapp.ui.screens.login.LoginScreen
import com.unilumin.smartapp.ui.screens.profile.ProfileScreen
import com.unilumin.smartapp.ui.screens.site.SitesScreen
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray50
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.ui.viewModel.LampViewModel
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel
import java.net.URLDecoder
import java.net.URLEncoder


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
    var sessionKey by rememberSaveable { mutableIntStateOf(0) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var imageLoader: ImageLoader = retrofitClient.getImageLoader(context)
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue600, background = Gray50, surface = Color.White, onSurface = Gray900
        )
    ) {

        val profileViewModel: ProfileViewModel
        // 使用 key 包装，当 sessionKey 变化时，内部所有状态都会被销毁重置
        key(sessionKey) {

            var cachedProfileViewModel by remember { mutableStateOf<ProfileViewModel?>(null) }

            var cachedLampViewModel by remember { mutableStateOf<LampViewModel?>(null) }

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
                        //概览
                        composable("dashboard") {
                            DashboardScreen(retrofitClient, onNotificationClick = {
                                navController.navigate("deviceAlarmScreen")
                            })
                        }
                        composable("deviceAlarmScreen") { e ->
                            DeviceAlarmScreen(
                                retrofitClient, onBack = { navController.popBackStack() })
                        }
                        //设备
                        composable("devices") {
                            DevicesScreen(
                                retrofitClient = retrofitClient,
                                onDetailClick = { lightDevice ->
                                    val deviceJson = Gson().toJson(lightDevice)
                                    val encodedJson =
                                        URLEncoder.encode(deviceJson, "UTF-8")
                                    navController.navigate("deviceDetail/$encodedJson")
                                },
                                onMenuClick = { e ->
                                    when (e) {
                                        OFFLINE_ANALYSIS -> navController.navigate("deviceStatusChart")
                                        SMART_LAMP -> navController.navigate("smartLampScreen")
                                        SMART_MONITOR -> navController.navigate("smartMonitorScreen")
                                        SMART_ENV -> navController.navigate("smartEnvScreen")
                                        SMART_BROAD -> navController.navigate("smartBroadScreen")
                                        SMART_PLAY_BOX -> navController.navigate("smartPlayBoxScreen")
                                    }
                                })
                        }
                        //离线报表
                        composable("deviceStatusChart") { e ->
                            DeviceStatusChartScreen(
                                retrofitClient, onBack = { navController.popBackStack() })
                        }

                        //智慧路灯
                        composable("smartLampScreen") { e ->
                            SmartLampScreen(
                                retrofitClient,
                                onBack = { navController.popBackStack() },
                                toNew = { e ->
                                    cachedLampViewModel = e
                                    navController.navigate("groupMemberScreen")
                                })
                        }
                        //分组成员页面
                        composable("groupMemberScreen") { e ->
                            cachedLampViewModel?.let {
                                LampGroupMemberContent(cachedLampViewModel!!, onBack = {
                                    navController.popBackStack()
                                })
                            }
                            //智慧广播
                            composable("smartBroadScreen") { e ->
                                SmartBroadScreen(
                                    retrofitClient, onBack = { navController.popBackStack() })
                            }
                            //智能感知
                            composable("smartEnvScreen") { e ->
                                SmartEnvScreen(
                                    retrofitClient, onBack = { navController.popBackStack() })
                            }

                            //安防监控
                            composable("smartMonitorScreen") { e ->
                                SmartMonitorScreen(
                                    retrofitClient, onBack = { navController.popBackStack() })
                            }

                            //智慧屏幕
                            composable("smartPlayBoxScreen") { e ->
                                SmartPlayBoxScreen(
                                    retrofitClient, onBack = { navController.popBackStack() })
                            }


                            //设备详情页面
                            composable("deviceDetail/{deviceJson}") { backStackEntry ->
                                //如果此处的json过大，可以改成deviceId，deviceName进行传递
                                val encodedJson = backStackEntry.arguments?.getString("deviceJson")
                                val deviceJson = URLDecoder.decode(encodedJson, "UTF-8")
                                val iotDevice =
                                    Gson().fromJson(deviceJson, IotDevice::class.java)
                                DeviceDetailScreen(
                                    iotDevice = iotDevice,
                                    retrofitClient = retrofitClient,
                                    onBack = { navController.popBackStack() })
                            }
                            //站点
                            composable("sites") { SitesScreen(retrofitClient) }

                            //我的
                            composable("profile") {
                                ProfileScreen(
                                    imageLoader = imageLoader,
                                    retrofitClient = retrofitClient, onLogout = {
                                        TokenManagerFactory.getInstance(context).clear()
                                        isLoggedIn = false
                                        sessionKey++
                                    }, onItemClick = { name, profileViewMode ->
                                        if (name == DeviceConstant.SYSTEM_INFO) {
                                            cachedProfileViewModel = profileViewMode
                                            navController.navigate("systemInfo")
                                        } else if (name == DeviceConstant.SYSTEM_CONFIG) {
                                            navController.navigate("systemConfig")
                                        }
                                    }

                                )
                            }
                            //系统信息
                            composable("systemInfo") { e ->
                                cachedProfileViewModel?.let {
                                    SystemInfoScreen(
                                        profileViewModel = it,
                                        onBack = { navController.popBackStack() })
                                }
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
}