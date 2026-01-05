package com.unilumin.smartapp.ui.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.auth.ProtocolCache
import com.unilumin.smartapp.auth.TokenManagerFactory
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.LoginRequest
import com.unilumin.smartapp.client.data.LoginResponse
import com.unilumin.smartapp.client.data.RsaPublicKeyRes
import com.unilumin.smartapp.client.service.AuthService
import com.unilumin.smartapp.ui.theme.Blue50
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray100
import com.unilumin.smartapp.ui.theme.Gray200
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray500
import com.unilumin.smartapp.ui.theme.Gray900
import com.unilumin.smartapp.util.EncryptUtil
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun LoginScreen(retrofitClient: RetrofitClient, onLogin: () -> Unit) {

    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("admin@unilumin.cn") }
    var password by remember { mutableStateOf("Unilumin123*") }
    val initialAgreedState = remember { ProtocolCache.hasAgreed(context) }
    var isAgreed by remember { mutableStateOf(initialAgreedState) }
    var hasReadUserAgreement by remember { mutableStateOf(initialAgreedState) }
    var hasReadPrivacyPolicy by remember { mutableStateOf(initialAgreedState) }
    var dialogState by remember { mutableStateOf<Triple<String, String, Int>?>(null) }

    LaunchedEffect(hasReadUserAgreement, hasReadPrivacyPolicy) {
        if (hasReadUserAgreement && hasReadPrivacyPolicy) {
            isAgreed = true
            ProtocolCache.setAgreed(context, true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Blue50, Color.White, Color(0xFFF3E8FF))
                )
            ), contentAlignment = Alignment.Center
    ) {

        IconButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = "Server Settings", tint = Gray900, modifier = Modifier.size(28.dp))
        }

        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(shape = RoundedCornerShape(16.dp), color = Blue600, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Rounded.ElectricBolt, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text("洲明路灯云控", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text("Smart City Lighting Management", fontSize = 14.sp, color = Gray500)
            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // ... 账号密码输入框 (保持不变) ...
                    Text("账号", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray400)
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray200, focusedBorderColor = Blue600)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("密码", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray400)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray200, focusedBorderColor = Blue600)
                    )

                    // --- 协议勾选区域 ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isAgreed,
                            onCheckedChange = { checked ->
                                isAgreed = checked
                                if (checked) {
                                    hasReadUserAgreement = true
                                    hasReadPrivacyPolicy = true
                                    // 手动勾选时，也保存状态
                                    ProtocolCache.setAgreed(context, true)
                                } else {
                                    hasReadUserAgreement = false
                                    hasReadPrivacyPolicy = false
                                    // 取消勾选时，更新状态（视需求而定，通常取消勾选不代表“撤销已读”，但为了逻辑闭环这里置为false）
                                    ProtocolCache.setAgreed(context, false)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Blue600, uncheckedColor = Gray400),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("我已阅读并同意", fontSize = 12.sp, color = Gray500)
                            Text(
                                "《用户协议》",
                                fontSize = 12.sp,
                                color = if (hasReadUserAgreement) Color(0xFF059669) else Blue600,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    val content = readAssetFile(context, "user_agreement.txt")
                                    dialogState = Triple("用户协议", content, 1)
                                }
                            )
                            Text("与", fontSize = 12.sp, color = Gray500)
                            Text(
                                "《隐私政策》",
                                fontSize = 12.sp,
                                color = if (hasReadPrivacyPolicy) Color(0xFF059669) else Blue600,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    val content = readAssetFile(context, "privacy_policy.txt")
                                    dialogState = Triple("隐私政策", content, 2)
                                }
                            )
                            if (hasReadUserAgreement && hasReadPrivacyPolicy) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 登录按钮 (保持不变)
                    Button(
                        onClick = {
                            if (!isAgreed) {
                                Toast.makeText(context, "请先阅读并勾选同意《用户协议》与《隐私政策》", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                try {
                                    doLogin(retrofitClient, context, LoginRequest(username, password))
                                    onLogin()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAgreed) Gray900 else Gray500)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("立即登录", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("© 2025 unilumin smart road app v1.0", fontSize = 12.sp, color = Gray400)
        }
    }

    // --- 协议详情弹窗 (保持不变) ---
    if (dialogState != null) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val (title, content, type) = dialogState!!

        AlertDialog(
            onDismissRequest = { dialogState = null },
            title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = screenHeight * 0.6f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = content, fontSize = 14.sp, color = Gray900, lineHeight = 22.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Divider(color = Gray100, thickness = 1.dp)
                    TextButton(
                        onClick = {
                            if (type == 1) hasReadUserAgreement = true
                            if (type == 2) hasReadPrivacyPolicy = true
                            dialogState = null
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("同意并关闭", color = Blue600, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(vertical = 24.dp)
        )
    }

    if (showSettings) {
        ServerSettingsDialog(
            onDismiss = { showSettings = false },
            onSave = { showSettings = false },
            retrofitClient
        )
    }
}

fun readAssetFile(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                BufferedReader(reader).readText()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "无法加载协议文件：$fileName"
    }
}

suspend fun doLogin(
    retrofitClient: RetrofitClient,
    context: Context,
    loginRequest: LoginRequest
) {
    val authService: AuthService = retrofitClient.getService(AuthService::class.java)
    val publicKeyRes = UniCallbackService<RsaPublicKeyRes>().parseDataSuspend(
        authService.getPublicKey(),
        context
    )
    val rsaPublicKey = publicKeyRes?.rsaPublicKey
    val encryptPass = EncryptUtil().encryptPass(loginRequest.password.toString(), rsaPublicKey)
    val loginRes = UniCallbackService<LoginResponse>().parseDataSuspend(
        authService.login(LoginRequest(loginRequest.username, encryptPass)),
        context
    )
    val instance = TokenManagerFactory.getInstance(context)
    instance.setAccessToken(loginRes?.token.toString())
}