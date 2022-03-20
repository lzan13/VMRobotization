package com.vmloft.develop.app.robotization.ui.skip

/**
 * Create by lzan13 on 2022/3/14
 * 描述：直播消息自动化配置数据bean
 */
data class SkipConfig(
    var skipSwitch: Boolean = false, // 跳过开关
    var skipToast: Boolean = false, // 跳过提醒开关
    var skipStartDelay: Long = 500, // 延迟时间
    var skipDelay: Long = 220, // 延迟时间
    var toastMsg: String = "👨‍💻小不点在努力搬砖‍", // 消息内容
) {
}