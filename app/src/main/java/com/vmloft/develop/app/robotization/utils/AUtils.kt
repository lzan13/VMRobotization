package com.vmloft.develop.app.robotization.utils

import android.app.ActivityManager
import android.content.Context
import com.vmloft.develop.app.robotization.app.App

/**
 * Create by lzan13 on 2022/3/15
 * 描述：工具类
 */
object AUtils {

    /**
     * 设置任务栈状态
     */
    fun setHideTaskStatus(hide: Boolean = false) {
        val am = App.appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.appTasks
        if (!tasks.isNullOrEmpty()) {
            tasks[0].setExcludeFromRecents(hide)
        }
    }
}