package com.unilumin.smartapp.ui.viewModel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.ProjectInfo
import com.unilumin.smartapp.client.data.ResponseData
import com.unilumin.smartapp.client.data.SystemInfo
import com.unilumin.smartapp.client.data.UserInfo
import com.unilumin.smartapp.client.service.ProjectService
import com.unilumin.smartapp.client.service.SystemService
import com.unilumin.smartapp.client.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call

class ProfileViewModel(
    val retrofitClient: RetrofitClient, application: Application
) : AndroidViewModel(application) {
    val context = getApplication<Application>()

    // 本地存储 Key
    private val SP_NAME = "app_config"
    private val KEY_CURRENT_PROJECT_ID = "current_project_id"

    val userService = retrofitClient.getService(UserService::class.java)
    val projectService = retrofitClient.getService(ProjectService::class.java)

    private val systemService = retrofitClient.getService(SystemService::class.java)

    // --- 状态管理 ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    //系统信息
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo = _systemInfo.asStateFlow()

    //项目列表
    private val _projectList = MutableStateFlow<List<ProjectInfo>>(emptyList())
    val projectList = _projectList.asStateFlow()

    //当前项目
    private val _currentProject = MutableStateFlow<ProjectInfo?>(null)
    val currentProject = _currentProject.asStateFlow()

    //用户信息
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo = _userInfo.asStateFlow()

    //用户头像地址
    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl = _userAvatarUrl.asStateFlow()

    fun fetchProjects() {
        viewModelScope.launch {
            try {
                val list = UniCallbackService.parseDataSuspend(
                    projectService.getProjects()
                )
                if (list != null && list.isNotEmpty()) {
                    _projectList.value = list

                    // --- 核心修复逻辑 ---
                    // 1. 获取本地存储的 ID (使用 getLong，默认值为 -1)
                    val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                    val savedProjectId = sp.getLong(KEY_CURRENT_PROJECT_ID, -1L)

                    var targetProject: ProjectInfo? = null

                    // 2. 优先级 1: 内存中已有的选中项 (防止刷新列表时突变)
                    if (_currentProject.value != null) {
                        targetProject = list.find { it.id == _currentProject.value!!.id }
                    }

                    // 3. 优先级 2: 本地存储的 ID (判断不为 -1)
                    if (targetProject == null && savedProjectId != -1L) {
                        targetProject = list.find { it.id == savedProjectId }
                    }

                    // 4. 优先级 3: 列表默认第一项
                    if (targetProject == null) {
                        targetProject = list[0]
                    }

                    // 5. 更新状态并同步保存
                    _currentProject.value = targetProject
                    // 如果当前选中的 ID 与保存的 ID 不一致，则更新本地存储
                    if (savedProjectId != targetProject.id) {
                        sp.edit { putLong(KEY_CURRENT_PROJECT_ID, targetProject.id) }
                    }
                } else {
                    _projectList.value = emptyList()
                    _currentProject.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // --- 新增：获取用户信息请求 ---
    fun fetchUserInfo() {
        launchWithLoading {
            try {
                val user = UniCallbackService.parseDataSuspend(
                    userService.getUserInfo()
                )
                if (user != null) {
                    _userInfo.value = user
                }
                if (user?.avatar != null) {
                    var parseDataSuspend = UniCallbackService.parseDataSuspend(
                        userService.getUserAvatarPath(user.avatar.toString())
                    )
                    _userAvatarUrl.value = parseDataSuspend?.url
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun switchProject(project: ProjectInfo) {
        _currentProject.value = project

        // --- 核心修复：保存用户选择 (使用 putLong) ---
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sp.edit { putLong(KEY_CURRENT_PROJECT_ID, project.id) }

        launchWithLoading {
            UniCallbackService.parseDataSuspend(
                projectService.switchProject(project.id)
            )

        }
    }

    fun getSystemInfo() {
        launchWithLoading {
            try {
                val call: Call<ResponseData<SystemInfo?>?>? = systemService.getSystemInfo()
                var parseDataSuspend =
                    UniCallbackService.parseDataSuspend(call)
                if (parseDataSuspend != null) {
                    _systemInfo.value = parseDataSuspend
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun launchWithLoading(consumer: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                consumer()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}