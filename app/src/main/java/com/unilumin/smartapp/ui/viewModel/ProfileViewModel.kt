package com.unilumin.smartapp.ui.viewModel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unilumin.smartapp.client.RetrofitClient
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.MinioUrl
import com.unilumin.smartapp.client.data.ProjectInfo
import com.unilumin.smartapp.client.data.UserInfo
import com.unilumin.smartapp.client.service.ProjectService
import com.unilumin.smartapp.client.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    val retrofitClient: RetrofitClient, val context: Context
) : ViewModel() {
    val userService = retrofitClient.getService(UserService::class.java)
    val projectService = retrofitClient.getService(ProjectService::class.java)
    private val _projectList = MutableStateFlow<List<ProjectInfo>>(emptyList())
    val projectList = _projectList.asStateFlow()
    private val _currentProject = MutableStateFlow<ProjectInfo?>(null)
    val currentProject = _currentProject.asStateFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo = _userInfo.asStateFlow()


    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl = _userAvatarUrl.asStateFlow()

    init {
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
                    if (_currentProject.value == null && list.isNotEmpty()) {
                        _currentProject.value = list[0]
                    }
                    if (list.isEmpty()) {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            UniCallbackService<String>().parseDataSuspend(
                projectService.switchProject(project.id),
                context
            )
            _currentProject.value = project
        }

    }
}
