package com.naf.erp.training.document

import com.azure.storage.blob.BlobContainerClient
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class AzureStorage(
    private val blobContainerClient: BlobContainerClient
) : DocumentStorage {

    override fun save(
        fileName: String,
        bytes: ByteArray
    ): String {
        val blobClient = blobContainerClient.getBlobClient(fileName)
        blobClient.upload(ByteArrayInputStream(bytes), bytes.size.toLong(), true)
        
        return blobClient.blobUrl
    }
}
