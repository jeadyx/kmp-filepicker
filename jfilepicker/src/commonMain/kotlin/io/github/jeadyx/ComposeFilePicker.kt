package io.github.jeadyx

import androidx.compose.runtime.*
import io.github.jeadyx.ui.FilePickerDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

open class ComposeFilePicker : FilePicker {
    override val platform: Platform = Platform.UNKNOWN
    private val _currentPath = MutableStateFlow("")
    private val _items = MutableStateFlow<List<FileItem>>(emptyList())
    private val _mode = MutableStateFlow(FilePickerMode.OPEN_FILE)
    private val _title = MutableStateFlow("选择文件")
    private val _allowedExtensions = MutableStateFlow<List<String>>(emptyList())
    private val _defaultName = MutableStateFlow("")
    
    protected var onResult: ((String?) -> Unit)? = null
    protected var onMultiResult: ((List<String>) -> Unit)? = null

    val currentPath: StateFlow<String> = _currentPath.asStateFlow()
    val items: StateFlow<List<FileItem>> = _items.asStateFlow()
    val mode: StateFlow<FilePickerMode> = _mode.asStateFlow()
    val title: StateFlow<String> = _title.asStateFlow()
    val allowedExtensions: StateFlow<List<String>> = _allowedExtensions.asStateFlow()
    val defaultName: StateFlow<String> = _defaultName.asStateFlow()

    @Composable
    open fun FilePickerContent() {
        var showDialog by remember { mutableStateOf(true) }

        if (showDialog) {
            FilePickerDialog(
                title = title.value,
                mode = mode.value,
                currentPath = currentPath.value,
                items = items.value,
                onItemClick = { item ->
                    if (item.isDirectory) {
                        navigateToDirectory(item.path)
                    } else {
                        selectFile(item)
                    }
                },
                onNavigateUp = {
                    navigateUp()
                },
                onConfirm = {
                    confirmSelection()
                    showDialog = false
                },
                onDismiss = {
                    cancelSelection()
                    showDialog = false
                }
            )
        }
        LaunchedEffect(Unit){
            println("exec launcher edfffect")
        }
    }

    override suspend fun pickFile(title: String, allowedExtensions: List<String>): String? {
        println("pcik file clicked ")
        return suspendCancellableCoroutine { continuation ->
            _title.value = title
            _allowedExtensions.value = allowedExtensions
            _mode.value = FilePickerMode.OPEN_FILE
            onResult = { result ->
                continuation.resume(result)
            }
            loadInitialDirectory()
        }
    }

    override suspend fun pickFiles(title: String, allowedExtensions: List<String>): List<String> {
        return suspendCancellableCoroutine { continuation ->
            _title.value = title
            _allowedExtensions.value = allowedExtensions
            _mode.value = FilePickerMode.OPEN_FILES
            onMultiResult = { result ->
                continuation.resume(result)
            }
            loadInitialDirectory()
        }
    }

    override suspend fun pickSaveLocation(title: String, defaultName: String, allowedExtensions: List<String>): String? {
        return suspendCancellableCoroutine { continuation ->
            _title.value = title
            _defaultName.value = defaultName
            _allowedExtensions.value = allowedExtensions
            _mode.value = FilePickerMode.SAVE_FILE
            onResult = { result ->
                continuation.resume(result)
            }
            loadInitialDirectory()
        }
    }

    override suspend fun pickDirectory(title: String): String? {
        return suspendCancellableCoroutine { continuation ->
            _title.value = title
            _mode.value = FilePickerMode.PICK_DIRECTORY
            onResult = { result ->
                continuation.resume(result)
            }
            loadInitialDirectory()
        }
    }

    fun setMode(mode: FilePickerMode) {
        _mode.value = mode
    }

    fun setAllowedExtensions(extensions: List<String>) {
        _allowedExtensions.value = extensions
    }

    override suspend fun saveFile(filePath: String, content: String): Boolean {
        return false
    }

    override suspend fun readFile(filePath: String, chunkSize: Int): ByteArray? {
        // 平台特定实现
        return null
    }

    override suspend fun writeFile(filePath: String, content: ByteArray, chunkSize: Int): Boolean {
        // 平台特定实现
        return false
    }

    protected open fun loadInitialDirectory() {
        // 平台特定实现
    }

    protected open fun navigateToDirectory(path: String) {
        // 平台特定实现
    }

    protected open fun navigateUp() {
        // 平台特定实现
    }

    protected open fun selectFile(item: FileItem) {
        // 平台特定实现
    }

    protected open fun confirmSelection() {
        // 平台特定实现
    }

    protected open fun cancelSelection() {
        onResult?.invoke(null)
        onMultiResult?.invoke(emptyList())
    }
} 