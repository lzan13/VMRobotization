package com.vmloft.develop.app.robotization.ui.skip

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.common.SPManager
import com.vmloft.develop.app.robotization.service.RobotizationManager
import com.vmloft.develop.app.robotization.service.RobotizationService
import com.vmloft.develop.library.base.utils.show
import com.vmloft.develop.library.common.utils.JsonUtils
import com.vmloft.develop.library.tools.utils.logger.VMLog
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


/**
 * Create by lzan13 on 2021/8/13
 * 描述：无障碍服务自动跳过帮助类管理类
 */
@SuppressLint("StaticFieldLeak")
object SkipHelper {

    private lateinit var context: Context
    private lateinit var config: SkipConfig // 直播配置

    private lateinit var executorService: ScheduledExecutorService
    private lateinit var executorProcess: ScheduledFuture<Any>

    private var isInit: Boolean = false


    private val homePackages = mutableSetOf<String>() // 首页相关包
    private val launcherPackages = mutableSetOf<String>() // 启动相关包
    private val imePackages = mutableSetOf<String>() // 输入法相关的包
    private val otherPackages = mutableSetOf<String>() // 其他一些包

    private var currPackageName = "" // 记录当前打开应用包名
    private var currClassName = "" // 记录当前打开应用类名

    fun init(context: Context) {
        this.context = context

        if (isInit) {
            return
        }
        isInit = true

        executorService = Executors.newSingleThreadScheduledExecutor()
        executorProcess = executorService.schedule({ }, 0, TimeUnit.MILLISECONDS) as ScheduledFuture<Any>
        // 获取配置信息
        val configStr = SPManager.getSkipConfig()
        config = JsonUtils.fromJson(configStr) ?: SkipConfig()

        setupPackages()

    }

    /**
     * 无障碍服务开启回调
     */
    fun onServiceStart(service: RobotizationService) {
        init(service)
    }

    /**
     * 无障碍服务终端回调
     */
    fun onServiceInterrupt() {
        // 停止自动化操作

    }

    /**
     * 无障碍服务停止回调
     */
    fun onServiceStop() {
        // 停止自动化操作
    }

    /**
     * 无障碍服务事件回调
     */
    fun onServiceEvent(event: AccessibilityEvent) {
        checkSkip(event)
    }

    /**
     * 获取配置
     */
    fun getConfig(): SkipConfig {
        return config
    }

    /**
     * 保存配置
     */
    fun saveConfig() {
        SPManager.setSkipConfig(JsonUtils.toJson(config))
    }

    /**
     * 初始化包集合
     */
    private fun setupPackages() {
        val pm = context.packageManager

        // 启动相关包
        var intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        var list = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        for (item in list) {
            if (!item.activityInfo.packageName.startsWith("com.android.") && !item.activityInfo.packageName.startsWith("com.google.")) {
                launcherPackages.add(item.activityInfo.packageName)
            }
        }
        // 首页相关包
        intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        list = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        for (item in list) {
            homePackages.add(item.activityInfo.packageName)
        }
        // 输入法相关的包
        val inputList = (context.getSystemService(AccessibilityService.INPUT_METHOD_SERVICE) as InputMethodManager).inputMethodList
        for (item in inputList) {
            imePackages.add(item.packageName)
        }
        // 其他一些包
        otherPackages.add(context.packageName)
        otherPackages.add("com.android.settings")

        // 移除不需要跳过处理的包
        launcherPackages.removeAll(homePackages)
        launcherPackages.removeAll(imePackages)
        launcherPackages.removeAll(otherPackages)
    }

    /**
     * 检查跳过
     */
    private fun checkSkip(event: AccessibilityEvent) {
        if (!config.skipSwitch) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)

        var packageName = ""
        event.packageName?.let { packageName = it.toString() }
        var className = ""
        event.className?.let { className = it.toString() }

        if (packageName.isNullOrEmpty() || className.isNullOrEmpty()) return
        // 输入法有可能会在应用运行中临时启动，直接跳过
        if (imePackages.contains(packageName)) return
        // 这里只记录 Activity 的 className
        val isActivity = !className.startsWith("android.") && !className.startsWith("androidx.")
        if (isActivity) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (currPackageName == packageName) {
                    // App 内界面切换，说明已经跳页，不过这个时候不能停止跳过处理，因为有些广告不是在首次启动 Activity 里（比如QQ音乐，每次退到后台再打开也会显示广告）
                    if (currClassName != className) {
                        currClassName = className
                        if (launcherPackages.contains(packageName)) {
                            // 这里一般是页面切换，弹窗通知等，比如青少年模式，通知开启，更新弹窗
                            event.text.forEach {
                                if (it.contains("青少年模式") || it.contains("青少年守护模式") ||
                                    it.contains("开启推送通知") || it.contains("打开通知") ||
                                    it.contains("检测到更新") || it.contains("版本更新")
                                ) {
                                    VMLog.e("有弹窗")
                                    startSkipADS()
                                    return
                                }
                            }
                        }
                    }
                } else {
                    currPackageName = packageName
                    currClassName = className
                    // 包名已切换先停止其他的处理
                    stopSkipADS()
                    // App 包名首次切换开始新的跳过处理
                    if (launcherPackages.contains(packageName)) {
                        startSkipADS()
                    }
                }
            }
        }
    }

    /**
     * 开始执行跳过广告
     */
    private fun startSkipADS() {
        executorProcess = executorService.schedule(SkipRunnable(context, config, currPackageName), config.skipDelay, TimeUnit.MILLISECONDS) as ScheduledFuture<Any>
    }

    /**
     * 停止正在跳过的广告
     */
    private fun stopSkipADS() {
        if (executorProcess.isCancelled || executorProcess.isDone) return
        executorProcess.cancel(false)
    }

}