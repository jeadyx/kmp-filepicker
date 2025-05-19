package io.github.jeadyx

import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class JVMFilePicker : ComposeFilePicker() {
    override val platform: Platform = Platform.JVM
    private val fileChooser = JFileChooser().apply {
        isMultiSelectionEnabled = false
        dialogTitle = "选择文件"
    }

    override suspend fun pickFile(title: String, allowedExtensions: List<String>): String? {
        fileChooser.apply {
            dialogTitle = title
            isMultiSelectionEnabled = false
            fileSelectionMode = JFileChooser.FILES_ONLY
            if (allowedExtensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter(
                    "支持的文件 (${allowedExtensions.joinToString(", ")})",
                    *allowedExtensions.toTypedArray()
                )
            } else {
                fileFilter = null
            }
        }

        return when (fileChooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile.absolutePath
            else -> null
        }
    }

    override suspend fun pickFiles(title: String, allowedExtensions: List<String>): List<String> {
        fileChooser.apply {
            dialogTitle = title
            isMultiSelectionEnabled = true
            fileSelectionMode = JFileChooser.FILES_ONLY
            if (allowedExtensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter(
                    "支持的文件 (${allowedExtensions.joinToString(", ")})",
                    *allowedExtensions.toTypedArray()
                )
            } else {
                fileFilter = null
            }
        }

        return when (fileChooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> fileChooser.selectedFiles.map { it.absolutePath }
            else -> emptyList()
        }
    }

    override suspend fun pickSaveLocation(title: String, defaultName: String, allowedExtensions: List<String>): String? {
        fileChooser.apply {
            dialogTitle = title
            isMultiSelectionEnabled = false
            fileSelectionMode = JFileChooser.FILES_ONLY
            selectedFile = File(defaultName)
            if (allowedExtensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter(
                    "支持的文件 (${allowedExtensions.joinToString(", ")})",
                    *allowedExtensions.toTypedArray()
                )
            } else {
                fileFilter = null
            }
        }

        return when (fileChooser.showSaveDialog(null)) {
            JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile.absolutePath
            else -> null
        }
    }

    override suspend fun pickDirectory(title: String): String? {
        fileChooser.apply {
            dialogTitle = title
            isMultiSelectionEnabled = false
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileFilter = null
        }

        return when (fileChooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile.absolutePath
            else -> null
        }
    }

    @Composable
    override fun FilePickerContent() {
        super.FilePickerContent()
    }

    override suspend fun saveFile(filePath: String, content: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) file.createNewFile()
            file.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun readFile(filePath: String, chunkSize: Int): ByteArray {
        return try {
            val file = File(filePath)
            if (!file.exists()) return byteArrayOf()
            file.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }
    }

    override suspend fun writeFile(filePath: String, content: ByteArray, chunkSize: Int): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) withContext(Dispatchers.IO) {
                file.createNewFile()
            }
            file.writeBytes(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 