package com.vmloft.develop.app.robotization.common

import com.vmloft.develop.library.tools.utils.VMSPUtil
import com.vmloft.develop.library.tools.utils.VMSystem

/**
 * Create by lzan13 on 2020/4/25 19:48
 * 描述：SharedPreference 管理
 */
object SPManager {
    /**
     * 记录设置项
     */
    private val settingsEntry = "settings"

    // 本地版本
    private val localVersionKey = "localVersionKey"

    // 隐私协议状态
    private val agreementPolicyKey = "policyStatusKey"

    // 通知开关
    private val notifyMsgSwitchKey = "notifyMsgSwitchKey"
    private val notifyMsgDetailSwitchKey = "notifyMsgDetailSwitchKey"

    // 深色模式
    private val darkModeSystemSwitchKey = "darkModeSystemSwitchKey"
    private val darkModeManualKey = "darkModeManualKey"

    /**
     * 记录登录信息
     */
    private val signEntry = "sign"
    private val tokenKey = "tokenKey"
    private val currUserKey = "currUserKey"
    private val prevUserKey = "prevUserKey"

    /**
     * 记录时间
     */
    private val timeEntry = "time"
    private val categoryTimeKey = "categoryTimeKey"
    private val professionTimeKey = "categoryTimeKey"

    /**
     * 记录自动化配置
     */
    private val accessibilityEntry = "accessibility"
    private val liveConfigKey = "liveConfigKey"
    private val skipConfigKey = "skipConfigKey"
    private val keepAliveStatusKey = "keepAliveStatusKey"
    private val hideTaskStatusKey = "hideTaskStatusKey"

    /**
     * -------------------------------------------------------------------------------
     * 通用的几个方法
     */
    /**
     * 通用获取数据
     */
    fun get(entry: String, key: String, default: Any): Any {
        return VMSPUtil.getEntry(entry).get(key, default)
    }

    /**
     * 通用设置数据
     */
    fun put(entry: String, key: String, value: String) {
        VMSPUtil.getEntry(entry).put(key, value)
    }

    /**
     * 通用设置数据，异步
     */
    fun putAsync(entry: String, key: String, value: Any) {
        VMSPUtil.getEntry(entry).putAsync(key, value)
    }

    /**
     * -------------------------------------------------------------------------------
     * 记录设置项
     */

    /**
     * 保存当前运行版本号
     */
    fun setLocalVersion(version: Long) {
        putAsync(settingsEntry, localVersionKey, version)
    }

    /**
     * 获取当前运行的版本号
     */
    fun getLocalVersion(): Long {
        return get(settingsEntry, localVersionKey, 0L) as Long
    }


    /**
     * 判断启动时是否需要展示引导界面，这里根据本地记录的 appVersion 以及运行 APP 获取到的 appVersion 对比
     */
    fun isGuideShow(): Boolean {
        // 上次运行保存的版本号
        val localVersion = getLocalVersion()
        // 程序当前版本
        val version = VMSystem.versionCode
        return version > localVersion
    }

    /**
     * 隐藏引导界面
     */
    fun setGuideHide() {
        setLocalVersion(VMSystem.versionCode)
    }

    /**
     * 协议与政策状态
     */
    fun isAgreementPolicy(): Boolean = get(settingsEntry, agreementPolicyKey, false) as Boolean
    fun setAgreementPolicy() {
        putAsync(settingsEntry, agreementPolicyKey, true)
    }

    /**
     * 通知开关
     */
    fun setNotifyMsgSwitch(status: Boolean) {
        putAsync(settingsEntry, notifyMsgSwitchKey, status)
    }

    fun isNotifyMsgSwitch(): Boolean = get(settingsEntry, notifyMsgSwitchKey, true) as Boolean

    fun setNotifyMsgDetailSwitch(status: Boolean) {
        putAsync(settingsEntry, notifyMsgDetailSwitchKey, status)
    }

    fun isNotifyMsgDetailSwitch(): Boolean =
        get(settingsEntry, notifyMsgDetailSwitchKey, true) as Boolean

    /**
     * 深色模式
     */
    fun setDarkModeSystemSwitch(status: Boolean) {
        putAsync(settingsEntry, darkModeSystemSwitchKey, status)
    }

    fun isDarkModeSystemSwitch(): Boolean =
        get(settingsEntry, darkModeSystemSwitchKey, true) as Boolean

    fun setDarkModeManual(mode: Int) {
        putAsync(settingsEntry, darkModeManualKey, mode)
    }

    fun getDarkModeManual(): Int {
        return get(settingsEntry, darkModeManualKey, -1) as Int
    }

    /**
     * -------------------------------------------------------------------------------
     * 记录登录信息
     */

    /**
     * token 需要实时更新
     */
    fun getToken(): String {
        return get(signEntry, tokenKey, "") as String
    }

    fun putToken(token: String) {
        put(signEntry, tokenKey, token)
    }

    /**
     * 当前账户登录记录
     *
     * @return 如果为空，说明没有登录记录
     */
    fun getCurrUser(): String {
        return get(signEntry, currUserKey, "") as String
    }

    fun putCurrUser(userJson: String) {
        put(signEntry, currUserKey, userJson)
    }

    /**
     * 上一个账户登录记录
     *
     * @return 如果为空，说明没有登录记录
     */
    fun getPrevUser(): String {
        return get(signEntry, prevUserKey, "") as String
    }

    fun putPrevUser(userJson: String) {
        putAsync(signEntry, prevUserKey, userJson)
    }

    /**
     * -------------------------------------------------------------------------------
     * 记录时间信息
     */

    /**
     * 获取最近一次分类获取缓存时间
     */
    fun getCategoryTime(): Long {
        return get(timeEntry, categoryTimeKey, 0L) as Long
    }

    /**
     * 设置最近一次分类获取缓存时间
     */
    fun setCategoryTime(time: Long) {
        putAsync(timeEntry, categoryTimeKey, time)
    }

    /**
     * 获取最近一次职业获取缓存时间
     */
    fun getProfessionTime(): Long {
        return get(timeEntry, professionTimeKey, 0L) as Long
    }

    /**
     * 设置最近一次职业获取缓存时间
     */
    fun setProfessionTime(time: Long) {
        putAsync(timeEntry, professionTimeKey, time)
    }


    /**
     * -------------------------------------------------------------------------------
     * 辅助功能记录
     */

    /**
     * 获取自动化直播配置
     */
    fun getLiveConfig(): String = get(accessibilityEntry, liveConfigKey, "") as String

    /**
     * 设置自动化直播配置
     */
    fun setLiveConfig(config: String) {
        putAsync(accessibilityEntry, liveConfigKey, config)
    }

    /**
     * 获取自动化跳过配置
     */
    fun getSkipConfig(): String = get(accessibilityEntry, skipConfigKey, "") as String

    /**
     * 设置自动化跳过配置
     */
    fun setSkipConfig(config: String) {
        putAsync(accessibilityEntry, skipConfigKey, config)
    }

    /**
     * 获取保活开关状态
     */
    fun getKeepAliveStatus(): Boolean = get(accessibilityEntry, keepAliveStatusKey, false) as Boolean

    /**
     * 设置保活开关状态
     */
    fun setKeepAliveStatus(keep: Boolean) {
        putAsync(accessibilityEntry, keepAliveStatusKey, keep)
    }

    /**
     * 获取隐藏任务栈状态
     */
    fun getHideTaskStatus(): Boolean = get(accessibilityEntry, hideTaskStatusKey, false) as Boolean

    /**
     * 设置隐藏任务栈状态
     */
    fun setHideTaskStatus(hide: Boolean) {
        putAsync(accessibilityEntry, hideTaskStatusKey, hide)
    }

}