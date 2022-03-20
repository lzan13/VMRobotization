package com.vmloft.develop.app.robotization.ui.main.home

import android.content.ComponentName
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.vmloft.develop.app.robotization.BuildConfig

import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.databinding.FragmentHomeBinding
import com.vmloft.develop.app.robotization.router.AppRouter
import com.vmloft.develop.app.robotization.service.RobotizationService
import com.vmloft.develop.library.base.BFragment
import com.vmloft.develop.library.base.BItemDelegate
import com.vmloft.develop.library.base.router.CRouter
import com.vmloft.develop.library.base.utils.showBar
import com.vmloft.develop.library.base.widget.CommonDialog
import com.vmloft.develop.library.base.widget.decoration.StaggeredItemDecoration
import com.vmloft.develop.library.tools.VMTools
import com.vmloft.develop.library.tools.utils.VMDimen
import com.vmloft.develop.library.tools.utils.VMStr
import com.vmloft.develop.library.tools.utils.VMSystem


/**
 * Create by lzan13 on 2020/05/02 11:54
 * 描述：首页
 */
class HomeFragment : BFragment<FragmentHomeBinding>() {

    // 适配器
    private val adapter by lazy(LazyThreadSafetyMode.NONE) { MultiTypeAdapter() }
    private val items = ArrayList<Any>()
    private val layoutManager by lazy(LazyThreadSafetyMode.NONE) { StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) }


    override fun initVB(inflater: LayoutInflater, parent: ViewGroup?) = FragmentHomeBinding.inflate(inflater, parent, false)

    override fun initUI() {
        super.initUI()
        setTopTitle(R.string.app_name)

        initRecyclerView()

        mBinding.accessibilityStatusLV.setOnClickListener { VMSystem.openAccessibilitySettings(requireContext()) }

    }

    override fun onResume() {
        super.onResume()
        mBinding.accessibilityStatusLV.visibility = if (VMSystem.checkAccessibilityStatus(requireContext(), RobotizationService::class.java.name)) View.GONE else View.VISIBLE
    }

    /**
     * 添加一个按钮
     */
    private fun initRecyclerView() {
        adapter.register(ItemAccessibilityDelegate(object : BItemDelegate.BItemListener<AccessibilityBean> {
            override fun onClick(v: View, data: AccessibilityBean, position: Int) {
                itemClick(data)
            }
        }))

        adapter.items = items

        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.addItemDecoration(StaggeredItemDecoration(VMDimen.dp2px(8)))
        mBinding.recyclerView.adapter = adapter
    }

    override fun initData() {
        items.add(AccessibilityBean(VMStr.byRes(R.string.home_skip_title), VMStr.byRes(R.string.home_skip_desc), AppRouter.appSkip))
        items.add(AccessibilityBean(VMStr.byRes(R.string.home_live_title), VMStr.byRes(R.string.home_live_desc), AppRouter.appLive))
        items.add(AccessibilityBean(VMStr.byRes(R.string.home_feedback_title), VMStr.byRes(R.string.home_feedback_desc), AppRouter.appFeedback))
        if (BuildConfig.DEBUG) {
            items.add(AccessibilityBean(VMStr.byRes(R.string.home_test_title), VMStr.byRes(R.string.home_test_desc), AppRouter.appExample))
        }
    }

    private fun itemClick(data: AccessibilityBean) {
        if (data.path == AppRouter.appFeedback) {
            mDialog = CommonDialog(requireContext())
            mDialog?.setContent(R.string.feedback_dialog)
            mDialog?.setPositive(VMStr.byRes(R.string.feedback_copy_gzh)) {
                VMSystem.copyToClipboard("ckcctx")
                showBar(R.string.feedback_copy_gzh_tips)
                openWeChat()
            }
            mDialog?.show()
        } else {
            CRouter.go(data.path)
        }
    }

    /**
     * 打开微信
     */
    private fun openWeChat() {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
//            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            VMTools.context.startActivity(intent)
        } catch (e: Exception) {

        }
    }
}