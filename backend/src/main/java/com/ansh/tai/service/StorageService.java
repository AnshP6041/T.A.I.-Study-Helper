package com.ansh.tai.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void upload(MultipartFile doc, String s3Key);

    void delete(String s3Key);

    String generatePresignedUrl(String s3Key);
}
