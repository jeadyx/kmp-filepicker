package io.github.jeadyx

import io.github.jeadyx.ComposeFilePicker
import io.github.jeadyx.FileItem
import io.github.jeadyx.FilePickerMode
import io.github.jeadyx.Platform
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume
import org.w3c.files.Blob
import org.w3c.files.File
import kotlin.js.Promise

class WasmFilePicker : ComposeFilePicker() {
    override val platform: Platform = Platform.WASM
    private var inputElement: HTMLInputElement? = null

    override fun loadInitialDirectory() {
        if (inputElement == null) {
            inputElement = document.createElement("input").unsafeCast<HTMLInputElement>().apply {
                type = "file"
                style.display = "none"
                document.body?.appendChild(this)
            }
        }
    }

    override fun navigateToDirectory(path: String) {
        // WASM 环境下不支持目录导航
    }

    override fun navigateUp() {
        // WASM 环境下不支持目录导航
    }

    override fun selectFile(item: FileItem) {
        // WASM 环境下，文件选择通过 input 元素处理
    }

    override fun confirmSelection() {
        val input = inputElement ?: return

        when (mode.value) {
            FilePickerMode.OPEN_FILE -> handleSingleFileSelection(input)
            FilePickerMode.OPEN_FILES -> handleMultipleFileSelection(input)
            FilePickerMode.SAVE_FILE -> { /* 由 @see saveFile 自己处理 */}
            FilePickerMode.PICK_DIRECTORY -> onResult?.invoke(null)
        }
    }

    private fun handleSingleFileSelection(input: HTMLInputElement) {
        input.apply {
            accept = allowedExtensions.value.joinToString(",") { ".$it" }
            multiple = false
            onchange = { _: Event ->
                val files = files
                onResult?.invoke(files?.get(0)?.name)
                cleanupInput()
            }
            click()
        }
    }

    private fun handleMultipleFileSelection(input: HTMLInputElement) {
        input.apply {
            accept = allowedExtensions.value.joinToString(",") { ".$it" }
            multiple = true
            onchange = { _: Event ->
                val files = files
                val fileNames = mutableListOf<String>()
                if (files != null) {
                    for (i in 0 until files.length) {
                        files[i]?.name?.let { fileNames.add(it) }
                    }
                }
                onMultiResult?.invoke(fileNames)
                cleanupInput()
            }
            click()
        }
    }
    override suspend fun saveFile(filePath: String, content: String): Boolean {
        return try {
            writeFile(filePath, content.encodeToByteArray())
//            downloadBinaryFile(byteArrayToUint8Array(content.encodeToByteArray()), filePath)
            true
        } catch (_: Throwable) {
            false
        }
    }

    override suspend fun pickFile(title: String, allowedExtensions: List<String>): String? {
        setAllowedExtensions(allowedExtensions)
        return suspendCancellableCoroutine { continuation ->try {
            onResult = { result ->
                continuation.resume(result)
            }
            setMode(FilePickerMode.OPEN_FILE)
            loadInitialDirectory()
            confirmSelection()
            } catch (e: Throwable) {
                e.printStackTrace()
                continuation.resume(null)
            }
            loadInitialDirectory()
        }
    }

    override suspend fun pickFiles(title: String, allowedExtensions: List<String>): List<String> {
        setAllowedExtensions(allowedExtensions)
        return suspendCancellableCoroutine { continuation -> try {
                onMultiResult = { result ->
                    continuation.resume(result)
                }
                setMode(FilePickerMode.OPEN_FILES)
                loadInitialDirectory()
                confirmSelection()
            } catch (e: Throwable) {
                e.printStackTrace()
                continuation.resume(emptyList())
            }
        }
    }

    override suspend fun pickDirectory(title: String): String? {
        return try {
            showDirectoryPickerJs().await<JsAny>().unsafeCast<File>().name
        } catch (e: Exception){
            null
        }
    }

    override suspend fun pickSaveLocation(
        title: String,
        defaultName: String,
        allowedExtensions: List<String>
    ): String? {
        return try {
            showSaveFilePickerJs().await<JsAny>().unsafeCast<File>().name
        } catch (e: Exception){
            null
        }
    }

    private fun cleanupInput() {
        inputElement?.value = ""
    }

    override suspend fun readFile(filePath: String, chunkSize: Int): ByteArray? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val input = document.createElement("input") as HTMLInputElement
                input.type = "file"
                input.style.display = "none"
                document.body?.appendChild(input)

                input.onchange = { event: Event ->
                    val target = event.target as HTMLInputElement
                    val file = target.files?.get(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = { e ->
                            val result = e.target?.unsafeCast<FileReader>()?.result as? ArrayBuffer
                            result?.let{
                                continuation.resume(arrayBufferToByteArray(result))
                            } ?: run {
                                continuation.resume(null)
                            }
                            document.body?.removeChild(input)
                        }
                        reader.onerror = {
                            continuation.resume(null)
                            document.body?.removeChild(input)
                        }
                        reader.readAsArrayBuffer(file)
                    } else {
                        continuation.resume(null)
                        document.body?.removeChild(input)
                    }
                }

                input.click()
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }
    override suspend fun writeFile(filePath: String, content: ByteArray, chunkSize: Int): Boolean {
        return try {
            val blob = Blob(wrapByteArrayWithJsArray(content).unsafeCast(), BlobPropertyBag("application/octet-stream"))
            val url = URL.createObjectURL(blob)
            document.createElement("a").unsafeCast<HTMLAnchorElement>().apply {
                href = url
                download = filePath
                click()
                URL.revokeObjectURL(url)
            }
//            downloadBinaryFile(byteArrayToUint8Array(content), filePath)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun arrayBufferToByteArray(arrayBuffer: ArrayBuffer): ByteArray {
        val byteArray = ByteArray(arrayBuffer.byteLength)
        val byteArrayView = Int8Array(arrayBuffer)
        for (i in 0..<byteArrayView.length) {
            byteArray[i] = byteArrayView[i]
        }
        return byteArray
    }

    private fun byteArrayToUint8Array(byteArray: ByteArray): Uint8Array {
         return Uint8Array(byteArray.size).apply {
            for (i in byteArray.indices) {
                this[i] = byteArray[i]
            }
        }
    }

    private fun wrapUint8ArrayWithJsArray(uint8Array: Uint8Array): JsArray<Uint8Array> {
        val retArray = JsArray<Uint8Array>()
        retArray[0] = uint8Array
        return retArray
    }

    private fun wrapByteArrayWithJsArray(byteArray: ByteArray): JsArray<Uint8Array> {
        return wrapUint8ArrayWithJsArray(byteArrayToUint8Array(byteArray))
    }
}

@JsFun("""
(data, filename) => {
    const blob = new Blob([data], {type: 'application/octet-stream'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
""")
external fun downloadBinaryFile(data: Uint8Array, filename: String)

@JsFun("() => window.showDirectoryPicker()")
external fun showDirectoryPickerJs(): Promise<JsAny>

@JsFun("() => window.showSaveFilePicker()")
external fun showSaveFilePickerJs(): Promise<JsAny>

// val mimeType = when {
//     filePath.endsWith(".pdf") -> "application/pdf"
//     filePath.endsWith(".png") -> "image/png"
//     filePath.endsWith(".jpg") -> "image/jpeg"
//     filePath.endsWith(".jpeg") -> "image/jpeg"
//     filePath.endsWith(".gif") -> "image/gif"
//     filePath.endsWith(".txt") -> "text/plain"
//     else -> "application/octet-stream"
// }
