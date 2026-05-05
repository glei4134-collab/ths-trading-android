package com.ths.tradingai.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "ths_prefs"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString("jwt_token", token).apply()
    }

    fun getToken(context: Context): String? {
        return prefs(context).getString("jwt_token", null)
    }

    fun saveServer(context: Context, server: String) {
        prefs(context).edit().putString("server_url", server).apply()
    }

    fun getServer(context: Context): String? {
        return prefs(context).getString("server_url", null)
    }

    fun saveDeviceId(context: Context, deviceId: String) {
        prefs(context).edit().putString("device_id", deviceId).apply()
    }

    fun getDeviceId(context: Context): String? {
        return prefs(context).getString("device_id", null)
    }

    fun saveDeviceName(context: Context, name: String) {
        prefs(context).edit().putString("device_name", name).apply()
    }

    fun getDeviceName(context: Context): String? {
        return prefs(context).getString("device_name", "我的手机")
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
