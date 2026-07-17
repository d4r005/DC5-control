package com.naf.erp.training.document

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class AwsStorage(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket}")
    private val bucket: String
) : DocumentStorage {

    override fun save(
        fileName: String,
        bytes: ByteArray
    ): String {
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(bytes))
        
        return "s3://$bucket/$fileName"
    }
}
