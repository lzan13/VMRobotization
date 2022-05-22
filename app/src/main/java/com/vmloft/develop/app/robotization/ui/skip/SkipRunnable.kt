package com.vmloft.develop.app.robotization.ui.skip

import android.content.Context

import com.vmloft.develop.app.robotization.service.RobotizationManager
import com.vmloft.develop.library.base.utils.show
import com.vmloft.develop.library.tools.utils.VMSystem

/**
 * Create by lzan13 on 2022/3/18
 * 描述：跳过任务执行程序
 */
class SkipRunnable(val context: Context, val packageName: String) : Runnable {

    private val maxRetryCount = 20 // 最大重复查找次数
    private var adsCount = 0 // 当前重试次数
    private var dialogCount = 0 // 当前重试次数
    private lateinit var config: SkipConfig

    override fun run() {
        config = SkipHelper.getConfig()

        Thread.sleep(config.skipStartDelay)

        skipADS()
        skipDialog()
    }

    /**
     * 执行跳过
     */
    private fun skipADS() {
        if (adsCount >= maxRetryCount) return
        VMSystem.runTask {
            // 延迟一会才处理
            Thread.sleep(config.skipDelay)
            val skipNode = RobotizationManager.getNodeInfo("跳过")
                ?: RobotizationManager.getNodeInfo("跳过广告")
                ?: if (packageName == "com.qiyi.video") RobotizationManager.getNodeInfo("关闭") else null
            if (skipNode == null) {
                adsCount++
                // 没找到控件，继续递归调用
                skipADS()
            } else {
                RobotizationManager.clickNode(skipNode)
                if (config.skipToast) VMSystem.runInUIThread({ context.show(config.toastMsg) })
            }
        }
    }

    /**
     * 执行跳过
     */
    private fun skipDialog() {
        if (dialogCount >= maxRetryCount) return
        VMSystem.runTask {
            // 延迟一会才处理
            Thread.sleep(config.skipDelay)
            val skipNode = RobotizationManager.getNodeInfo("我知道了")
                ?: RobotizationManager.getNodeInfo("以后再说")
                ?: RobotizationManager.getNodeInfo("稍后再说")
                ?: RobotizationManager.getNodeInfo("暂不开启")
                ?: RobotizationManager.getNodeInfo("拒绝", isEquals = true) // 拒绝这里一定要完全匹配，不然会在微信通话被拒绝后触发点击
            if (skipNode == null) {
                dialogCount++
                // 没找到控件，继续递归调用
                skipDialog()
            } else {
                RobotizationManager.clickNode(skipNode)
                if (config.skipToast) VMSystem.runInUIThread({ context.show(config.toastMsg) })
            }
        }
    }
}