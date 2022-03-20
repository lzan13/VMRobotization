package com.vmloft.develop.app.robotization.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup

import com.vmloft.develop.app.robotization.databinding.ItemRobotizationDelegateBinding
import com.vmloft.develop.library.base.BItemDelegate

/**
 * Create by lzan13 on 2021/05/05 17:56
 * 描述：内容详情评论 Item
 */
class ItemAccessibilityDelegate(listener: BItemListener<AccessibilityBean>) : BItemDelegate<AccessibilityBean, ItemRobotizationDelegateBinding>(listener) {
    override fun initVB(inflater: LayoutInflater, parent: ViewGroup) = ItemRobotizationDelegateBinding.inflate(inflater, parent, false)

    override fun onBindView(holder: BItemHolder<ItemRobotizationDelegateBinding>, item: AccessibilityBean) {
        holder.binding.titleTV.text = item.title
        holder.binding.descTV.text = item.desc

    }
}

data class AccessibilityBean(val title: String, val desc: String, val path: String) {}
