package com.vmloft.develop.app.robotization.ui.live

/**
 * Create by lzan13 on 2022/3/14
 * 描述：直播消息自动化配置数据bean
 */
data class LiveConfig(
    var msgDelay: Long = 2800, // 消息延迟
    var msgContent: String = "我来了 😁", // 消息内容

    var thumbDelay: Long = 180, // 点赞延迟
    var thumbX: Int = 850, // 点赞X坐标 触发位置是在这个坐标点附近随机生成
    var thumbY: Int = 1080, // 点赞Y坐标

    var buyDelay: Long = 150, // 下单按钮点击延迟
    var buyContent: String = "", // 商品选项内容，逗号分隔

) {
}