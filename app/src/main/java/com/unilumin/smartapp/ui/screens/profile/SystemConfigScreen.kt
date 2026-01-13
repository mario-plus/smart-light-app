import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.data.ProductType
import com.unilumin.smartapp.ui.components.CommonTopAppBar
import com.unilumin.smartapp.ui.theme.CardWhite
import com.unilumin.smartapp.ui.theme.PageBackground
import com.unilumin.smartapp.ui.theme.TextDark
import com.unilumin.smartapp.ui.viewModel.ProfileViewModel
import com.unilumin.smartapp.ui.viewModel.SystemViewModel
import com.unilumin.smartapp.ui.viewModel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemConfigScreen(
    retrofitClient: RetrofitClient,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val systemViewModel: SystemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(retrofitClient, context) as T
        }
    })
    val productTypes by systemViewModel.productTypes.collectAsState()

    var isDeviceListExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Column(modifier = Modifier.background(CardWhite)) {
                    CommonTopAppBar(title = "系统配置", onBack = { onBack() })
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
        ) {
            // --- 这里的配置项：设备产品类型管理 ---
            item {
                ConfigExpandableCard(
                    title = "设备类型配置",
                    subtitle = "管理首页显示的设备类型",
                    isExpanded = isDeviceListExpanded,
                    onExpandClick = { isDeviceListExpanded = !isDeviceListExpanded }
                )
            }
            if (isDeviceListExpanded) {
                items(productTypes, key = { it.id }) { product ->
                    DeviceTypeSwitchItem(
                        product = product,
                        onCheckedChange = { isChecked ->
                            systemViewModel.toggleProductType(product.id, isChecked)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 配置项主卡片（外壳）
 */
@Composable
fun ConfigExpandableCard(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, color = TextDark, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

/**
 * 具体的设备开关项
 */
@Composable
fun DeviceTypeSwitchItem(
    product: ProductType,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = CardWhite.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp), // 调大圆角更美观
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp), // 增加点击区域高度
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 显示图标（如果 ProductType 有图标字段的话）
            Icon(
                imageVector = product.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TextDark.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = product.name,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                color = TextDark,
                fontWeight = FontWeight.Medium
            )

            Switch(
                checked = product.isSelected,
                onCheckedChange = onCheckedChange, // 传递给 ViewModel
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}