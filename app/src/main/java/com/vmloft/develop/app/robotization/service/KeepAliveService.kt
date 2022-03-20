package com.vmloft.develop.app.robotization.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vmloft.develop.app.robotization.R
import com.vmloft.develop.app.robotization.ui.main.MainActivity
import com.vmloft.develop.library.base.notify.NotifyManager
import com.vmloft.develop.library.tools.utils.VMStr

/**
 * Create by lzan13 on 2022/3/15
 * 描述：前台保活服务
 */
class KeepAliveService : Service() {

    // 服务通信
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    //服务创建时
    override fun onCreate() {
        super.onCreate()
        // 启动前台保活通知
        openKeepAliveNotify()
        // 有业务逻辑的代码可写在下边
    }

    //服务销毁时
    override fun onDestroy() {
        // 在服务被销毁时，关闭前台通知
        stopForeground(true)
        super.onDestroy()
    }

    /**
     * 开启前台保活通知
     */
    private fun openKeepAliveNotify() {
        val builder: NotificationCompat.Builder = NotifyManager.getBuilder(NotifyManager.getKeepAliveChannelId(), false)

        // 通知标题
        builder.setContentTitle(VMStr.byRes(R.string.app_name))

        // 通知内容
        // 开始在状态栏上显示的提示文案
        // builder.setTicker(content)
        builder.setContentText(VMStr.byRes(R.string.app_keep_alive_tips))
        builder.setShowWhen(false)
        // TODO 视具体业务打开对应界面
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // 设置通知点击跳转
        builder.setContentIntent(pendingIntent)

        val notifyId: Int = NotifyManager.getKeepAliveChannelId().hashCode()
        // 开启前台服务
        startForeground(notifyId, builder.build())
    }

}