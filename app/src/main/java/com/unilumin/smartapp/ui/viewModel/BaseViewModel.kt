package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.util.ToastUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 使用 open 关键字，允许其他 ViewModel 继承
open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    // 提取公共的 Context
    protected val context = getApplication<Application>()
    // 提取公共的 Loading 状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /**
     * 统一封装带有 Loading 和异常处理的协程启动器
     * @param showErrorToast 是否在捕获异常时自动弹出错误提示，默认 true
     * @param consumer 实际要执行的挂起函数（网络请求等）
     */
    protected fun launchWithLoading(
        showErrorToast: Boolean = true,
        consumer: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                consumer()
            } catch (e: Exception) {
                e.printStackTrace()
                if (showErrorToast) {
                    val errorMsg = e.message ?: "未知错误"
                    ToastUtil.showError(context, "操作失败: $errorMsg")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    protected fun launchDirect(
        showErrorToast: Boolean = true,
        consumer: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                consumer()
            } catch (e: Exception) {
                e.printStackTrace()
                if (showErrorToast) {
                    val errorMsg = e.message ?: "未知错误"
                    ToastUtil.showError(context, "操作失败: $errorMsg")
                }
            }
        }
    }

}