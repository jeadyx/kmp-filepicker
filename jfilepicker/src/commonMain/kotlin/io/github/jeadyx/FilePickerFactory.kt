package io.github.jeadyx


expect object FilePickerFactory {
    fun create(activity: Any? = null): ComposeFilePicker
} 