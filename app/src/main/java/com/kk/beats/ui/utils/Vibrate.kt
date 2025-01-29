package com.kk.beats.ui.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator


fun Vibrate(context: Context, vibrator: Vibrator, millis: Long) {
    if (vibrator.hasVibrator()) {
        // 安卓11及以上使用VibrationEffect.createOneShot创建单次震动效果
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibrationEffect = VibrationEffect.createOneShot(
                500, VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            // 安卓11以下使用vibrate(long milliseconds)方法
            vibrator.vibrate(millis)
        }
    }

}
