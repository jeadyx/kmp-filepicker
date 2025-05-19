# JFilePicker

JFilePicker 是一个基于 Kotlin Multiplatform 的跨平台文件选择器库，支持 Android、Desktop 和 Web 平台。

*本项目由 `Claude` AI提供技术支持*

## 功能特性

- 支持选择单个文件
- 支持选择多个文件
- 支持选择保存文件的位置
- 支持选择目录
- 支持文件类型过滤
- 跨平台支持（Android、Desktop、Web）

## 安装

### KMP项目下改build.gradle.kts

```kotlin
sourceSets {
    val commonMain by getting {
        dependencies {
            implementation("io.github.jeadyx:kmp-filepicker:1.0.1")
        }
    }
}
```

## 使用示例

```kotlin
// 创建文件选择器实例
val filePicker = FilePickerFactory.create()

// 选择单个文件, 得到已选文件的路径/名称/uri路径
val file = filePicker.pickFile(
    title = "选择图片",
    allowedExtensions = listOf("jpg", "png", "gif")
)

// 选择多个文件, 得到已选文件的路径/名称列表/uri路径
val files = filePicker.pickFiles(
    title = "选择多个文件",
    allowedExtensions = listOf("txt", "pdf")
)

// 选择保存位置, 类似pickFile
val saveLocation = filePicker.pickSaveLocation(
    title = "保存文件",
    defaultName = "document.pdf",
    allowedExtensions = listOf("pdf")
)

// 选择目录, 得到已选目录的路径/uri路径
val directory = filePicker.pickDirectory(title = "选择目录")

// 写文件内容，写内容到指定文件/通过构建uri写入/下载文件
val writeToFileResult = filePicker.writeFile(
    if(filePicker.platform==Platform.ANDROID)
        "content://io.github.jeadyx.filepicker.example.fileprovider/public_docs/test.txt"
    else "test.txt",
    "dige666".encodeToByteArray(),
    1024
)


// 读文件内容，读取指定文件/选择一个文件读取/通过构建uri读取
val fileBytes = filePicker.readFile(
    if(filePicker.platform==Platform.ANDROID)
        "content://io.github.jeadyx.filepicker.example.fileprovider/public_docs/test.txt"
    else "test.txt",
)
```

## 平台特定实现

- Android: 使用系统原生的文件选择器, 通常使用uri路径读写文件
- Desktop: 使用 Swing/AWT 文件选择器，直接路径读写文件
- Web: 使用 HTML5 文件选择器，读取选中的文件，下载文件到本地，路径由浏览器决定

## 许可证

MIT License 