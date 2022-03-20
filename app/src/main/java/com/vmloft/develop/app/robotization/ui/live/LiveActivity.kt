package com.vmloft.develop.app.robotization.ui.live

import android.os.Build

import com.alibaba.android.arouter.facade.annotation.Route
import com.vmloft.develop.app.robotization.R

import com.vmloft.develop.app.robotization.databinding.ActivityLiveBinding
import com.vmloft.develop.app.robotization.router.AppRouter
import com.vmloft.develop.library.base.BActivity
import com.vmloft.develop.library.base.utils.errorBar
import com.vmloft.develop.library.base.utils.showBar
import com.vmloft.develop.library.base.widget.CommonDialog
import com.vmloft.develop.library.tools.utils.VMStr
import com.vmloft.develop.library.tools.utils.VMSystem


/**
 * Create by lzan13 on 2021/8/13
 * 描述：直播消息辅助功能设置界面
 */
@Route(path = AppRouter.appLive)
class LiveActivity : BActivity<ActivityLiveBinding>() {

    override var isHideTopSpace: Boolean = true

    private lateinit var config: LiveConfig

    override fun initVB() = ActivityLiveBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()

        setTopTitle(R.string.live_title)
        setTopEndBtnListener(VMStr.byRes(R.string.btn_save)) { save() }

        mBinding.liveFloatLV.setOnClickListener {
            if (LiveHelper.floatStatus) {
                closeFloatWindow()
            } else {
                openFloatWindow()
            }
        }
    }

    override fun initData() {
        LiveHelper.init(mActivity)

        config = LiveHelper.getConfig()
    }

    override fun onResume() {
        super.onResume()

        bindInfo()
    }

    /**
     * 开始直播自动化悬浮窗
     */
    private fun openFloatWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!VMSystem.checkOverlayPermission(mActivity)) {
                val dialog = CommonDialog(mActivity)
                dialog.setTitle(VMStr.byRes(R.string.permission_float_window_hint))
                dialog.setPositive(listener = {
                    VMSystem.openOverlayPermissionSettings(mActivity)
                })
                dialog.show()
            } else {
                // 修改配置
                LiveHelper.floatStatus = true
                LiveHelper.showFloatWindow()
            }
        } else {
            // 修改配置
            LiveHelper.floatStatus = true
            LiveHelper.showFloatWindow()
        }
        mBinding.liveFloatLV.isActivated = LiveHelper.floatStatus
    }

    /**
     * 停止直播自动化悬浮窗
     */
    private fun closeFloatWindow() {
        // 修改配置
        LiveHelper.floatStatus = false
        // 同时移除悬浮窗
        LiveHelper.hideFloatView()
        mBinding.liveFloatLV.isActivated = LiveHelper.floatStatus
    }

    /**
     * 绑定信息
     */
    private fun bindInfo() {
        mBinding.liveFloatLV.isActivated = LiveHelper.floatStatus

        mBinding.msgDelayET.setText(config.msgDelay.toString())
        mBinding.msgContentET.setText(config.msgContent)

        mBinding.thumbDelayET.setText(config.thumbDelay.toString())
        mBinding.thumbXET.setText(config.thumbX.toString())
        mBinding.thumbYET.setText(config.thumbY.toString())

        mBinding.buyDelayET.setText(config.buyDelay.toString())
        mBinding.buyContentET.setText(config.buyContent)
    }

    /**
     * 保存配置
     */
    private fun save() {
        var input = mBinding.msgDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("消息间隔时间不能为空")
        } else {
            config.msgDelay = input.toLong()
        }
        input = mBinding.msgContentET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("消息内容不能为空")
        } else {
            config.msgContent = input
        }

        input = mBinding.thumbDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("点赞时间间隔不能为空")
        } else {
            config.thumbDelay = input.toLong()
        }
        input = mBinding.thumbXET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("点赞 X 不能为空")
        } else {
            config.thumbX = input.toInt()
        }
        input = mBinding.thumbYET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("点赞 Y 不能为空")
        } else {
            config.thumbY = input.toInt()
        }

        input = mBinding.buyDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("下单延迟不能为空")
        } else {
            config.buyDelay = input.toLong()
        }
        // 商品选项，可以为空
        input = mBinding.buyContentET.text.trim().toString()
        if (input.isNotEmpty()) {
            config.buyContent = input
        }

        LiveHelper.saveConfig()
        showBar("配置保存成功")
    }

}