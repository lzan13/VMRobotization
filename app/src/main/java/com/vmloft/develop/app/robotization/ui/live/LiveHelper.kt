package com.vmloft.develop.app.robotization.ui.live

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.TextView

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.common.SPManager
import com.vmloft.develop.app.robotization.service.RobotizationManager
import com.vmloft.develop.app.robotization.service.RobotizationService
import com.vmloft.develop.app.robotization.widget.FloatTouchListener
import com.vmloft.develop.library.base.utils.CUtils
import com.vmloft.develop.library.base.utils.show
import com.vmloft.develop.library.common.utils.JsonUtils
import com.vmloft.develop.library.tools.utils.VMStr
import com.vmloft.develop.library.tools.utils.logger.VMLog


/**
 * Create by lzan13 on 2021/8/13
 * 描述：直播消息管理类
 */
@SuppressLint("StaticFieldLeak")
object LiveHelper {

    private const val handleStopWhat = 0x00 // 中断自动化

    private const val handleLiveThumbWhat = 0x10 // 触发直播点赞
    private const val handleLiveMsgWhat = 0x11 // 触发直播消息
    private const val handleLiveBuyWhat = 0x12 // 触发抢购


    private lateinit var context: Context
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams

    private lateinit var config: LiveConfig // 直播配置

    private var floatView: View? = null // 直播悬浮窗

    private var isInit: Boolean = false // 初始化状态

    var floatStatus: Boolean = false // 直播自动化悬浮窗开关
    var thumbStatus: Boolean = false // 直播自动化点赞开关
    var msgStatus: Boolean = false // 直播自动化消息开关
    var buyStatus: Boolean = false // 直播自动化抢单开关

    private var currPackageName = "" // 记录当前打开应用包名
    private var currClassName = "" // 记录当前打开应用类名

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                handleStopWhat -> onServiceInterrupt() // 直播下单
                handleLiveThumbWhat -> liveThumb() // 直播点赞
                handleLiveMsgWhat -> liveMsg() // 直播消息
                handleLiveBuyWhat -> liveBuy() // 直播下单
            }
        }
    }

    fun init(context: Context) {
        this.context = context

        if (isInit) {
            return
        }
        isInit = true

        // 获取配置信息
        val configStr = SPManager.getLiveConfig()
        config = JsonUtils.fromJson(configStr) ?: LiveConfig()
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        VMLog.d("服务中断 onServiceInterrupt")
        // 停止自动化操作
        thumbStatus = false
        msgStatus = false
        buyStatus = false

        bindFloatInfo()
    }

    /**
     * 无障碍服务停止回调
     */
    fun onServiceStop() {
        VMLog.d("服务停止 onServiceStop")
        // 停止自动化操作
        thumbStatus = false
        msgStatus = false
        buyStatus = false

        bindFloatInfo()
    }

    /**
     * 无障碍服务事件变化回调
     */
    fun onServiceEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.text == null || event.text.isEmpty()) return

            event.packageName?.let {
                currPackageName = event.packageName.toString()
            }
            event.className?.let {
                // 这里只记录 Activity 的 className
                val isActivity = !it.startsWith("android.") && !it.startsWith("androidx.")
                if (isActivity) {
                    if (event.text[0].indexOf("桌面") == -1) mHandler.removeMessages(handleStopWhat)
                    // 淘宝从订单返回商详时有个过渡页，这里处理下
                    if (event.text[0] != "加载中…") {
                        currClassName = it.toString()
                    }
                    // 如果是淘宝，执行购买操作，内部会自动判断是否开启了够买，这里只是调用下入口
                    if (event.text[0] == "淘宝" || event.text[0] == "商品图") {
                        liveBuy()
                    }
                }
            }
            // 判断回到首页，用来中断一些需要中断的操作，比如：点赞、发消息等
            if (event.text[0].indexOf("桌面") != -1) {
                // 监听回到桌面 1.5s 后停止服务，这里这样做的原因是，在使用淘宝过程中，页面跳转中间偶尔为夹杂着桌面堆栈信息，导致异常停止
                mHandler.sendEmptyMessageDelayed(handleStopWhat, 300)
            }
        }
    }


    /**
     * 保存配置
     */
    fun getConfig(): LiveConfig {
        return config
    }

    /**
     * 保存配置
     */
    fun saveConfig() {
        SPManager.setLiveConfig(JsonUtils.toJson(config))

        bindFloatInfo()
    }

    /**
     * 改变悬浮窗状态
     */
    fun changeFloatStatus(mini: Boolean = true) {
        val settingsIV = floatView?.findViewById<ImageView>(R.id.settingsIV)
        val miniIV = floatView?.findViewById<ImageView>(R.id.miniIV)
        val floatCL = floatView?.findViewById<View>(R.id.floatCL)

        miniIV?.visibility = if (mini) View.GONE else View.VISIBLE
        settingsIV?.visibility = if (mini) View.GONE else View.VISIBLE
        floatCL?.visibility = if (mini) View.GONE else View.VISIBLE
    }

    /**
     * 开始展示悬浮窗
     */
    fun showFloatWindow() {
        if (floatView != null) {
            return
        }
        layoutParams = WindowManager.LayoutParams()
        // 位置为右侧顶部
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        // 设置宽高自适应
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        // 设置悬浮窗透明
        layoutParams.format = PixelFormat.TRANSPARENT

        // 设置窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        // 设置窗口标志类型，其中 FLAG_NOT_FOCUSABLE 是放置当前悬浮窗拦截点击事件，造成桌面控件不可操作
        // 会拦截输入框获取焦点
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        // 获取要现实的布局
        floatView = LayoutInflater.from(context).inflate(R.layout.widget_live_float_view, null)
        // 添加悬浮窗 View 到窗口
        windowManager.addView(floatView, layoutParams)

        bindFloatInfo()

        // 设置触摸监听，进行更新位置
        floatView?.setOnTouchListener(FloatTouchListener(layoutParams, windowManager))

        // 点击悬浮窗展开
        floatView?.setOnClickListener { changeFloatStatus(false) }
        // 点击缩小
        floatView?.findViewById<ImageView>(R.id.miniIV)?.setOnClickListener { changeFloatStatus(true) }
        // 打开直播配置页
        floatView?.findViewById<ImageView>(R.id.settingsIV)?.setOnClickListener {
            val intent = Intent(context, LiveActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        // 直播点赞开关
        floatView?.findViewById<View>(R.id.liveThumbStatusIV)?.setOnClickListener { changeLiveThumbStatus() }
        // 直播消息开关
        floatView?.findViewById<View>(R.id.liveMsgStatusIV)?.setOnClickListener { changeLiveMsgStatus() }
        // 直播抢单开关
        floatView?.findViewById<View>(R.id.liveBuyStatusIV)?.setOnClickListener { changeLiveBuyStatus() }
        floatView?.findViewById<View>(R.id.liveMsgContentTV)?.setOnClickListener { showLiveDialog() }
    }

    /**
     * 停止悬浮窗
     */
    fun hideFloatView() {
        if (floatView != null) {
            windowManager.removeView(floatView)
            floatView = null
        }
    }

    /**
     * 改变直播消息自动化处理状态
     */
    private fun changeLiveMsgStatus() {
        if (msgStatus) {
            msgStatus = false
            mHandler.removeMessages(handleLiveMsgWhat)
        } else {
            msgStatus = true
            mHandler.sendEmptyMessage(handleLiveMsgWhat)
        }
        bindFloatInfo()
    }

    /**
     * 改变直播点赞自动化处理状态
     */
    private fun changeLiveThumbStatus() {
        if (thumbStatus) {
            thumbStatus = false
            mHandler.removeMessages(handleLiveThumbWhat)
        } else {
            thumbStatus = true
            mHandler.sendEmptyMessage(handleLiveThumbWhat)
        }
        bindFloatInfo()
    }

    /**
     * 改变直播抢单自动化处理状态
     */
    private fun changeLiveBuyStatus() {
        if (buyStatus) {
            buyStatus = false
            mHandler.removeMessages(handleLiveBuyWhat)
        } else {
            buyStatus = true
            mHandler.sendEmptyMessage(handleLiveBuyWhat)
        }
        bindFloatInfo()
    }

    /**
     * 绑定悬浮窗信息
     */
    private fun bindFloatInfo() {
        val thumbStatusIV = floatView?.findViewById<ImageView>(R.id.liveThumbStatusIV)
        val msgStatusIV = floatView?.findViewById<ImageView>(R.id.liveMsgStatusIV)
        val buyStatusIV = floatView?.findViewById<ImageView>(R.id.liveBuyStatusIV)
        val msgContentTV = floatView?.findViewById<TextView>(R.id.liveMsgContentTV)

        thumbStatusIV?.isSelected = thumbStatus
        msgStatusIV?.isSelected = msgStatus
        buyStatusIV?.isSelected = buyStatus
        msgContentTV?.text = config.msgContent
    }

    /**
     * 显示直播配置对话框
     */
    private fun showLiveDialog() {
        val dialog = LiveDialog(context)
        // 设置窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_PHONE)
        }
        dialog.show()
    }

    /**
     * 直播点赞
     */
    private fun liveThumb() {
        if (!thumbStatus) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)
        RobotizationManager.clickPosition(config.thumbX + CUtils.random(16, true), config.thumbY + CUtils.random(16, true))
        // 发送下一条 Handler 消息
        mHandler.sendEmptyMessageDelayed(handleLiveThumbWhat, config.thumbDelay + CUtils.random(32))
    }

    /**
     * 直播消息
     */
    private fun liveMsg() {
        if (!msgStatus) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)
        // 查找输入框点击入口
        var chatNode = when (currPackageName) {
            "com.taobao.taobao" -> RobotizationManager.getNodeInfo("taolive_chat_btn_text", currPackageName)
            "com.ss.android.ugc.aweme" -> RobotizationManager.getNodeInfo("dfo", currPackageName)
            else -> return VMLog.d("-lz- 不支持的直播间")
        }
        chatNode?.let {
            RobotizationManager.clickNode(it)
        }

        Thread.sleep(180)

        // 查找输入框，输入消息
        var msgNode = when (currPackageName) {
            "com.taobao.taobao" -> RobotizationManager.getNodeInfo("taolive_edit_text", currPackageName)
            "com.ss.android.ugc.aweme" -> {
                val msgETContainer = RobotizationManager.getNodeInfo("dj2", currPackageName)
                msgETContainer?.getChild(0)
            }
            else -> return VMLog.d("-lz- 不支持的直播间")
        }
        msgNode?.let {
            RobotizationManager.editAction(it, config.msgContent)
        }

        Thread.sleep(180)

        // 查找发送按钮
        var sendNode = when (currPackageName) {
            "com.taobao.taobao" -> RobotizationManager.getNodeInfo("taolive_edit_send", currPackageName)
            "com.ss.android.ugc.aweme" -> RobotizationManager.getNodeInfo("lq7", currPackageName)
            else -> return VMLog.d("-lz- 不支持的直播间")
        }
        sendNode?.let {
            RobotizationManager.clickNode(it)
        }

        // 发送延迟 Handler 消息
        mHandler.sendEmptyMessageDelayed(handleLiveMsgWhat, config.msgDelay)
    }

    /**
     * 直播抢单
     */
    private fun liveBuy() {
        mHandler.removeMessages(handleLiveBuyWhat)
        if (!buyStatus) return
        if (!RobotizationManager.isInit) return context.show(R.string.accessibility_service_not_start)

        // 只在商品详情和抢购界面才查找对应按钮
        if (currClassName == "com.taobao.android.detail.wrapper.activity.DetailActivity") {
            // 查找确认购买按钮，淘宝确认购买有多重情况
            var buyNode = RobotizationManager.getNodeInfo("确认")
                ?: RobotizationManager.getNodeInfo("领券购买")
                ?: RobotizationManager.getNodeInfo("立即购买")
            // 直接点击按钮有时候会不生效，这里改为点击按钮所在坐标
            // if (buyBtn != null) service?.clickView(buyBtn)
            if (buyNode == null) {
                // 没找到，延迟下一次重新查找
                mHandler.sendEmptyMessageDelayed(handleLiveBuyWhat, config.buyDelay)
            } else {
                // 找到了直接点击
                RobotizationManager.clickNode(buyNode)
            }
        }

        // 商品选择
        if (currClassName == "com.taobao.android.sku.widget.a" || currClassName == "com.taobao.android.tbsku.TBXSkuActivity") {
            if (config.buyContent.isNotEmpty()) {
                val list = VMStr.strToList(config.buyContent)
                list.forEach {
                    Thread.sleep(config.buyDelay)
                    var itemNode = RobotizationManager.getNodeInfo(it)
                    if (itemNode == null) {
                        context.show("没找到 $it 继续")
                        // 没找到，延迟下一次重新查找
                        mHandler.sendEmptyMessageDelayed(handleLiveBuyWhat, config.buyDelay)
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
                // 没找到，延迟下一次重新查找
                mHandler.sendEmptyMessageDelayed(handleLiveBuyWhat, config.buyDelay)
            } else {
                // 找到了直接点击
                RobotizationManager.clickNode(buyNode)
            }
        }
        // 提交订单
        if (currClassName == "com.taobao.android.purchase.aura.TBBuyActivity") {
            // 查找提交订单按钮
            var submitNode = RobotizationManager.getNodeInfo("提交订单")
//            // TODO 测试下
//            if (submitNode == null) {
//                context.show("找不到提交订单按钮，继续")
//                // 没找到，延迟下一次重新查找
//                mHandler.sendEmptyMessageDelayed(handleLiveBuyWhat, config.buyDelay)
//            } else {
//                context.show("找到了提交订单按钮")
//            }
            // 直接点击按钮有时候会不生效，这里改为点击按钮所在坐标
            // if (submitNode != null) service?.clickView(submitBtn)
            if(submitNode ==null) {
                // 没找到，延迟下一次重新查找
                mHandler.sendEmptyMessageDelayed(handleLiveBuyWhat, config.buyDelay)
            }else{
                RobotizationManager.clickNode(submitNode)
            }
        }
    }

}