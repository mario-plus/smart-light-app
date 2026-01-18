package com.unilumin.smartapp.ui.viewModel


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.MinioUrl
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



    fun loadData() {
        fetchProjects()
        fetchUserInfo()
    }






    fun fetchProjects() {
        viewModelScope.launch {
            try {
                val list = UniCallbackService<List<ProjectInfo>>().parseDataSuspend(
                    projectService.getProjects(), context
                )
                if (list != null) {
                    _projectList.value = list
                    val current = _currentProject.value

                    if (current == null && list.isNotEmpty()) {
                        _currentProject.value = list[0]
                    } else if (current != null && list.isNotEmpty()) {
                        val found = list.find { it.id == current.id }
                        if (found != null) {
                            _currentProject.value = found
                        } else {
                            _currentProject.value = list[0]
                        }
                    } else {
                        _currentProject.value = null
                    }
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
                val user = UniCallbackService<UserInfo>().parseDataSuspend(
                    userService.getUserInfo(),
                    context
                )
                if (user != null) {
                    _userInfo.value = user
                }
                if (user?.avatar != null) {
                    var parseDataSuspend = UniCallbackService<MinioUrl>().parseDataSuspend(
                        userService.getUserAvatarPath(user.avatar.toString()),
                        context
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
        launchWithLoading {
            UniCallbackService<String>().parseDataSuspend(
                projectService.switchProject(project.id),
                context
            )

        }
    }

    fun getSystemInfo() {
        viewModelScope.launch {
            try {
                val call: Call<ResponseData<SystemInfo?>?>? = systemService.getSystemInfo()
                var parseDataSuspend =
                    UniCallbackService<SystemInfo>().parseDataSuspend(call, context)
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
