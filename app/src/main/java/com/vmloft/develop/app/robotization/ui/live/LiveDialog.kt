package com.vmloft.develop.app.robotization.ui.live

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView

import com.vmloft.develop.app.robotization.databinding.WidgetLiveDialogBinding
import com.vmloft.develop.library.base.utils.show
import com.vmloft.develop.library.tools.base.VMBDialog

/**
 * Create by lzan13 on 2021/07/10 21:41
 * 描述：直播消息输入配置对话框
 */
class LiveDialog(context: Context) : VMBDialog<WidgetLiveDialogBinding>(context) {
    private var config: LiveConfig = LiveHelper.getConfig()

    init {

        mBinding.msgDelayET.setText(config.msgDelay.toString())
        mBinding.msgContentET.setText(config.msgContent)

        mBinding.thumbDelayET.setText(config.thumbDelay.toString())
        mBinding.thumbXET.setText(config.thumbX.toString())
        mBinding.thumbYET.setText(config.thumbY.toString())

        mBinding.buyDelayET.setText(config.buyDelay.toString())
        mBinding.buyContentET.setText(config.buyContent)

        getPositiveTV().setOnClickListener { v ->
            save()
            if (positiveDismissSwitch) {
                dismiss()
            }
        }
    }

    override fun initVB() = WidgetLiveDialogBinding.inflate(LayoutInflater.from(context))

    override fun getNegativeTV(): TextView = mBinding.negativeTV

    override fun getPositiveTV(): TextView = mBinding.positiveTV

    /**
     * 保存配置
     */
    fun save() {
        var input = mBinding.msgDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return context.show("消息间隔时间不能为空")
        } else {
            config.msgDelay = input.toLong()
        }
        input = mBinding.msgContentET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return context.show("消息内容不能为空")
        } else {
            config.msgContent = input
        }

        input = mBinding.thumbDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return context.show("点赞时间间隔不能为空")
        } else {
            config.thumbDelay = input.toLong()
        }
        input = mBinding.thumbXET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return context.show("点赞 X 不能为空")
        } else {
            config.thumbX = input.toInt()
        }
        input = mBinding.thumbYET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return context.show("点赞 Y 不能为空")
        } else {
            config.thumbY = input.toInt()
        }

        input = mBinding.buyDelayET.text.trim().toString()
        if (input.isNullOrEmpty()) {
            return context.show("下单延迟不能为空")
        } else {
            config.buyDelay = input.toLong()
        }
        // 商品选项，可以为空
        input = mBinding.buyContentET.text.trim().toString()
        if (input.isNotEmpty()) {
            config.buyContent = input
        }

        LiveHelper.saveConfig()
        context.show("配置保存成功")
    }

}