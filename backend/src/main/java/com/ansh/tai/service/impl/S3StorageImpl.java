package com.ansh.tai.service.impl;

import com.ansh.tai.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageImpl implements StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3StorageImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void upload(MultipartFile doc, String s3key) {
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3key)
                    .contentType(doc.getContentType())
                    .build();

            s3Client.putObject(
                    req,
                    RequestBody.fromInputStream(
                            doc.getInputStream(),
                            doc.getSize()
                    )
            );
        } catch (Exception err) {
            throw new RuntimeException("Failed to upload document to s3", err);
        }
    }

    @Override
    public void delete(String s3key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3key)
                .build();

        s3Client.deleteObject(req);
    }
}
