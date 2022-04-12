package com.vmloft.develop.app.robotization.ui.example

import android.annotation.SuppressLint
import android.content.Context
import com.vmloft.develop.app.robotization.service.RobotizationService


/**
 * Create by lzan13 on 2021/8/13
 * 描述：测试无障碍帮助类管理类
 */
@SuppressLint("StaticFieldLeak")
object ExampleHelper {

    private lateinit var context: Context

    private var service: RobotizationService? = null

    private var isInit: Boolean = false

    fun init(context: Context) {
        if (isInit) {
            return
        }
        isInit = true
        this.context = context
    }

    /**
     * 无障碍服务开启回调
     */
    fun onServiceStart(service: RobotizationService) {
        this.service = service
    }

    /**
     * 无障碍服务中断回调
     */
    fun onServiceInterrupt() {
        // 停止自动化操作

    }

    /**
     * 无障碍服务停止回调
     */
    fun onServiceStop() {
        this.service = null
        // 停止自动化操作
    }


}