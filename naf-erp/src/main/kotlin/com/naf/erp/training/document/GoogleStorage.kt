package com.naf.erp.training.document

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GoogleStorage(
    private val storage: Storage,
    @Value("\${google.cloud.bucket}")
    private val bucketName: String
) : DocumentStorage {

    override fun save(
        fileName: String,
        bytes: ByteArray
    ): String {
        val blobId = BlobId.of(bucketName, fileName)
        val blobInfo = BlobInfo.newBuilder(blobId).build()
        val blob = storage.create(blobInfo, bytes)
        
        return blob.mediaLink
    }
}
