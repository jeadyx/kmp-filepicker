package io.github.jeadyx

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.Node
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.Promise

class JsFilePicker : ComposeFilePicker() {
    override val platform: Platform = Platform.JS
    private var fileInput: HTMLInputElement? = null

    init {
        setupFileInput()
    }

    private fun setupFileInput() {
        fileInput = document.createElement("input") as HTMLInputElement
        fileInput?.style?.display = "none"
        document.body?.appendChild(fileInput as Node)
    }

    override suspend fun pickFile(title: String, allowedExtensions: List<String>): String? {
        return suspendCancellableCoroutine { continuation ->
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = allowedExtensions.joinToString(",") { ".$it" }
            
            input.onchange = { event: Event ->
                val target = event.target as HTMLInputElement
                val file = target.files?.item(0)
                if (file != null) {
                    continuation.resume(file.name)
                } else {
                    continuation.resume(null)
                }
            }
            
            input.click()
        }
    }

    override suspend fun pickFiles(title: String, allowedExtensions: List<String>): List<String> {
        return suspendCancellableCoroutine { continuation ->
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.multiple = true
            input.accept = allowedExtensions.joinToString(",") { ".$it" }
            
            input.onchange = { event: Event ->
                val target = event.target as HTMLInputElement
                val files = target.files
                val fileNames = mutableListOf<String>()
                for (i in 0 until (files?.length ?: 0)) {
                    files?.item(i)?.let { fileNames.add(it.name) }
                }
                continuation.resume(fileNames)
            }
            
            input.click()
        }
    }

    override suspend fun pickSaveLocation(title: String, defaultName: String, allowedExtensions: List<String>): String? {
        return try {
            val promise = js("window.showSaveFilePicker()") as Promise<dynamic>
            promise.await().unsafeCast<File>().name
        } catch (e: Throwable){
            println(e)
            null
        }
    }

    override suspend fun pickDirectory(title: String): String? {
        return try {
            val promise = js("window.showDirectoryPicker()") as Promise<dynamic>
            promise.await().unsafeCast<File>().name
        } catch (e: Throwable){
            println(e)
            null
        }
    }

    override suspend fun saveFile(filePath: String, content: String): Boolean {
        return try {
            val blob = Blob(arrayOf(content))
            val url = URL.createObjectURL(blob)
            val a = document.createElement("a") as HTMLAnchorElement
            a.href = url
            a.download = filePath
            a.click()
            URL.revokeObjectURL(url)
            true
        } catch (e: Exception) {
            false
        }
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
        try {
            val uint8Array = js("new Uint8Array(content)") as Uint8Array
            val blob = Blob(arrayOf(uint8Array), BlobPropertyBag(type = "application/octet-stream"))
            val url = URL.createObjectURL(blob)
            val a = document.createElement("a") as HTMLAnchorElement
            a.href = url
            a.download = filePath
            document.body?.appendChild(a)
            a.click()

            document.body?.removeChild(a)
            URL.revokeObjectURL(url)
            return true
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
    }

    @Composable
    override fun FilePickerContent() {
        super.FilePickerContent()
    }
    private fun arrayBufferToByteArray(arrayBuffer: ArrayBuffer): ByteArray {
        val byteArray = ByteArray(arrayBuffer.byteLength)
        val byteArrayView = Int8Array(arrayBuffer)
        for (i in 0..<byteArrayView.length) {
            byteArray[i] = byteArrayView[i]
        }
        return byteArray
    }

}