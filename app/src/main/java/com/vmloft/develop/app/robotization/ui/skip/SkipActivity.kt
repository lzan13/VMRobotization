package com.vmloft.develop.app.robotization.ui.skip

import com.alibaba.android.arouter.facade.annotation.Route

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.databinding.ActivitySkipBinding
import com.vmloft.develop.app.robotization.router.AppRouter
import com.vmloft.develop.library.base.BActivity
import com.vmloft.develop.library.base.utils.errorBar
import com.vmloft.develop.library.base.utils.showBar
import com.vmloft.develop.library.tools.utils.VMStr


/**
 * Create by lzan13 on 2021/8/13
 * 描述：无障碍辅助自动跳过配置界面
 */
@Route(path = AppRouter.appSkip)
class SkipActivity : BActivity<ActivitySkipBinding>() {
    override var isHideTopSpace: Boolean = true

    private lateinit var config: SkipConfig

    override fun initVB() = ActivitySkipBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()

        setTopTitle(R.string.skip_title)
        setTopEndBtnListener(VMStr.byRes(R.string.btn_save)) { save() }

        mBinding.skipSwitchLV.setOnClickListener { changeSkipStatus() }
        mBinding.skipToastLV.setOnClickListener { changeToastStatus() }
    }

    override fun initData() {
        SkipHelper.init(this)

        config = SkipHelper.getConfig()
    }

    override fun onResume() {
        super.onResume()
        bindInfo()
    }

    /**
     * 修改跳过开关状态，这里是实时保存
     */
    private fun changeSkipStatus() {
        config.skipSwitch = !config.skipSwitch
        mBinding.skipSwitchLV.isActivated = config.skipSwitch
        // 保存配置
        SkipHelper.saveConfig()
        showBar("已${if (config.skipSwitch) "开启" else "关闭"}跳过开关")
    }

    /**
     * 修改 toast 状态，这里是实时保存
     */
    private fun changeToastStatus() {
        config.skipToast = !config.skipToast
        mBinding.skipToastLV.isActivated = config.skipToast
        // 保存配置
        SkipHelper.saveConfig()
        showBar("已${if (config.skipToast) "开启" else "关闭"}跳过提醒")
    }

    /**
     * 绑定信息
     */
    private fun bindInfo() {
        mBinding.skipSwitchLV.isActivated = config.skipSwitch
        mBinding.skipToastLV.isActivated = config.skipToast

        mBinding.skipStartDelayET.setText(config.skipStartDelay.toString())
        mBinding.skipDelayET.setText(config.skipDelay.toString())
        mBinding.skipToastET.setText(config.toastMsg)
    }

    /**
     * 保存配置
     */
    private fun save() {
        var input = mBinding.skipDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("延迟时间不能为空")
        } else {
            config.skipDelay = input.toLong()
        }
        input = mBinding.skipToastET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return errorBar("提醒内容不能为空")
        } else {
            config.toastMsg = input
        }

        SkipHelper.saveConfig()
        showBar("配置保存成功")
    }

}