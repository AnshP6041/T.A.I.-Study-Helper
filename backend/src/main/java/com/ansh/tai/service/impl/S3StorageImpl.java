package com.ansh.tai.service.impl;

import com.ansh.tai.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
public class S3StorageImpl implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3StorageImpl(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public void upload(MultipartFile doc, String s3Key) {
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
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
    public void delete(String s3Key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        s3Client.deleteObject(req);
    }

    @Override
    public String generatePresignedUrl(String s3Key) {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3Key)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }
}
