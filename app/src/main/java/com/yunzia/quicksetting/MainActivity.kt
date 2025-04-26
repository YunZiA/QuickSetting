package com.yunzia.quicksetting

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.yunzia.quicksetting.Helpers.sh
import com.yunzia.quicksetting.ui.theme.WIFIEnhanceTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
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
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.dismissDialog
import top.yukonga.miuix.kmp.utils.SmoothRoundedCornerShape
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import kotlin.math.log


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val workModeList = listOf("基础","adb", "root")
    val user = remember { mutableIntStateOf(if (Helpers.isRoot()) 2 else 0) }
    val initDialog = remember { mutableStateOf(user.intValue == 0) }

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val update = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()

    val developerOptions = remember { mutableStateOf(checkDeveloperOptionsEnabled(context)) }
    val usbDebugging = remember { mutableStateOf(isUsbDebuggingEnabled(context)) }
    val wirelessDebugging = remember { mutableStateOf(isWirelessDebuggingEnabled(context)) }
    val usbDebuggingI = remember { mutableStateOf(isUsbDebuggingIEnabled()) }


    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            //isRefreshing = true
            delay(100)
            pullToRefreshState.completeRefreshing {
                //isRefreshing = false
            }
        }
    }


    val wifiManager = remember {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    DisposableEffect(wifiManager) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifiState = intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                wirelessDebugging.value = wifiState == WifiManager.WIFI_STATE_ENABLED
            }
        }

        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(receiver, intentFilter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }


    LaunchedEffect(developerOptions.value) {
        if (!developerOptions.value){
            usbDebugging.value = false
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        developerOptions.value  = checkDeveloperOptionsEnabled(context)
        usbDebugging.value  = isUsbDebuggingEnabled(context)
        wirelessDebugging.value  = isWirelessDebuggingEnabled(context)
        usbDebuggingI.value  = isUsbDebuggingIEnabled()
    }


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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
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

        PullToRefresh(
            pullToRefreshState = pullToRefreshState,
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
            onRefresh = {
                developerOptions.value  = checkDeveloperOptionsEnabled(context)
                usbDebugging.value  = isUsbDebuggingEnabled(context)
                wirelessDebugging.value  = isWirelessDebuggingEnabled(context)
                usbDebuggingI.value  = isUsbDebuggingIEnabled()
            }
        ) {

            LazyColumn (
                modifier = Modifier
                    .height(getWindowSize().height.dp)
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding())
            ) {
                item {
                    Card(
                        modifier= Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 16.dp)
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
                    SuperSwitch(
                        title = "开发者选项",
                        checked = developerOptions.value,
                        onCheckedChange = {
                            developerOptions.value = it
                            val developerOptionsEnabled = if (it) 1 else 0
                            Helpers.rootShell("settings put global development_settings_enabled $developerOptionsEnabled")
                        }
                    )
                    SuperSwitch(
                        title = "USB调试",
                        checked = usbDebugging.value,
                        enabled = developerOptions.value,
                        onCheckedChange = {
                            usbDebugging.value = it
                            val adbEnabled = if (it) 1 else 0
                            Helpers.rootShell("settings put global adb_enabled $adbEnabled")
                        }
                    )
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
                        enabled = developerOptions.value,
                        onClick = {
                            val intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
                            launcher.launch(intent)
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
                            launcher.launch(intent)
                        }
                    )
                    SuperArrow(
                        title = "使用二维码配对设备",
                        enabled = wirelessDebugging.value,
                        onClick = {
                            "am start -n com.android.settings/com.android.settings.development.AdbQrCodeActivity\n".sh()
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
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp),
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