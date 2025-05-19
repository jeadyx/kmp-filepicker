package com.jeadyx.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jeadyx.ComposeFilePicker
import io.github.jeadyx.FilePickerFactory
import io.github.jeadyx.Platform
import kotlinx.coroutines.launch

@Composable
fun App(filePicker: ComposeFilePicker?=null) {

    var selectedFile by remember { mutableStateOf<String?>(null) }
    var selectedFiles by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedDirectory by remember { mutableStateOf<String?>(null) }
    var saveToDirectoryResult by remember { mutableStateOf<Boolean?>(null) }

    var selectedSaveFile by remember { mutableStateOf<String?>(null) }
    var savedToFileResult by remember { mutableStateOf<Boolean?>(null) }

    var fileBytes by remember { mutableStateOf<ByteArray?>(null) }

    var writeToFileResult by remember { mutableStateOf<Boolean?>(null) }


    val filePicker = remember { filePicker ?: FilePickerFactory.create() }
    val scope = rememberCoroutineScope()
    val useEn = remember { filePicker.platform == Platform.WASM }
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val verticalScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (useEn) "Demo for file picker" else "文件选择器示例",
                    style = MaterialTheme.typography.headlineMedium
                )
                // 选择单个文件
                FilePickerButton(
                    text = if (useEn) "Select Single File" else "选择单个文件",
                    icon = Icons.Default.AccountBox,
                    onClick = {
                        scope.launch {
                            selectedFile = filePicker.pickFile(
                                title = if (useEn) "Select File" else "选择文件",
                                allowedExtensions = listOf("txt", "pdf", "jpg", "png")
                            )
                        }
                    }
                )
                selectedFile?.let {
                    SelectedItem(if (useEn) "Selected File Name/Path" else "已选择文件", it)
                }

                // 选择多个文件
                FilePickerButton(
                    text = if (useEn) "Select Multi Files" else "选择多个文件",
                    icon = Icons.Default.Face,
                    onClick = {
                        scope.launch {
                            selectedFiles = filePicker.pickFiles(
                                title = if (useEn) "Select Multi Files" else "选择多个文件",
                                allowedExtensions = listOf("txt", "pdf", "jpg", "png")
                            )
                        }
                    }
                )
                if (selectedFiles.isNotEmpty()) {
                    SelectedItem(
                        if (useEn) "Selected Files Name/Path" else "已选择文件",
                        selectedFiles.joinToString("\n")
                    )
                }

                // 选择保存位置
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilePickerButton(
                        text = if (useEn) "Select Save Path" else "选择保存位置",
                        icon = Icons.Default.Call,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                selectedSaveFile = filePicker.pickSaveLocation(
                                    title = if (useEn) "Select Save Path" else "保存位置",
                                    defaultName = "document.txt",
                                    allowedExtensions = listOf("txt")
                                )
                                savedToFileResult = null
//                                // 直接保存内容
//                                selectedSaveFile?.let {
//                                    savedToFileResult = filePicker.saveFile(
//                                        filePath = "$selectedSaveFile",
//                                        content = "测试选择保存文件保存\n第二行内容"
//                                    )
//                                }
                            }
                        }
                    )
                    FilePickerButton(
                        text = if (useEn) "Save Sample Content" else "保存示例内容",
                        icon = Icons.Default.Call,
                        modifier = Modifier.weight(1f),
                        enabled = selectedSaveFile != null,
                        onClick = {
                            scope.launch {
                                savedToFileResult = filePicker.saveFile(
                                    filePath = "$selectedSaveFile",
                                    content = if (useEn) "success to save file.\n<^_^>" else "测试选择保存文件保存\n第二行内容<^_^>"
                                )
                            }
                        }
                    )
                }
                selectedSaveFile?.let {
                    SelectedItem(if (useEn) "Selected Save Path" else "选择的保存位置", it)
                    savedToFileResult?.let { result ->
                        SelectedItem(
                            if (useEn) "Result: " else "保存结果: ",
                            if (result) (if (useEn) "Success" else "保存成功") else (if (useEn) "Failed" else "保存失败")
                        )
                    }
                }

                // 选择目录
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilePickerButton(
                        text = if (useEn) "Select Directory" else "选择目录",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                selectedDirectory =
                                    filePicker.pickDirectory(if (useEn) "Select Directory" else "选择目录")
                                saveToDirectoryResult = null
                                println("get selected directroy")
                                // 直接保存内容
//                                selectedDirectory?.let {
//                                    saveToDirectoryResult = filePicker.saveFile(
//                                        filePath = "$selectedDirectory/sample.txt",
//                                        content = "测试选择保存目录保存\n第二行内容"
//                                    )
//                                }
                            }
                        }
                    )
                    FilePickerButton(
                        text = if (useEn) "Save Sample" else "保存示例文件",
                        icon = Icons.Default.ShoppingCart,
                        modifier = Modifier.weight(1f),
                        enabled = selectedDirectory != null,
                        onClick = {
                            scope.launch {
                                saveToDirectoryResult = filePicker.saveFile(
                                    filePath = "$selectedDirectory/sample.txt",
                                    content = if (useEn) "WOW \n success" else "测试选择保存目录保存\n第二行内容"
                                )
                            }
                        }
                    )
                }
                selectedDirectory?.let {
                    SelectedItem(if (useEn) "Selected Directory" else "已选择目录", it)
                    saveToDirectoryResult?.let { result ->
                        SelectedItem(
                            if (useEn) "Result: " else "保存结果: ",
                            if (result) (if (useEn) "Success" else "保存成功") else (if (useEn) "Failed" else "保存失败")
                        )
                    }
                }

                // 写文件内容
                FilePickerButton(
                    text = if (useEn) "write" else "写文件",
                    icon = Icons.Default.Place,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            writeToFileResult = filePicker.writeFile(
//                                "sampleAndroidWrite.txt",
//                                "content://io.github.jeadyx.filepicker.example.fileprovider/external_docs/12345",
                                if(filePicker.platform==Platform.ANDROID)
                                "content://io.github.jeadyx.filepicker.example.fileprovider/public_docs/test.txt"
                                else "test.txt",
                                "dige666".encodeToByteArray(),
                                1024
                            )
                        }
                    }
                )
                writeToFileResult?.let {
                    SelectedItem(if (useEn) "result: " else "结果：", if (writeToFileResult == true) "sucess" else "failed")
                }

                // 读文件内容
                FilePickerButton(
                    text = if (useEn) "read" else "读写入的文件",
                    icon = Icons.Default.Phone,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            fileBytes = null
//                            fileBytes = filePicker.readFile("sampleAndroidWrite.txt")
                            fileBytes = filePicker.readFile(
                                if(filePicker.platform==Platform.ANDROID)
                                    "content://io.github.jeadyx.filepicker.example.fileprovider/public_docs/test.txt"
                                else "test.txt",
                            )
                        }
                    }
                )
                fileBytes?.let {
                    SelectedItem(if (useEn) "content: " else "文件内容: ", it.decodeToString())
                }
            }
        }
    }
}
@Composable
private fun FilePickerButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick  = onClick,
        modifier = modifier,
        enabled  = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun SelectedItem(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 