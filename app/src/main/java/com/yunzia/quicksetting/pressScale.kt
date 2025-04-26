package com.yunzia.quicksetting

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Modifier.pressScale(scaleDownScale: Float = 0.9f): Modifier {
    var isPressed by remember { mutableStateOf(false) }

    // 动画缩放值：按下时缩小，抬起时恢复
    val scale by animateFloatAsState(targetValue = if (isPressed) scaleDownScale else 1f)

    return this
        .scale(scale) // 根据动画值动态缩放
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    Log.d("ggc","press")
                    isPressed = true // 按下时触发
                    try {
                        awaitRelease() // 等待抬起
                    } finally {
                        Log.d("ggc","release")
                        isPressed = false // 抬起时恢复
                    }
                }
            )
        }
}