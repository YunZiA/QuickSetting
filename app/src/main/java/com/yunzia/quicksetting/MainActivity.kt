package com.yunzia.quicksetting

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import top.yukonga.miuix.kmp.extra.CheckboxLocation
import top.yukonga.miuix.kmp.extra.SpinnerEntry
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.extra.SuperSpinner
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.basic.Check
import top.yukonga.miuix.kmp.theme.MiuixTheme
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

    val workModeList = listOf("基础","adb", "root")
    val user = remember { mutableIntStateOf(if (Helpers.isRoot()) 2 else 0) }
    val initDialog = remember { mutableStateOf(user.intValue == 0) }

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
        title = "工作模式",
        insideMargin = DpSize(0.dp, 24.dp),
        onDismissRequest={
            initDialog.value = false
        }
    ) {
        workModeList.forEachIndexed { index,mode->

            SpinnerItemImpl(
                title = mode,
                index = index,
                isSelected = user.intValue == index,
                onSelectedIndexChange = {_->
                    user.intValue = index
                    dismissDialog(initDialog)
                }
            )

        }

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
            item {
                Card(
                    modifier= Modifier.padding(horizontal = 12.dp).padding(top = 16.dp)
                ) {
                    SuperArrow(
                        title = "当前工作模式",
                        rightText = workModeList.get(user.intValue),
                        onClick = {
                            initDialog.value = true
                        }
                    )
                }
            }
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
                SuperArrow(
                    title = "使用二维码配对设备",
                    onClick = {
                        "com.android.settings.development.AdbQrCodePreferenceController"
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

@Composable
fun SpinnerItemImpl(
    title: String,
    isSelected: Boolean,
    index: Int,
    onSelectedIndexChange: (Int) -> Unit,
) {
    val additionalTopPadding =  12f.dp
    val additionalBottomPadding =  12f.dp
    val titleColor: Color
    val summaryColor: Color
    val selectColor: Color
    val backgroundColor: Color
    if (isSelected) {
        titleColor = MiuixTheme.colorScheme.onTertiaryContainer
        summaryColor = MiuixTheme.colorScheme.onTertiaryContainer
        selectColor = MiuixTheme.colorScheme.onTertiaryContainer
        backgroundColor = MiuixTheme.colorScheme.tertiaryContainer
    } else {
        titleColor = MiuixTheme.colorScheme.onSurface
        summaryColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
        selectColor = Color.Transparent
        backgroundColor = MiuixTheme.colorScheme.surface
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .heightIn(min = 56.dp)
            .widthIn(min = 200.dp)
            .fillMaxWidth()
            .clickable {
                onSelectedIndexChange(index)
            }
            .background(backgroundColor)
            .padding(horizontal = 28.dp)
            .padding(top = additionalTopPadding, bottom = additionalBottomPadding)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Column {
                Text(
                    text = title,
                    fontSize = MiuixTheme.textStyles.headline1.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
            }
        }
        Image(
            modifier = Modifier.padding(start = 12.dp).size(20.dp),
            imageVector = MiuixIcons.Basic.Check,
            colorFilter = BlendModeColorFilter(selectColor, BlendMode.SrcIn),
            contentDescription = null,
        )
    }
}

fun LazyListScope.titleItem(
    key: String,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit
){
    item(key){
        Spacer(Modifier.height(6.dp))
        SmallTitle(key)
        Card(
            modifier= Modifier.padding(horizontal = 12.dp)
        ){
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WIFIEnhanceTheme {
        App()
    }
}