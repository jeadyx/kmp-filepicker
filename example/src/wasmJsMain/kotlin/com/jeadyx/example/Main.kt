package com.jeadyx.example

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class, ExperimentalStdlibApi::class)
fun main() {
    ComposeViewport(document.body!!){
        App()
    }
}