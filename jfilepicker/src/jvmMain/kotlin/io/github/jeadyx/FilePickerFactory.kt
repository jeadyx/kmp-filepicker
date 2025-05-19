package io.github.jeadyx

actual object FilePickerFactory {
    actual fun create(activity: Any?): ComposeFilePicker {
        return JVMFilePicker()
    }
} 