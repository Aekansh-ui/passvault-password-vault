package com.example.password_vault.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.password_vault.data.db.dao.ProfileDao
import com.example.password_vault.data.db.entity.ProfileEntity
import com.example.password_vault.domain.model.ProfileData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: ProfileDao
) {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun observeProfile(): Flow<ProfileData> =
        profileDao.observe().map { entity ->
            entity?.let {
                ProfileData(it.displayName, it.username, it.imagePath)
            } ?: ProfileData("", "", null)
        }

    suspend fun saveProfile(displayName: String, username: String, imageUri: Uri?) {
        withContext(Dispatchers.IO) {
            val current = profileDao.get() ?: ProfileEntity()
            val imagePath = if (imageUri != null) {
                saveEncryptedImage(imageUri, current.imagePath)
            } else {
                current.imagePath
            }
            profileDao.upsert(
                current.copy(
                    displayName = displayName,
                    username = username,
                    imagePath = imagePath
                )
            )
        }
    }

    suspend fun ensureProfileExists() {
        withContext(Dispatchers.IO) {
            if (profileDao.get() == null) {
                profileDao.upsert(ProfileEntity())
            }
        }
    }

    private fun saveEncryptedImage(uri: Uri, oldPath: String?): String {
        // Delete old image if exists
        oldPath?.let { File(it).delete() }

        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return oldPath ?: ""

        // Downscale to max 512x512
        val scaled = scaleBitmap(bitmap, 512)
        val bytes = ByteArrayOutputStream().also { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }.toByteArray()

        val filename = "profile_${UUID.randomUUID()}.enc"
        val file = File(context.filesDir, filename)

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { it.write(bytes) }
        return file.absolutePath
    }

    fun loadDecryptedImageBytes(path: String): ByteArray? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileInput().use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleBitmap(src: Bitmap, maxSize: Int): Bitmap {
        val ratio = minOf(maxSize.toFloat() / src.width, maxSize.toFloat() / src.height)
        if (ratio >= 1f) return src
        val w = (src.width * ratio).toInt()
        val h = (src.height * ratio).toInt()
        return Bitmap.createScaledBitmap(src, w, h, true)
    }
}
