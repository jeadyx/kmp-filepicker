package com.jeadyx.example

import androidx.compose.runtime.*
import io.github.jeadyx.FilePickerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable("root") {
        val filePicker = remember { FilePickerFactory.create() }
        var selectedFile by remember { mutableStateOf<String?>(null) }
        var selectedSavePath by remember { mutableStateOf<String?>(null) }
        var selectedDirectory by remember { mutableStateOf<String?>(null) }
        var selectedFiles by remember { mutableStateOf<List<String>>(emptyList()) }
        var savedToFileResult by remember { mutableStateOf<Boolean?>(null) }
        var writeToFileResult by remember { mutableStateOf<Boolean?>(null) }
        var fileBytes by remember { mutableStateOf<ByteArray?>(null) }

        val scope = rememberCoroutineScope()

        Div(
            attrs = {
                style {
                    padding(32.px)
                    maxWidth(600.px)
                    fontFamily("Segoe UI", "sans-serif")
                    property("margin", "0 auto")
                }
            }
        ) {
            H1({
                style {
                    color(Color("#333"))
                    marginBottom(24.px)
                }
            }) {
                Text("📁 File Picker Demo")
            }

            Section("Select Single File") {
                FilePickerButton("Select Single File") {
                    scope.launch {
                        selectedFile = filePicker.pickFile("Select File", listOf("txt", "jpg", "png"))
                    }
                }
                selectedFile?.let { SelectedItem("Selected File", it) }
            }

            Section("Select Multiple Files") {
                FilePickerButton("Select Multiple Files") {
                    scope.launch {
                        selectedFiles = filePicker.pickFiles("Select Files", listOf("txt", "jpg", "png"))
                    }
                }
                if (selectedFiles.isNotEmpty()) {
                    SelectedItem("Selected Files", selectedFiles.joinToString("\n"))
                }
            }

            Section("Save File") {
                FilePickerButton("Save File") {
                    scope.launch {
                        savedToFileResult = filePicker.saveFile("testJSDownload.txt", "生活不止眼前的枸杞 <v_v>")
                    }
                }
                savedToFileResult?.let {
                    SelectedItem("Result", if (it) "✅ Success" else "❌ Failed")
                }
            }

            Section("Pick Save Path") {
                FilePickerButton("Pick Save Path") {
                    scope.launch {
                        selectedSavePath = filePicker.pickSaveLocation("Pick Save Path")
                    }
                }
                selectedSavePath?.let{
                    SelectedItem("Selected Save Path", it)
                }
            }

            Section("Pick Save Directory") {
                FilePickerButton("Pick Save Directory") {
                    scope.launch {
                        selectedDirectory = filePicker.pickDirectory("Pick Save Directory")
                    }
                }
                selectedDirectory?.let {
                    SelectedItem("Selected Save Directory", it)
                }
            }

            Section("Read File") {
                FilePickerButton("Read File") {
                    scope.launch {
                        fileBytes = null
                        fileBytes = filePicker.readFile()
                    }
                }
                fileBytes?.let {
                    SelectedItem("Content", it.decodeToString())
                }
            }

            Section("Write File") {
                FilePickerButton("Write File") {
                    scope.launch {
                        writeToFileResult = filePicker.writeFile(
                            "testWasmWrite.txt",
                            "dige666".encodeToByteArray(),
                            1024
                        )
                    }
                }
                writeToFileResult?.let {
                    SelectedItem("Result", if (it) "✅ Success" else "❌ Failed")
                }
            }
        }
    }
}

@Composable
private fun FilePickerButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        attrs = {
            if (enabled) onClick { onClick() }
            style {
                backgroundColor(if (enabled) Color("#6200EE") else Color.lightgray)
                color(Color.white)
                padding(12.px, 24.px)
                borderRadius(8.px)
                border {
                    style = LineStyle.None
                }
                cursor(if (enabled) "pointer" else "not-allowed")
                property("transition", "background-color 0.3s ease")
                marginBottom(12.px)
            }
        }
    ) {
        Text(text)
    }
}

@Composable
private fun SelectedItem(label: String, value: String) {
    Div(
        attrs = {
            style {
                width(96.percent)
                backgroundColor(Color("#F0F0F0"))
                padding(16.px)
                borderRadius(8.px)
                marginBottom(16.px)
                whiteSpace("pre-wrap")
            }
        }
    ) {
        P({
            style {
                fontWeight("bold")
                marginBottom(4.px)
                color(Color("#333"))
            }
        }) {
            Text(label)
        }

        P {
            Text(value)
        }
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Div(attrs = {
        style {
            marginBottom(32.px)
            padding(16.px)
            backgroundColor(Color("#FAFAFA"))
            borderRadius(12.px)
            border {
                width = 1.px
                color = Color("#DDD")
                style = LineStyle.Solid
            }
        }
    }) {
        content()
    }
}
