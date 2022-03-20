package com.vmloft.develop.app.robotization.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.alibaba.android.arouter.facade.annotation.Route

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.common.SPManager
import com.vmloft.develop.app.robotization.databinding.ActivityMainBinding
import com.vmloft.develop.app.robotization.router.AppRouter
import com.vmloft.develop.app.robotization.service.KeepAliveService
import com.vmloft.develop.app.robotization.ui.main.home.HomeFragment
import com.vmloft.develop.app.robotization.ui.main.mine.MineFragment
import com.vmloft.develop.app.robotization.utils.AUtils
import com.vmloft.develop.library.base.BActivity
import com.vmloft.develop.library.base.notify.NotifyManager


/**
 * Create by lzan13 on 2018/4/13
 */
@Route(path = AppRouter.appMain)
class MainActivity : BActivity<ActivityMainBinding>() {

    private val currentTabKey = "currentTabKey"
    private val fragmentKeys = arrayListOf("homeKey", "mineKey")
    private var currentTab = 0
    private var currentFragment: Fragment? = null

    private val fragmentList = arrayListOf<Fragment>()
    private lateinit var homeFragment: HomeFragment
    private lateinit var mineFragment: MineFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            fragmentList.clear()
            initFragmentList()
        } else {
            //内存被回收了，fragments的list也被回收了，重新add进去
            currentTab = savedInstanceState.getInt(currentTabKey)
            fragmentList.clear()
            homeFragment = (supportFragmentManager.findFragmentByTag(fragmentKeys[0]) as HomeFragment?) ?: HomeFragment()
            mineFragment = (supportFragmentManager.findFragmentByTag(fragmentKeys[1]) as MineFragment?) ?: MineFragment()
            fragmentList.run {
                add(homeFragment)
                add(mineFragment)
            }
            currentFragment = fragmentList[currentTab]
        }
        switchFragment(currentTab)
    }

    override fun initVB() = ActivityMainBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()
        setTheme(R.style.AppTheme)

        initBottomNav()
    }

    override fun initData() {
        // 判断是否开启前台
        if (SPManager.getKeepAliveStatus()) {
            val intent = Intent(this, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        // 判断是否隐藏后台
        if (SPManager.getHideTaskStatus()) {
            AUtils.setHideTaskStatus(SPManager.getHideTaskStatus())
        }
    }

    /**
     * 初始化底部导航
     */
    private fun initBottomNav() {
        // TODO 如果导航是多色图标，需要取消 BottomNavigationView 的着色效果，自己去设置 selector
        // mainNav.itemIconTintList = null
        mBinding.mainNav.setOnNavigationItemSelectedListener(onNavigationItemSelected)
    }

    /**
     * 导航监听
     */
    private val onNavigationItemSelected = BottomNavigationView.OnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.nav_home -> switchFragment(0)
            R.id.nav_mine -> switchFragment(1)
        }
        true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前 Tab
        outState.putInt(currentTabKey, currentTab)
    }

    /**
     * 界面切换
     */
    private fun switchFragment(position: Int) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        val oldFragment: Fragment = fragmentList[currentTab]
        val newFragment: Fragment = fragmentList[position]
        if (currentFragment == null) {
            transaction.add(R.id.mainFragmentContainer, newFragment, fragmentKeys[position])
                .commit()
        } else {
            if (newFragment.isAdded) {
                transaction.hide(oldFragment).show(newFragment).commit()
            } else {
                transaction.hide(oldFragment)
                    .add(R.id.mainFragmentContainer, newFragment, fragmentKeys[position])
                    .commit()
            }
        }

        currentTab = position
        currentFragment = newFragment
    }

    /**
     * 初始化 Fragment 集合
     */
    private fun initFragmentList() {
        homeFragment = HomeFragment()
        mineFragment = MineFragment()

        fragmentList.run {
            add(homeFragment)
            add(mineFragment)
        }
    }
}