package io.github.jeadyx

import io.github.jeadyx.ComposeFilePicker


actual object FilePickerFactory {
    actual fun create(activity: Any?): ComposeFilePicker {
        return WasmFilePicker()
    }
} 