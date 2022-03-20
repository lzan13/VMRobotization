package com.vmloft.develop.app.robotization.service

import android.view.accessibility.AccessibilityEvent

import com.vmloft.develop.library.tools.service.VMAutoService

/**
 * Create by lzan13 on 2021/8/12
 * 描述：淘宝直播消息辅助服务
 */
class RobotizationService : VMAutoService() {

    /**
     * 辅助功能事件回调
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        super.onAccessibilityEvent(event)
        RobotizationManager.onEvent(event)
    }

    /**
     * 辅助功能启动
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        RobotizationManager.onStart(this)
    }

    /**
     * 辅助服务被打断
     */
    override fun onInterrupt() {
        super.onInterrupt()
        RobotizationManager.onInterrupt()
    }

    /**
     * 辅助服务结束
     */
    override fun onDestroy() {
        super.onDestroy()
        RobotizationManager.onStop()
    }
}