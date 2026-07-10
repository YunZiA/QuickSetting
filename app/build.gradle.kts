import com.android.build.api.dsl.ApplicationExtension
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import kotlin.apply
import kotlin.collections.set

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val versionInfo: Pair<Int, String> by lazy {
    val versionFile = file("version.properties")
    val props = Properties().apply { load(FileInputStream(versionFile)) }
    if (versionFile.canRead()) {
        var versionCode = props.getProperty("VERSION_CODE", "1").toInt()
        val versionName = props.getProperty("VERSION_NAME", "1.0")
        val runTasks = gradle.startParameter.taskNames
        System.out.println("> Configure project :runTasks = $runTasks")
        if (":app:assembleDebug" !in runTasks && "" !in runTasks){
            versionCode += 1
            props["VERSION_CODE"] = versionCode.toString()
            props["VERSION_NAME"] = versionName
            FileOutputStream(versionFile).use { output ->
                props.store(output, null)
            }
        }
        System.out.println("> Configure project :{versionCode = $versionCode, versionName = $versionName}")
        Pair(versionCode, versionName)
    } else {
        throw GradleException("Could not find version.properties!")
    }
}

configure<ApplicationExtension> {
    namespace = "com.yunzia.quicksetting"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yunzia.quicksetting"
        minSdk = 33
        targetSdk = 36
        versionCode = versionInfo.first
        versionName = versionInfo.second

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreFile = System.getenv("KEYSTORE_PATH")
    signingConfigs {
        if (keystoreFile != null) {
            create("ci") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                enableV4Signing = true
            }

        }else{
            create("default"){
                enableV4Signing = true

            }
        }

    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName(if (keystoreFile != null) "ci" else "default")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

base {
    archivesName.set("QuickSetting${versionInfo.second}")
}

dependencies {

    implementation(libs.miuix)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}