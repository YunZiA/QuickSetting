package com.yunzia.quicksetting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.yunzia.quicksetting.Helpers.sh
import androidx.core.net.toUri


class Helper {
}

fun checkDeveloperOptionsEnabled(context: Context): Boolean {
    return try {
        val result = android.provider.Settings.Secure.getInt(
            context.contentResolver,
            android.provider.Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED
        )
        result == 1
    } catch (e: Exception) {
        Log.e("DeveloperOptions", "Error checking developer options: ${e.message}")
        false
    }
}

fun isUsbDebuggingEnabled(context: Context): Boolean {
    return try {
        val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED)
        adbEnabled == 1
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun isWirelessDebuggingEnabled(context: Context): Boolean {
    return try {
        // 检查 ADB_WIFI_ENABLED 设置
        val adbWifiEnabled = Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0)
        adbWifiEnabled == 1
    } catch (e: Exception) {
        Log.e("WirelessDebugging", "Error checking wireless debugging: ${e.message}")
        false
    }
}

fun isUsbInstallEnabled(context: Context): Boolean {
    return try {
        // 检查是否允许通过 USB 安装应用
        val installNonMarketApps = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.INSTALL_NON_MARKET_APPS, 0
        )
        installNonMarketApps == 1
    } catch (e: Exception) {
        Log.e("UsbInstallCheck", "Error checking USB install status: ${e.message}")
        false
    }
}


fun isUsbDebuggingIEnabled(): Boolean {
    return "getprop persist.security.adbinput".sh() == "1"
}


fun handleUsbDebugging1(enabled: Boolean) {
    val status = if (enabled) 1 else 0
    "resetprop persist.security.adbinput $status".sh()
    //"stop adbd;start adbd".sh()

}

private fun callPreference(context: Context, str: String?, bundle: Bundle?): Bundle? {
    try {
        return context.contentResolver.call(
            "content://com.miui.securitycenter.remoteprovider".toUri(),
            "callPreference",
            str,
            bundle
        )
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
}

fun setPreferenceBoolean(context: Context, str: String?, z: Boolean) {
    val bundle = Bundle()
    bundle.putInt("type", 1)
    bundle.putString("key", str)
    bundle.putBoolean("value", z)
    callPreference(context, "SET", bundle)
}

fun getPreferenceBoolean(context: Context, str: String?, z: Boolean): Boolean {
    val bundle = Bundle()
    bundle.putInt("type", 1)
    bundle.putString("key", str)
    bundle.putBoolean("default", z)
    val callPreference = callPreference(context, "GET", bundle)
    return callPreference?.getBoolean(str, z) ?: z
}

fun isInstallEnabled(context: Context): Boolean {
    return getPreferenceBoolean(context, "security_adb_install_enable", false)
}

fun setInstallEnabled(context: Context, z: Boolean) {
    setPreferenceBoolean(context, "security_adb_install_enable", z)
}

fun getInterceptIntent(str: String?, str2: String?, str3: String?): Intent {
    val intent = Intent("miui.intent.action.SPECIAL_PERMISSIO_NINTERCEPT")
    intent.putExtra("pkgName", str)
    intent.putExtra("permName", str2)
    intent.putExtra("permDesc", str3)
    intent.setPackage("com.miui.securitycenter")
    return intent
}

fun isIntentEnable(context: Context, intent: Intent): Boolean {
    return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty()
}
