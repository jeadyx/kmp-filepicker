package io.github.jeadyx

/**
 * 文件项模型
 */
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val isSelected: Boolean = false,
    val size: Long = 0,
    val lastModified: Long = 0
)

/**
 * 文件选择器模式
 */
enum class FilePickerMode {
    OPEN_FILE,
    OPEN_FILES,
    SAVE_FILE,
    PICK_DIRECTORY
} 