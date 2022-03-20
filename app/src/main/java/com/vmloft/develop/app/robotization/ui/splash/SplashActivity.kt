package com.vmloft.develop.app.robotization.ui.splash

import com.vmloft.develop.app.robotization.databinding.ActivitySplashBinding
import com.vmloft.develop.library.base.BActivity
import com.vmloft.develop.library.base.router.CRouter

/**
 * Create by lzan13 2022/03/12
 * 描述：闪屏页，做承接调整用
 */
class SplashActivity : BActivity<ActivitySplashBinding>() {

    override fun initVB() = ActivitySplashBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()
        jump()
    }

    override fun initData() {
    }

    private fun jump() {
        CRouter.goMain()
        finish()
    }
}