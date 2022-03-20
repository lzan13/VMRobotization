package com.vmloft.develop.app.robotization.ui.example

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.Handler
import android.os.Message

import com.alibaba.android.arouter.facade.annotation.Route
import com.vmloft.develop.app.robotization.R

import com.vmloft.develop.app.robotization.databinding.ActivityExampleBinding
import com.vmloft.develop.app.robotization.router.AppRouter
import com.vmloft.develop.app.robotization.service.RobotizationManager
import com.vmloft.develop.app.robotization.ui.live.LiveHelper
import com.vmloft.develop.library.base.BActivity
import com.vmloft.develop.library.base.utils.showBar
import com.vmloft.develop.library.base.widget.CommonDialog
import com.vmloft.develop.library.tools.utils.VMStr
import com.vmloft.develop.library.tools.utils.VMSystem
import com.vmloft.develop.library.tools.utils.VMUtils


/**
 * Create by lzan13 on 2021/8/13
 * 描述：无障碍辅助功能测试界面
 */
@Route(path = AppRouter.appExample)
class ExampleActivity : BActivity<ActivityExampleBinding>() {

    private var count = 0

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            multiClick()
        }
    }

    override fun initVB() = ActivityExampleBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()
        setTopTitle("无障碍操作测试")

        //屏幕横滑手势
        mBinding.motionBtn.setOnClickListener {
            RobotizationManager.motionAction(VMUtils.random(4))
        }
        mBinding.clickBtn.setOnClickListener { RobotizationManager.clickNode("测试控件") }
        mBinding.longClickBtn.setOnClickListener { RobotizationManager.longClickNode("测试控件") }
        mBinding.multiClickBtn.setOnClickListener { multiClick() }
        mBinding.backBtn.setOnClickListener { RobotizationManager.keyAction() }
        mBinding.homeBtn.setOnClickListener { RobotizationManager.keyAction(AccessibilityService.GLOBAL_ACTION_HOME) }

        mBinding.testBtn.setOnClickListener { showBar("测试按钮被点击") }
        mBinding.testBtn.setOnLongClickListener {
            showBar("测试按钮被长按")
            false
        }
        mBinding.countBtn.setOnClickListener {
            count++
            mBinding.countBtn.text = "点击次数 $count"
        }
    }

    override fun initData() {

    }

    private fun multiClick() {
        RobotizationManager.clickPosition(400, 600)
        if (count < 50) {
            mHandler.sendEmptyMessageDelayed(0, 50)
        }
    }
}