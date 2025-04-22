package com.yunzia.quicksetting

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.yunzia.quicksetting.ui.theme.WIFIEnhanceTheme
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.dismissDialog
import top.yukonga.miuix.kmp.utils.overScrollVertical


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            PreferencesUtil.getInstance().init(this)
            WIFIEnhanceTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {

    val initDialog = remember { mutableStateOf(true) }

    val user = remember { mutableIntStateOf(if (Helpers.isRoot()) 1 else 0) }

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    val context = LocalContext.current


    when(user.intValue){
        0 -> {
            PreferencesUtil.putBoolean("first_use", false)
            PreferencesUtil.putString("mode", "adb")
        }
        1 -> {
            if (!Helpers.isRoot()){
                Helpers.isRoot()
            }
            //PreferencesUtil.putBoolean("first_use", false)
        }
    }

    SuperDialog(
        show = initDialog,
        title = "设置模式",
        insideMargin = DpSize(0.dp, 24.dp),
        onDismissRequest={
            initDialog.value = false
        }
    ) {
        SuperDropdown(
            title = "设置权限",
            insideMargin = PaddingValues(24.dp,16.dp),
            items = listOf("adb", "root"),
            selectedIndex = user.intValue,
            onSelectedIndexChange = {
                user.intValue = it
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            onClick = {
                dismissDialog(initDialog)
            }
        ) {
            Text(text = "关闭")
        }

    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "快捷设置",
                scrollBehavior = scrollBehavior)
        }
    ) { innerPadding ->

        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPadding,
        ) {
            titleItem("快捷开关"){
                val developerOptions = remember { mutableStateOf(checkDeveloperOptionsEnabled(context)) }
                SuperSwitch(
                    title = "开发者选项",
                    checked = developerOptions.value,
                    onCheckedChange = {
                        developerOptions.value = it
                        val developerOptionsEnabled = if (it) 1 else 0
                        Helpers.rootShell("settings put global development_settings_enabled $developerOptionsEnabled")
                    }
                )
                val usbDebugging = remember { mutableStateOf(isUsbDebuggingEnabled(context)) }
                SuperSwitch(
                    title = "USB调试",
                    checked = usbDebugging.value,
                    onCheckedChange = {
                        usbDebugging.value = it
                        val adbEnabled = if (it) 1 else 0
                        Helpers.rootShell("settings put global adb_enabled $adbEnabled")
                    }
                )
                val wirelessDebugging = remember { mutableStateOf(isWirelessDebuggingEnabled(context)) }
                SuperSwitch(
                    title = "无线调试",
                    checked = wirelessDebugging.value,
                    onCheckedChange = {
                        wirelessDebugging.value = it
                        val wirelessDebuggingEnabled = if (it) 1 else 0
                        Helpers.rootShell("settings put global adb_wifi_enabled $wirelessDebuggingEnabled")
                    }
                )
//                val usbInstall = remember { mutableStateOf(isInstallEnabled(context)) }
//                SuperSwitch(
//                    title = "USB安装",
//                    checked = usbInstall.value,
//                    onCheckedChange = {
//                        usbInstall.value = it
//                        setInstallEnabled(context,it)
//                    }
//                )
                val usbDebuggingI = remember { mutableStateOf(isUsbDebuggingIEnabled()) }
                SuperSwitch(
                    title = "USB调试（安全设置）",
                    checked = usbDebuggingI.value,
                    onCheckedChange = {
                        usbDebuggingI.value = it
                        handleUsbDebugging1(it)
                    }
                )

            }
            titleItem("快捷跳转") {

                SuperArrow(
                    title = "开发者选项",
                    onClick = {
                        val intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
                        context.startActivity(intent)
                    }
                )
                SuperArrow(
                    title = "无线调试",
                    onClick = {
                        val intent = Intent()
                        val subSettings = ComponentName("com.android.settings", "com.android.settings.SubSettings")
                        intent.setComponent(subSettings)
                        intent.putExtra(":settings:source_metrics",39)
                        intent.putExtra(":settings:show_fragment","com.android.settings.development.WirelessDebuggingFragment")
                        intent.putExtra(":settings:show_fragment_title","wcnm无线调试")
                        context.startActivity(intent)
                    }
                )
//                Box(
//                    modifier = Modifier.clickable(){
//                        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
//                        if (wifiManager != null && wifiManager.isWifiEnabled) {
//                            wifiManager.disconnect() // 断开当前 WiFi 连接
//                            wifiManager.setWifiEnabled(false)
//                        }
//                    }
//                ){
//                    Text(
//                        text = "断开当前wifi"
//                    )
//
//                }
            }


        }
    }
}

fun LazyListScope.titleItem(
    key: String,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit
){
    item(key){
        SmallTitle(key)
        Card(
            modifier= Modifier.padding(horizontal = 12.dp)
        ){
            content()
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WIFIEnhanceTheme {
        App()
    }
}