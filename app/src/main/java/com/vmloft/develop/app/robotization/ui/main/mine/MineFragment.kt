package com.vmloft.develop.app.robotization.ui.main.mine

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.common.SPManager
import com.vmloft.develop.app.robotization.databinding.FragmentMineBinding
import com.vmloft.develop.app.robotization.service.KeepAliveService
import com.vmloft.develop.app.robotization.service.RobotizationManager
import com.vmloft.develop.app.robotization.service.RobotizationService
import com.vmloft.develop.app.robotization.utils.AUtils
import com.vmloft.develop.library.base.BFragment
import com.vmloft.develop.library.base.notify.NotifyManager
import com.vmloft.develop.library.base.widget.CommonDialog
import com.vmloft.develop.library.tools.utils.VMStr
import com.vmloft.develop.library.tools.utils.VMSystem


/**
 * Create by lzan13 on 2020/05/02 11:54
 * 描述：我的
 */
class MineFragment : BFragment<FragmentMineBinding>() {

    override fun initVB(inflater: LayoutInflater, parent: ViewGroup?) = FragmentMineBinding.inflate(inflater, parent, false)

    override fun initUI() {
        super.initUI()
        setTopTitle(R.string.app_name)

        mBinding.accessibilityStatusLV.setOnClickListener {
            if (VMSystem.checkAccessibilityStatus(requireContext(), RobotizationService::class.java.name)) {
                RobotizationManager.closeService()
            } else {
                VMSystem.openAccessibilitySettings(requireContext())
            }
        }
        mBinding.keepAliveStatusLV.setOnClickListener { changeKeepAlive() }
        mBinding.hideTaskStatusLV.setOnClickListener { setHideTaskStatus() }
    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        bindInfo()
    }

    /**
     * 切换保活开关
     */
    private fun changeKeepAlive() {
        if (!NotifyManager.checkNotifySetting()) {
            mDialog = CommonDialog(requireContext())
            mDialog?.setContent(R.string.keep_alive_notify_dialog)
            mDialog?.setPositive { NotifyManager.openNotifySetting() }
            mDialog?.show()
            return
        }
        SPManager.setKeepAliveStatus(!SPManager.getKeepAliveStatus())
        mBinding.keepAliveStatusLV.isActivated = SPManager.getKeepAliveStatus()

        val intent = Intent(requireContext(), KeepAliveService::class.java)
        if (SPManager.getKeepAliveStatus()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        } else {
            requireContext().stopService(intent)
        }
    }

    /**
     * 设置隐藏任务栈状态
     */
    private fun setHideTaskStatus() {
        SPManager.setHideTaskStatus(!SPManager.getHideTaskStatus())
        mBinding.hideTaskStatusLV.isActivated = SPManager.getHideTaskStatus()

        AUtils.setHideTaskStatus(SPManager.getHideTaskStatus())
    }

    private fun bindInfo() {
        mBinding.accessibilityStatusLV.isActivated = VMSystem.checkAccessibilityStatus(requireContext(), RobotizationService::class.java.name)
        mBinding.keepAliveStatusLV.isActivated = SPManager.getKeepAliveStatus()
        mBinding.hideTaskStatusLV.isActivated = SPManager.getHideTaskStatus()

    }
}