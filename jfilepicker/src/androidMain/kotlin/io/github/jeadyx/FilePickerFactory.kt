package io.github.jeadyx

import androidx.activity.ComponentActivity

actual object FilePickerFactory {
    actual fun create(activity: Any?): ComposeFilePicker {
        return AndroidFilePicker(activity as? ComponentActivity)
    }
}