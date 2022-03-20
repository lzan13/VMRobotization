package com.vmloft.develop.app.robotization.widget

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

/**
 * Create by lzan13 on 2022/3/14
 * 描述：悬浮窗触摸监听，主要是为了更新悬浮窗位置
 */
class FloatTouchListener(val wmlp: WindowManager.LayoutParams, val wm: WindowManager) : View.OnTouchListener {
    var x = 0
    var y = 0
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x = event.rawX.toInt()
                y = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.toInt()
                val nowY = event.rawY.toInt()
                val movedX = nowX - x
                val movedY = nowY - y
                x = nowX
                y = nowY
                wmlp.apply {
                    x += movedX
                    y += movedY
                }
                //更新悬浮球控件位置
                wm.updateViewLayout(v, wmlp)
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return false
    }
}