package io.github.jeadyx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class AndroidFilePicker(private val activity: ComponentActivity?) : ComposeFilePicker() {
    override val platform: Platform = Platform.ANDROID
    private var filePickerLauncher: ActivityResultLauncher<Intent>? = null
    private var directoryPickerLauncher: ActivityResultLauncher<Intent>? = null

    init {
        setupLaunchers()
    }

    private fun setupLaunchers() {
        activity?.let { activity ->
            filePickerLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                println("pick file res $result")
                if (result.resultCode == Activity.RESULT_OK) {
                    when (mode.value) {
                        FilePickerMode.OPEN_FILE -> handleSingleFileResult(result.data?.data!!)
                        FilePickerMode.OPEN_FILES -> handleMultipleFileResult(result.data)
                        FilePickerMode.SAVE_FILE -> handleSaveFileResult(result.data?.data!!)
                        else -> {}
                    }
                } else {
                    cancelSelection()
                }
            }

            directoryPickerLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                println("pick directory res $result")
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { handleDirectoryResult(it) }
                } else {
                    cancelSelection()
                }
            }
        }
    }

    private fun handleMultipleFileResult(resultData: Intent?) {
        val selectedFiles = mutableListOf<String>()
        
        resultData?.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                getRealPathFromUri(activity!!, uri)?.let { path ->
                    selectedFiles.add(path)
                }
            }
        } ?: resultData?.data?.let { uri ->
            getRealPathFromUri(activity!!, uri)?.let { path ->
                selectedFiles.add(path)
            }
        }

        onMultiResult?.invoke(selectedFiles)
    }

    private fun handleSingleFileResult(uri: Uri) {
        activity?.let { activity ->
            try {
                val path = getRealPathFromUri(activity, uri)
                onResult?.invoke(path)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult?.invoke(null)
            }
        }
    }

    private fun handleSaveFileResult(uri: Uri) {
        activity?.let { activity ->
            try {
                activity.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                onResult?.invoke(uri.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                onResult?.invoke(null)
            }
        }
    }

    private fun handleDirectoryResult(uri: Uri) {
        activity?.let { activity ->
            try {
                activity.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                onResult?.invoke(uri.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                onResult?.invoke(null)
            }
        }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> uri.path
                else -> createTempFile(context, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createTempFile(context: Context, uri: Uri): String? {
        return try {
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(uri))
            val tempFile = File.createTempFile("temp", ".$extension", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getMimeTypes(extensions: List<String>): Array<String> {
        return extensions.mapNotNull { ext ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.removePrefix("."))
        }.toTypedArray()
    }

    override fun loadInitialDirectory() {
        when (mode.value) {
            FilePickerMode.OPEN_FILE -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = if (allowedExtensions.value.isEmpty()) "*/*"
                           else getMimeTypes(allowedExtensions.value).firstOrNull() ?: "*/*"
                    if (allowedExtensions.value.isNotEmpty()) {
                        putExtra(Intent.EXTRA_MIME_TYPES, getMimeTypes(allowedExtensions.value))
                    }
                }
                filePickerLauncher?.launch(intent)
            }
            FilePickerMode.OPEN_FILES -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = if (allowedExtensions.value.isEmpty()) "*/*"
                           else getMimeTypes(allowedExtensions.value).firstOrNull() ?: "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    if (allowedExtensions.value.isNotEmpty()) {
                        putExtra(Intent.EXTRA_MIME_TYPES, getMimeTypes(allowedExtensions.value))
                    }
                }
                filePickerLauncher?.launch(intent)
            }
            FilePickerMode.SAVE_FILE -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = if (allowedExtensions.value.isEmpty()) "*/*"
                           else getMimeTypes(allowedExtensions.value).firstOrNull() ?: "*/*"
                    putExtra(Intent.EXTRA_TITLE, defaultName.value)
                }
                filePickerLauncher?.launch(intent)
            }
            FilePickerMode.PICK_DIRECTORY -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                }
                directoryPickerLauncher?.launch(intent)
            }
        }
    }

    fun saveToFile(uri: Uri, content: String): Boolean {
        println("save file by uri $uri")
        return try {
            activity?.contentResolver?.openOutputStream(uri)?.use { output ->
                println("write to output ${uri.path}, ${uri}")
                output.write(content.toByteArray())
                output.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            val filePath = uri.toString()
            val baseUri = filePath.substringBeforeLast('/')
            val fileName = filePath.substringAfterLast('/')
            val uri = Uri.parse(baseUri)
            saveToFile(uri, fileName, content)
        }
    }

    fun saveToFile(uri: Uri, fileName: String,  content: String): Boolean {
        println("save file by name and content")
        return try {
            val pickedDir = DocumentFile.fromTreeUri(activity as Context, uri)
            if (pickedDir != null && pickedDir.canWrite()) {
                val existingFile = pickedDir.findFile(fileName)
                val newFile = existingFile ?: pickedDir.createFile("text/plain", fileName)
                newFile?.uri?.let { fileUri ->
                    activity.contentResolver?.openOutputStream(fileUri, "w")?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                        outputStream.flush()
                    }
                    return true
                }
            }
            false
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    override suspend fun saveFile(filePath: String, content: String): Boolean {
        return try {
            val uri = Uri.parse(filePath)
            saveToFile(uri, content)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun readFile(filePath: String, chunkSize: Int): ByteArray {
        return try {
            val file = File(activity?.filesDir, filePath)
            withContext(Dispatchers.IO) {
                BufferedInputStream(FileInputStream(file)).use { bis ->
                    ByteArrayOutputStream().use { output ->
                        val buffer = ByteArray(chunkSize)
                        var bytesRead: Int
                        while (bis.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.toByteArray()
                    }
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
            try {
                val uri = Uri.parse(filePath)
                activity?.contentResolver?.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(chunkSize)
                    val output = ByteArrayOutputStream()
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.toByteArray()
                } ?: byteArrayOf()
            }catch (e: Exception){
                e.printStackTrace()
                byteArrayOf()
            }
        }
    }

    override suspend fun writeFile(filePath: String, content: ByteArray, chunkSize: Int): Boolean {
        return try {
            val file = File(activity?.filesDir, filePath)
            if(!file.exists()) withContext(Dispatchers.IO) {
                file.createNewFile()
            }
            withContext(Dispatchers.IO) {
                FileOutputStream(file).use { fos ->
                    fos.write(content)
                }
            }
            println("return write file true")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val uri = Uri.parse(filePath)
                saveToFile(uri, content.toString(Charsets.UTF_8))
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}