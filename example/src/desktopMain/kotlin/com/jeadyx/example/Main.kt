package com.jeadyx.example

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "文件选择器示例"
    ) {
        App()
    }
} 