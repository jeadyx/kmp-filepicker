package io.github.jeadyx

import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosFilePicker : FilePicker {
    override val platform: Platform = Platform.IOS
    override suspend fun pickFile(title: String, allowedExtensions: List<String>): String? {
        return suspendCancellableCoroutine { continuation ->
            val documentPicker = UIDocumentPickerViewController(
                documentTypes = allowedExtensions.map { "public.$it" }.toTypedArray(),
                inMode = UIDocumentPickerMode.Import
            )
            
            documentPicker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAt: List<*>
                ) {
                    val url = didPickDocumentsAt.firstOrNull() as? NSURL
                    continuation.resume(url?.path)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    continuation.resume(null)
                }
            }

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                documentPicker,
                animated = true,
                completion = null
            )
        }
    }

    override suspend fun pickFiles(title: String, allowedExtensions: List<String>): List<String> {
        return suspendCancellableCoroutine { continuation ->
            val documentPicker = UIDocumentPickerViewController(
                documentTypes = allowedExtensions.map { "public.$it" }.toTypedArray(),
                inMode = UIDocumentPickerMode.Import
            )
            documentPicker.allowsMultipleSelection = true
            
            documentPicker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAt: List<*>
                ) {
                    val paths = didPickDocumentsAt.mapNotNull { it as? NSURL }.mapNotNull { it.path }
                    continuation.resume(paths)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    continuation.resume(emptyList())
                }
            }

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                documentPicker,
                animated = true,
                completion = null
            )
        }
    }

    override suspend fun pickSaveLocation(title: String, defaultName: String, allowedExtensions: List<String>): String? {
        return suspendCancellableCoroutine { continuation ->
            val documentPicker = UIDocumentPickerViewController(
                documentTypes = allowedExtensions.map { "public.$it" }.toTypedArray(),
                inMode = UIDocumentPickerMode.ExportToService
            )
            
            documentPicker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAt: List<*>
                ) {
                    val url = didPickDocumentsAt.firstOrNull() as? NSURL
                    continuation.resume(url?.path)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    continuation.resume(null)
                }
            }

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                documentPicker,
                animated = true,
                completion = null
            )
        }
    }

    override suspend fun pickDirectory(title: String): String? {
        return suspendCancellableCoroutine { continuation ->
            val documentPicker = UIDocumentPickerViewController(
                documentTypes = arrayOf("public.folder"),
                inMode = UIDocumentPickerMode.Open
            )
            
            documentPicker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAt: List<*>
                ) {
                    val url = didPickDocumentsAt.firstOrNull() as? NSURL
                    continuation.resume(url?.path)
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    continuation.resume(null)
                }
            }

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                documentPicker,
                animated = true,
                completion = null
            )
        }
    }

    override suspend fun saveFile(filePath: String, content: String): Boolean {
        return try {
            val data = content.encodeToByteArray().toNSData()
            val url = NSURL.fileURLWithPath(filePath)
            data.writeToURL(url, atomically = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun readFile(filePath: String, chunkSize: Int): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun writeFile(filePath: String, content: ByteArray, chunkSize: Int): Boolean {
        TODO("Not yet implemented")
    }
} 