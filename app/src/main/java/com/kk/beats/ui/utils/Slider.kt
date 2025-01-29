package com.kk.beats.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Slider() {
    var volume by remember { mutableStateOf(0.5f) } // 初始音量值，范围从 0.0f 到 1.0f

    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Volume: ${(volume * 100).toInt()}%") // 显示当前音量百分比
        Slider(
            value = volume,
            onValueChange = { newValue ->
                volume = newValue
            },
            valueRange = 0f..1f, // 音量范围从 0.0f 到 1.0f
            onValueChangeFinished = {
                // 当用户完成拖动时，可以添加额外的逻辑，如保存音量设置
                // 例如将音量设置保存到 SharedPreferences 等
            }
        )
    }
}


// 垂直拖动条
@Composable
fun VerticalSlider(name: String, h: Int, w: Int, sliderValue: Float, onValueChange: (Float) -> Unit) {

    Column(
        modifier = Modifier
            .height(h.dp)
            .width(w.dp)
            .padding(all = 2.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "$name",
            fontSize = 20.sp,
            fontFamily = FontFamily.Cursive,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            valueRange = 0f..400f, // 进度范围
            onValueChangeFinished = {
                // 当用户完成拖动时，可以添加额外的逻辑，如保存音量设置
                // 例如将音量设置保存到 SharedPreferences 等
            },
            modifier = Modifier
//                .fillMaxWidth()
                .fillMaxHeight()
//                .weight(1f)
                .rotate(270f) // 旋转 270 度使其垂直
        )
    }
}