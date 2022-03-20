package com.vmloft.develop.app.robotization.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.ui.skip.SkipHelper

import com.vmloft.develop.app.robotization.ui.live.LiveHelper
import com.vmloft.develop.library.base.utils.CUtils
import com.vmloft.develop.library.base.utils.show
import com.vmloft.develop.library.tools.service.VMATypeFind
import com.vmloft.develop.library.tools.utils.VMDimen
import com.vmloft.develop.library.tools.utils.logger.VMLog


/**
 * Create by lzan13 on 2021/8/13
 * 描述：自动化操作管理类
 */
@SuppressLint("StaticFieldLeak")
object RobotizationManager {

    private lateinit var context: Context
    private var service: RobotizationService? = null

    var isInit: Boolean = false
        private set

    /**
     * 回调无障碍服务开始，自动化开始初始化
     */
    fun onStart(context: Context) {
        VMLog.d("辅助服务已开启")
        if (isInit) {
            return
        }
        isInit = true
        this.context = context
        service = context as RobotizationService

        SkipHelper.onServiceStart(service!!)
        LiveHelper.onServiceStart(service!!)
    }

    /**
     * 回调无障碍服务中断
     */
    fun onInterrupt() {
        VMLog.d("辅助服务已中断")

        SkipHelper.onServiceInterrupt()
        LiveHelper.onServiceInterrupt()
    }

    /**
     * 回调无障碍服务停止
     */
    fun onStop() {
        VMLog.d("辅助服务已停止")
        service = null

        SkipHelper.onServiceStop()
        LiveHelper.onServiceStop()
    }

    /**
     * 回调无障碍监听到的事件
     */
    fun onEvent(event: AccessibilityEvent) {
//        VMLog.d("无障碍服务事件触发 ${event.action} - ${event.eventType} - ${event.packageName} - ${event.className}")
        VMLog.d("无障碍服务事件触发 $event")

        // TODO 这里要通知关心事件回调的地方
        // 通知其它辅助功能部分事件变化，具体要怎么做看各自需求
        SkipHelper.onServiceEvent(event)
        LiveHelper.onServiceEvent(event)
    }


    /**
     * 关闭无障碍服务
     */
    fun closeService() {
        if (service == null) return context.show(R.string.accessibility_service_not_start)
        if (isInit && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service?.disableSelf()
        }
    }

    /**
     * 手势动作
     * @param type 执行的手势动作类型 0-上滑 1-下滑 2-左滑 3-右滑
     */
    fun motionAction(type: Int = 0) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return VMLog.e("7.0及以上才能使用手势")
        }
        if (service == null) return context.show(R.string.accessibility_service_not_start)

        val w = VMDimen.screenWidth
        val h = VMDimen.screenHeight
        var x = 0.0f
        var y = 0.0f
        var endX = 100.0f
        var endY = 100.0f
        val path = Path()
        when (type) {
            1 -> {
                x = (w / 2 + CUtils.random(72, true)).toFloat()
                y = (h / 3 + CUtils.random(72, true)).toFloat()
                endX = (w / 2 + CUtils.random(72, true)).toFloat()
                endY = (h * 2 / 3 + CUtils.random(72, true)).toFloat()
                path.moveTo(x, y)
                path.lineTo(endX, endY)
            }
            2 -> {
                x = (w * 2 / 3 + CUtils.random(72, true)).toFloat()
                y = (h / 2 + CUtils.random(72, true)).toFloat()
                endX = (w / 3 + CUtils.random(72, true)).toFloat()
                endY = (h / 2 + CUtils.random(72, true)).toFloat()
                path.moveTo(x, y)
                path.lineTo(endX, endY)
            }
            3 -> {
                x = (w / 3 + CUtils.random(72, true)).toFloat()
                y = (h / 2 + CUtils.random(72, true)).toFloat()
                endX = (w * 2 / 3 + CUtils.random(72, true)).toFloat()
                endY = (h / 2 + CUtils.random(72, true)).toFloat()
                path.moveTo(x, y)
                path.lineTo(endX, endY)
            }
            else -> {
                // 默认都上滑
                x = (w / 2 + CUtils.random(72, true)).toFloat()
                y = (h * 2 / 3 + CUtils.random(72, true)).toFloat()
                endX = (w / 2 + CUtils.random(72, true)).toFloat()
                endY = (h / 3 + CUtils.random(72, true)).toFloat()
                path.moveTo(x, y)
                path.lineTo(endX, endY)
            }
        }

        val sd = GestureDescription.StrokeDescription(path, 0, 500)
        service?.dispatchGesture(GestureDescription.Builder().addStroke(sd).build(), object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                VMLog.d("手势执行成功")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                VMLog.d("手势执行失败，请重启手机再试")
            }
        }, null)
    }

    /**
     * 点击节点
     * @param text 控件上的文字
     */
    fun clickNode(text: String) {
        if (service == null) return context.show(R.string.accessibility_service_not_start)
        val nodeInfo = getNodeInfo(text) ?: return VMLog.d("没有找到控件")

        clickNode(nodeInfo)
    }

    /**
     * 点击节点
     * @param node 节点控件
     */
    fun clickNode(node: AccessibilityNodeInfo) {
        // 直接执行控件的点击操作
        //        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);//长按
        //        service?.clickView(node)

        // 获取控件位置坐标
        val absXY = Rect()
        node.getBoundsInScreen(absXY)
        // 计算下触摸位置，这里加上一个随机数，尽量保证不要每次都是同一个位置
        var x = absXY.left + (absXY.right - absXY.left) / 2 + CUtils.random(16, true)
        var y = absXY.top + (absXY.bottom - absXY.top) / 2 + CUtils.random(16, true)
        // 执行点击操作，位置这里选择控件正中间+随机数
        service?.dispatchGestureClick(x, y)

        node.recycle()
    }

    /**
     * 点击操作
     * @param x x坐标
     * @param y y坐标
     * @param offset 坐标随机偏移范围
     */
    fun clickPosition(x: Int, y: Int, offset: Int = 16) {
        if (service == null) return context.show(R.string.accessibility_service_not_start)
        // 执行点击操作，位置这里选择控件正中间+随机数
        service?.dispatchGestureClick(x + CUtils.random(offset, true), y + CUtils.random(offset, true))
    }

    /**
     * 输入操作
     * @param node 节点控件
     * @param 输入内容
     */
    fun editAction(node: AccessibilityNodeInfo, content: String) {
        service?.editAction(node, content)

        node.recycle()
    }

    /**
     * 长按操作
     * @param text 控件上的文字
     */
    fun longClickNode(text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return VMLog.e("7.0及以上才能使用手势")
        }
        if (service == null) return context.show(R.string.accessibility_service_not_start)
        val nodeInfo = getNodeInfo(text) ?: return VMLog.e("没有找到控件")

        // 直接执行控件的长按操作
        // nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);//长按

        // 获取控件位置坐标
        val absXY = Rect()
        nodeInfo.getBoundsInScreen(absXY)
        // 计算下触摸位置，这里加上一个随机数，尽量保证不要每次都是同一个位置
        var x = absXY.left + (absXY.right - absXY.left) / 2 + CUtils.random(16, true)
        var y = absXY.top + (absXY.bottom - absXY.top) / 2 + CUtils.random(16, true)
        // 执行长按操作，位置这里选择控件正中间+随机数
        service?.dispatchGestureLongClick(x, y)
    }

    /**
     * 执行系统 Action
     */
    fun keyAction(action: Int = AccessibilityService.GLOBAL_ACTION_BACK) {
        if (service == null) return context.show(R.string.accessibility_service_not_start)
        // 可以选择多种系统 action back/home 等
        // AccessibilityService.GLOBAL_ACTION_BACK 返回
        // AccessibilityService.GLOBAL_ACTION_HOME 首页
        service?.performGlobalAction(action)
    }

    /**
     * 获取节点信息
     * @param text 文本内容 或 控件id
     * @param packageName 包名 只有通过控件 id 查找时才需要
     */
    fun getNodeInfo(text: String, packageName: String = ""): AccessibilityNodeInfo? {
        return if (packageName.isNotEmpty()) {
            service?.findFirst(VMATypeFind.newId(packageName, text) as VMATypeFind<Any>)
        } else {
            service?.findFirst(VMATypeFind.newText(text, false) as VMATypeFind<Any>)
        }
    }
}