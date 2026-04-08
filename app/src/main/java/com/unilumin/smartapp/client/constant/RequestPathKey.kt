package com.unilumin.smartapp.client.constant

object RequestPathKey {

    const val IOT_HUB_MANAGER = "/iot_hub_manager"
    const val SMART_COMMON_APPLICATION = "/smart_common_application"
    const val SMART_ROAD_LAMPS = "/smart_road_lamps"
    const val CAS = "/cas"
    const val SMART_LED = "/smart_led"
    const val ZLM = "/unilumin_zlm"

    const val KEY_LOGIN: String = "$SMART_COMMON_APPLICATION/cas/doLogin"
    const val KEY_ROAD_LIST: String = "$SMART_COMMON_APPLICATION/base/project_road_list"
    const val LEY_LAMP_LIST: String = "$SMART_COMMON_APPLICATION/base/smart_lamppole_list"
    const val LEY_SITE_MAP_POINT: String = "$SMART_COMMON_APPLICATION/base/lamp_map_point_list"
    const val KEY_ENV_DATA_LIST: String = "$SMART_COMMON_APPLICATION/env/env_data"
    const val KEY_CAMERA_LIVE_URL: String = "$SMART_COMMON_APPLICATION/security/camera_live"

    //产品规则
    const val KEY_PRODUCT_RULE: String = "$SMART_COMMON_APPLICATION/base/protocol_rule"

    //站点详情，包含挂载的设备
    const val KEY_SITE_DETAIL: String = "$SMART_COMMON_APPLICATION/base/smart_lamppole_detail"


    const val KEY_CAMERA_LIVE: String = "$ZLM/index/api/webrtc"


    //播放盒列表
    const val KEY_LED_LIST: String = "$SMART_LED/uni-led/led_page_list"

    //素材列表
    const val KEY_LED_FILE_LIST: String = "$SMART_LED/ledmaterial/info/list"

    //播放表列表
    const val KEY_PROGRAM_LIST: String = "$SMART_LED/uni-led/led_program_list"

    //播放盒分组管理
    const val KEY_LED_GROUP_LIST: String = "$SMART_LED/uni-led/device_group_list"

    //分组成员
    const val KEY_LED_GROUP_MEMBER: String = "$SMART_LED/uni-led/device_list_by_group_id"

    //可添加分组成员
    const val KEY_LED_GROUP_DEV_OPTIONAL: String = "$SMART_LED/uni-led/optional_device_list"

    //提交播放盒分组关联成员数据
    const val KEY_LED_GROUP_MEMBER_UPDATE: String = "$SMART_LED/uni-led/device_group_mapping"

    //播放方案
    const val KEY_LED_PLAN_LIST: String = "$SMART_LED/uni-led/led_schedule_list"
    //删除播放方案
    const val KEY_LED_PLAN_DEL: String = "$SMART_LED/uni-led/led_schedule"

    //控制方案详情
    const val KEY_LED_CTL_PLAN_DETAIL: String = "$SMART_LED/uni-led/led_schedule_detail"
    //更新控制方案
    const val KEY_LED_CTL_PLAN_UPDATE: String = "$SMART_LED/uni-led/led_schedule_execute_plan"

    //播放盒(远程控制)
    const val KEY_LED_COMMAND: String = "$SMART_LED/uni-led/led_command"

    //播放盒详情
    const val KEY_LED_DEVICE_DETAIL: String = "$SMART_LED/uni-led/led_detail"

    //播放盒分组日志
    const val KEY_LED_GROUP_LOG: String = "$SMART_LED/uni-led/led_group_command_logs"


    const val KEY_GET_USER: String = "$CAS/uc/user/profile/get"

    const val KEY_GET_PUBLIC_KEY: String = "$CAS/uc/rsa/get_public_key"
    const val KEY_PROJECT_LIST: String = "$CAS/uc/list-projects"
    const val KEY_SWITCH_PROJECT: String = "$CAS/uc/switch-project"
    const val KEY_GET_MINIO_PATH: String = "$CAS/sys/file/get-whole-object-url"

    //获取系统信息
    const val KEY_GET_SYSTEM_INFO: String = "$CAS/monitor/server/get"

    const val KEY_GET_LIGHT_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getLightList"
    const val KEY_GET_GW_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getLightGatewayList"

    const val KEY_GET_LIGHT_GW_LIST: String =
        "$SMART_ROAD_LAMPS/uni_light_getLightControlGatewayList"

    const val KEY_GET_LOOP_LIST: String = "$SMART_ROAD_LAMPS/uni_light_loop_page_list"

    const val KEY_GET_GROUP_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getGroupList"
    const val KEY_GET_STRATEGY_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getStrategyList"
    const val KEY_LAMP_CTL: String = "$SMART_ROAD_LAMPS/uni_light_lightCtl"
    const val KEY_GROUP_CTL: String = "$SMART_ROAD_LAMPS/uni_light_groupCtl"
    const val KEY_LOOP_CTL: String = "$SMART_ROAD_LAMPS/uni_light_loop_cmd"

    const val KEY_JOB_SCENE_LIST: String = "$SMART_ROAD_LAMPS/uni_light_selectTaskKey"

    const val KEY_JOB_LIST: String = "$SMART_ROAD_LAMPS/uni_light_selectTaskList"

    //创建策略，选择分组产品
    const val KEY_GET_GROUP_PRODUCT_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getGroupProductList"

    //分组产品下，可选分组
    const val KEY_GET_STRATEGY_PRODUCT_LIST: String =
        "$SMART_ROAD_LAMPS/uni_light_getStrategyGroupInfoList"


    //亮灯率+在线率
    const val KEY_REAL_TIME_COUNT: String = "$SMART_ROAD_LAMPS/light_real_time_count"

    //当月能耗对比
    const val KEY_GET_LIGHT_ENERGY: String = "$SMART_ROAD_LAMPS/contrast_light_energy"

    //年度用电对比趋势
    const val KEY_GET_ANNUAL_TREND: String = "$SMART_ROAD_LAMPS/annual_power_consumption_trend"

    //今7天用电量
    const val KEY_GET_HOME_LIGHT_ENERGY: String = "$SMART_ROAD_LAMPS/home_light_energy"

    //获取分组成员
    const val KEY_GET_GROUP_MEMBER: String = "$SMART_ROAD_LAMPS/uni_light_getGroupMemberInfo"

    //获取分组产品
    const val KEY_GET_GROUP_PRODUCT: String = "$SMART_ROAD_LAMPS/uni_light_getGroupProduct"

    //创建分组
    const val KEY_GET_GROUP_CREATE: String = "$SMART_ROAD_LAMPS/uni_light_createGroup"

    //集控列表
    const val KEY_GET_GROUP_GATEWAY: String = "$SMART_ROAD_LAMPS/uni_light_group_gateway_list"

    //分组成员添加或删除
    const val KEY_GET_GROUP_DEV_OPT: String = "$SMART_ROAD_LAMPS/uni_light_addDevToGroup"

    //强制移除分组成员
    const val KEY_FORCE_DEL_GROUP_DEV: String = "$SMART_ROAD_LAMPS/force_delete_group_device"

    //获取可添加的分组成员
    const val KEY_GET_GROUP_GET_DEV: String = "$SMART_ROAD_LAMPS/uni_light_getGroupDevList"

    //任务详情
    const val KEY_GET_JOB_DETAIL: String = "$SMART_ROAD_LAMPS/uni_light_getTaskDetailById"

    //创建策略
    const val KEY_CREATE_STRATEGY: String = "$SMART_ROAD_LAMPS/uni_light_strategy"

    //更新策略
    const val KEY_UPDATE_STRATEGY: String = "$SMART_ROAD_LAMPS/uni_light_updateStrategy"

    //下发策略
    const val KEY_SYNC_STRATEGY: String = "$SMART_ROAD_LAMPS/uni_light_send_strategy"

    //取消任务
    const val KEY_CANCEL_TASK: String = "$SMART_ROAD_LAMPS/uni_light_cancelTaskById"


    //设备列表
    const val KEY_GET_DEVICE: String = "$IOT_HUB_MANAGER/iot_device_page"

    //设备详情
    const val KEY_GET_DEVICE_DETAIL: String = "$IOT_HUB_MANAGER/iot_device_detail"

    //设备认证配置
    const val KEY_GET_DEVICE_CONFIG: String = "$IOT_HUB_MANAGER/getDeviceConfig"

    //设备实时数据
    const val KEY_GET_DEVICE_REAL_DATA: String = "$IOT_HUB_MANAGER/get_real_time_data_ts"

    //设备历史数据
    const val KEY_GET_DEVICE_HISTORY_DATA: String = "$IOT_HUB_MANAGER/get_history_data_page"

    //设备图表数据
    const val KEY_GET_DEVICE_SEQUENCE_TSL: String = "$IOT_HUB_MANAGER/get_sequence_tsl"

    //设备离线报表
    const val KEY_GET_DEVICE_STATUS_ANALYSIS: String = "$IOT_HUB_MANAGER/getDevStatusAnalysis"

    //离线设备
    const val KEY_GET_OFFLINE_DEVICE_LIST: String = "$IOT_HUB_MANAGER/offline_device_list"

    //告警列表
    const val KEY_GET_DEVICE_ALARM_LIST: String = "$IOT_HUB_MANAGER/device_alarm_manage_list"

    //产品详情
    const val KEY_GET_PRODUCT_LIST: String = "$IOT_HUB_MANAGER/iot_product_list"

    //产品列表
    const val KEY_GET_SIMPLE_PRODUCT_LIST: String = "$IOT_HUB_MANAGER/simple_product_list"

    //添加设备
    const val KEY_ADD_DEVICE: String = "$IOT_HUB_MANAGER/iot_device"

    const val KEY_SAVE_DEVICE_CONFIG: String = "$IOT_HUB_MANAGER/setDeviceConfig"


}
