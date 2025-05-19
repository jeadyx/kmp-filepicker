package io.github.jeadyx


/**
 * 文件选择器接口
 */
interface FilePicker {
    /**
     * 当前平台
     */
    val platform: Platform

    /**
     * 选择单个文件
     * @param title 选择器标题
     * @param allowedExtensions 允许的文件扩展名列表，为空表示允许所有类型
     * @return 选择的文件路径，如果用户取消则返回null
     */
    suspend fun pickFile(title: String = "选择文件", allowedExtensions: List<String> = emptyList()): String?

    /**
     * 选择多个文件
     * @param title 选择器标题
     * @param allowedExtensions 允许的文件扩展名列表，为空表示允许所有类型
     * @return 选择的文件路径列表，如果用户取消则返回空列表
     */
    suspend fun pickFiles(title: String = "选择文件", allowedExtensions: List<String> = emptyList()): List<String>

    /**
     * 选择保存文件的位置
     * @param title 选择器标题
     * @param defaultName 默认文件名
     * @param allowedExtensions 允许的文件扩展名列表，为空表示允许所有类型
     * @return 选择的保存路径，如果用户取消则返回null
     */
    suspend fun pickSaveLocation(title: String = "保存文件", defaultName: String = "", allowedExtensions: List<String> = emptyList()): String?

    /**
     * 选择目录
     * @param title 选择器标题
     * @return 选择的目录路径，如果用户取消则返回null
     */
    suspend fun pickDirectory(title: String = "选择目录"): String?

    /**
     * 保存文件内容
     * @param filePath 文件路径
     * @param content 要保存的内容
     * @return 是否保存成功
     */
    suspend fun saveFile(filePath: String = "", content: String = ""): Boolean

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @param chunkSize 每次读取的块大小（字节），默认为8MB
     * @return 文件内容的字节数组，如果读取失败则返回null
     */
    suspend fun readFile(filePath: String = "", chunkSize: Int = 8 * 1024 * 1024): ByteArray?

    /**
     * 写入文件内容
     * @param filePath 文件路径
     * @param content 要写入的内容
     * @param chunkSize 每次写入的块大小（字节），默认为8MB
     * @return 是否写入成功
     */
    suspend fun writeFile(filePath: String = "", content: ByteArray = byteArrayOf(), chunkSize: Int = 8 * 1024 * 1024): Boolean
}