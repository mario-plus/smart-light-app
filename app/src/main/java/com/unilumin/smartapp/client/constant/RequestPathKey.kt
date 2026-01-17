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
    const val KEY_ENV_DATA: String =
        "$SMART_COMMON_APPLICATION/env/map_visualization_weather_condition"
    const val KEY_ENV_DATA_LIST: String = "$SMART_COMMON_APPLICATION/env/env_data"
    const val KEY_CAMERA_LIVE_URL: String = "$SMART_COMMON_APPLICATION/security/camera_live"
    const val KEY_CAMERA_LIVE: String = "$ZLM/index/api/webrtc"


    const val KEY_SCENE: String = "$SMART_LED/bn/area_scene_list"
    const val KEY_SCENE_CTRL: String = "$SMART_LED/exhibition/model/central_control"
    const val KEY_LED_LIST: String = "$SMART_LED/uni-led/led_page_list"
    const val KEY_GET_USER: String = "$CAS/uc/user/profile/get"

    const val KEY_GET_PUBLIC_KEY: String = "$CAS/uc/rsa/get_public_key"
    const val KEY_PROJECT_LIST: String = "$CAS/uc/list-projects"
    const val KEY_SWITCH_PROJECT: String = "$CAS/uc/switch-project"
    const val KEY_GET_MINIO_PATH: String = "$CAS/sys/file/get-whole-object-url"

    //获取系统信息
    const val KEY_GET_SYSTEM_INFO: String = "$CAS/monitor/server/get"

    const val KEY_GET_LIGHT_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getLightList"
    const val KEY_GET_GW_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getLightGatewayList"

    const val KEY_GET_LIGHT_GW_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getLightControlGatewayList"

    const val KEY_GET_LOOP_LIST: String = "$SMART_ROAD_LAMPS/uni_light_loop_page_list"

    const val KEY_GET_GROUP_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getGroupList"
    const val KEY_GET_STRATEGY_LIST: String = "$SMART_ROAD_LAMPS/uni_light_getStrategyList"
    const val KEY_LAMP_CTL: String = "$SMART_ROAD_LAMPS/uni_light_lightCtl"
    const val KEY_LOOP_CTL: String = "$SMART_ROAD_LAMPS/uni_light_loop_cmd"

    //设备列表
    const val KEY_GET_DEVICE: String = "$IOT_HUB_MANAGER/iot_device_page"

    //设备详情
    const val KEY_GET_DEVICE_DETAIL: String = "$IOT_HUB_MANAGER/iot_device_detail"

    //设备认证配置
    const val KEY_GET_DEVICE_CONFIG: String = "$IOT_HUB_MANAGER/getDeviceConfig"

    //设备实时数据
    const val KEY_GET_DEVICE_REAL_DATA: String = "$IOT_HUB_MANAGER/get_real_time_data"

    //设备历史数据
    const val KEY_GET_DEVICE_HISTORY_DATA: String = "$IOT_HUB_MANAGER/get_history_data_page"

    //设备图表数据
    const val KEY_GET_DEVICE_SEQUENCE_TSL: String = "$IOT_HUB_MANAGER/get_sequence_tsl"

    //设备离线报表
    const val KEY_GET_DEVICE_STATUS_ANALYSIS: String = "$IOT_HUB_MANAGER/getDevStatusAnalysis"

    //离线设备
    const val KEY_GET_OFFLINE_DEVICE_LIST: String = "$IOT_HUB_MANAGER/offline_device_list"



}
