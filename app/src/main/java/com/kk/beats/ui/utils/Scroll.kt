package com.kk.beats.ui.utils

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScrollableRow(h: Int, data: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(h.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        for (item in data) {
            VerticalSlider(item, 250, 65, 0F, {})
        }
    }
}