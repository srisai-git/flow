package com.simats.focusflow.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL



object ProfileImageManager {

    private const val AVATAR_FILE_PREFIX = "user_avatar_"
    private const val AVATAR_FILE_EXTENSION = ".jpg"

    

    fun getAvatarFile(context: Context, userId: Int): File {
        return File(context.filesDir, "${AVATAR_FILE_PREFIX}${userId}${AVATAR_FILE_EXTENSION}")
    }

    

    fun getAvatarPathIfExists(context: Context, userId: Int): String? {
        val file = getAvatarFile(context, userId)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    

    suspend fun saveAvatarToInternalStorage(
        context: Context,
        uri: Uri,
        userId: Int
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val targetFile = getAvatarFile(context, userId)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                val buffer = inputStream.readBytes()
                BitmapFactory.decodeByteArray(buffer, 0, buffer.size, options)

                var inSampleSize = 1
                val maxDim = 1200
                if (options.outHeight > maxDim || options.outWidth > maxDim) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                        inSampleSize *= 2
                    }
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }

                val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size, decodeOptions)
                if (bitmap != null) {
                    FileOutputStream(targetFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        out.flush()
                    }
                    targetFile.absolutePath
                } else {
                    targetFile.outputStream().use { out ->
                        out.write(buffer)
                        out.flush()
                    }
                    targetFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    

    suspend fun saveBytesToInternalStorage(
        context: Context,
        bytes: ByteArray,
        userId: Int
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val targetFile = getAvatarFile(context, userId)
            targetFile.outputStream().use { out ->
                out.write(bytes)
                out.flush()
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    

    suspend fun getBase64FromInternalAvatar(
        context: Context,
        userId: Int
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = getAvatarFile(context, userId)
            if (!file.exists() || file.length() == 0L) return@withContext null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            var inSampleSize = 1
            val maxDim = 600
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    

    suspend fun cacheRemoteAvatarToInternalStorage(
        context: Context,
        avatarSource: String,
        userId: Int
    ): String? = withContext(Dispatchers.IO) {
        if (avatarSource.isBlank() || avatarSource.startsWith("avatar:")) return@withContext null

        return@withContext try {
            if (avatarSource.startsWith("data:image")) {
                val base64Data = avatarSource.substringAfter("base64,")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                saveBytesToInternalStorage(context, decodedBytes, userId)
            } else if (avatarSource.startsWith("http://") || avatarSource.startsWith("https://")) {
                val url = URL(avatarSource)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    requestMethod = "GET"
                    doInput = true
                }
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val bytes = connection.inputStream.use { it.readBytes() }
                    saveBytesToInternalStorage(context, bytes, userId)
                } else {
                    null
                }
            } else if (avatarSource.startsWith("/") && File(avatarSource).exists()) {
                avatarSource
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    

    fun isImageAvatar(avatarStr: String?): Boolean {
        if (avatarStr.isNullOrBlank()) return false
        val trimmed = avatarStr.trim()
        if (trimmed.startsWith("avatar:")) return false

        return trimmed.startsWith("/") ||
                trimmed.startsWith("file:") ||
                trimmed.startsWith("content:") ||
                trimmed.startsWith("http:") ||
                trimmed.startsWith("https:") ||
                trimmed.startsWith("data:image") ||
                File(trimmed).exists()
    }

    

    fun resolveImageModel(context: Context, avatarStr: String?, userId: Int = 1): Any? {
        if (avatarStr.isNullOrBlank()) {
            val localFile = getAvatarFile(context, userId)
            return if (localFile.exists() && localFile.length() > 0) localFile else null
        }

        val trimmed = avatarStr.trim()
        if (trimmed.startsWith("avatar:")) return null

        if (trimmed.startsWith("/")) {
            val file = File(trimmed)
            return if (file.exists()) file else {
                val fallbackFile = getAvatarFile(context, userId)
                if (fallbackFile.exists() && fallbackFile.length() > 0) fallbackFile else file
            }
        }

        if (trimmed.startsWith("file://")) {
            val path = trimmed.removePrefix("file://")
            val file = File(path)
            return if (file.exists()) file else file
        }

        return trimmed
    }
}

