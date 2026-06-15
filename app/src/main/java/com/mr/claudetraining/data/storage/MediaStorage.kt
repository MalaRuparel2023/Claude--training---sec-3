package com.mr.claudetraining.data.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Uploads yoga media (thumbnail images, intro videos) to Firebase Storage and returns the
 * public download URL — the value that gets persisted on a [YogaProgram] in Realtime Database.
 */
class MediaStorage(
    storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val root = storage.reference

    suspend fun uploadImage(programId: String, image: Uri): String =
        upload("yoga/$programId/image", image)

    suspend fun uploadVideo(programId: String, video: Uri): String =
        upload("yoga/$programId/video", video)

    private suspend fun upload(path: String, uri: Uri): String {

        val ref = root.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}