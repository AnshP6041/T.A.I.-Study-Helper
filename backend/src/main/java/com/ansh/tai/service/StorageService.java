package com.ansh.tai.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void upload(MultipartFile doc, String s3Key);

    void delete(String prefix);

    String generatePresignedUrl(String s3Key);

    byte[] download(String s3Key);

    void upload(byte[] bytes, String contentType, String s3Key);
}
