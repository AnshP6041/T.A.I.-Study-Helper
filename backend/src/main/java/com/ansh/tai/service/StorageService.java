package com.ansh.tai.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile doc, String s3key);

    void delete(String s3key);
}
