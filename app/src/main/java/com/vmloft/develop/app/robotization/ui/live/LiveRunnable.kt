package com.vmloft.develop.app.robotization.ui.live

import android.content.Context

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.service.RobotizationManager
import com.vmloft.develop.library.base.utils.CUtils
import com.vmloft.develop.library.base.utils.show
import com.vmloft.develop.library.tools.utils.VMStr
import com.vmloft.develop.library.tools.utils.VMSystem
import com.vmloft.develop.library.tools.utils.logger.VMLog

/**
 * Create by lzan13 on 2022/3/18
 * 描述：直播任务执行程序
 */
class LiveRunnable(val context: Context, val what: Int = 0, val packageName: String, val className: String) : Runnable {

    private lateinit var config: LiveConfig

    override fun run() {
        config = LiveHelper.getConfig()
        if (what == LiveHelper.liveThumbWhat) {
            liveThumb()
        } else if (what == LiveHelper.liveMsgWhat) {
            liveMsg()
        } else if (what == LiveHelper.liveBuyWhat) {
            liveBuy()
        }
    }

    /**
     * 直播点赞
     */
    private fun liveThumb() {
        if (!LiveHelper.thumbStatus) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)
        RobotizationManager.clickPosition(config.thumbX + CUtils.random(16, true), config.thumbY + CUtils.random(16, true))
        // 循环下一次操作
        Thread.sleep(config.thumbDelay)
        liveThumb()
    }

    /**
     * 直播消息
     */
    private fun liveMsg() {
        if (!LiveHelper.msgStatus) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)
        // 查找输入框点击入口
        var chatNode = when (packageName) {
            "com.taobao.taobao" -> RobotizationManager.getNodeInfo("taolive_chat_btn_text", packageName)
            "com.ss.android.ugc.aweme" -> RobotizationManager.getNodeInfo("dfo", packageName)
            else -> return VMLog.d("-lz- 不支持的直播间")
        }
        chatNode?.let {
            RobotizationManager.clickNode(it)
        }

        Thread.sleep(180)

        // 查找输入框，输入消息
        var msgNode = when (packageName) {
            "com.taobao.taobao" -> RobotizationManager.getNodeInfo("taolive_edit_text", packageName)
            "com.ss.android.ugc.aweme" -> {
                val msgETContainer = RobotizationManager.getNodeInfo("dj2", packageName)
                msgETContainer?.getChild(0)
            }
            else -> return VMLog.d("-lz- 不支持的直播间")
        }
        msgNode?.let {
            RobotizationManager.editAction(it, config.msgContent)
        }

        Thread.sleep(180)

        // 查找发送按钮
        var sendNode = when (packageName) {
            "com.taobao.taobao" -> RobotizationManager.getNodeInfo("taolive_edit_send", packageName)
            "com.ss.android.ugc.aweme" -> RobotizationManager.getNodeInfo("lq7", packageName)
            else -> return VMLog.d("-lz- 不支持的直播间")
        }
        sendNode?.let {
            RobotizationManager.clickNode(it)
        }

        // 循环下一次操作
        Thread.sleep(config.msgDelay)
        liveMsg()
    }

    /**
     * 直播抢单
     */
    private fun liveBuy() {
        if (!LiveHelper.buyStatus) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)

        // 只在商品详情和抢购界面才查找对应按钮
        if (className == "com.taobao.android.detail.wrapper.activity.DetailActivity") {
            // 查找确认购买按钮，淘宝确认购买有多重情况
            var buyNode = RobotizationManager.getNodeInfo("确认")
                ?: RobotizationManager.getNodeInfo("领券购买")
                ?: RobotizationManager.getNodeInfo("立即购买")
            // 直接点击按钮有时候会不生效，这里改为点击按钮所在坐标
            // if (buyBtn != null) service?.clickView(buyBtn)
            if (buyNode == null) {
                // 循环下一次操作
                Thread.sleep(config.buyDelay)
                liveBuy()
            } else {
                // 找到了直接点击
                RobotizationManager.clickNode(buyNode)
            }
        }

        // 商品选择
        if (className == "com.taobao.android.sku.widget.a" || className == "com.taobao.android.tbsku.TBXSkuActivity") {
            if (config.buyContent.isNotEmpty()) {
                val list = VMStr.strToList(config.buyContent)
                list.forEach {
                    Thread.sleep(config.buyDelay)
                    var itemNode = RobotizationManager.getNodeInfo(it)
                    if (itemNode == null) {
                        // 循环下一次操作
                        Thread.sleep(config.buyDelay)
                        liveBuy()
                    } else {
                        RobotizationManager.clickNode(itemNode)
                    }
                }
            }
            Thread.sleep(config.buyDelay)
            // 查找确认购买按钮，淘宝确认购买有多重情况
            var buyNode = RobotizationManager.getNodeInfo("确认")
                ?: RobotizationManager.getNodeInfo("领券购买")
                ?: RobotizationManager.getNodeInfo("立即购买")
            // 直接点击按钮有时候会不生效，这里改为点击按钮所在坐标
            // if (buyBtn != null) service?.clickView(buyBtn)
            if (buyNode == null) {
                // 循环下一次操作，没找到，延迟下一次重新查找
                Thread.sleep(config.buyDelay)
                liveBuy()
            } else {
                // 找到了直接点击
                RobotizationManager.clickNode(buyNode)
            }
        }
        // 提交订单
        if (className == "com.taobao.android.purchase.aura.TBBuyActivity") {
            // 查找提交订单按钮
            var submitNode = RobotizationManager.getNodeInfo("提交订单")
            // TODO 测试下
//            if (submitNode == null) {
//                VMSystem.runInUIThread({ context.show("找不到提交订单按钮，继续") })
//                // 循环下一次操作，没找到，延迟下一次重新查找
//                Thread.sleep(config.buyDelay)
//                liveBuy()
//            } else {
//                VMSystem.runInUIThread({ context.show("找到了提交订单按钮") })
//            }
            // 直接点击按钮有时候会不生效，这里改为点击按钮所在坐标
            // if (submitNode != null) service?.clickView(submitBtn)
            if (submitNode == null) {
                // 循环下一次操作，没找到，延迟下一次重新查找
                Thread.sleep(config.buyDelay)
                liveBuy()
            } else {
                RobotizationManager.clickNode(submitNode)
            }
        }
    }
}