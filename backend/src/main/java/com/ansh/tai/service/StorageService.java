package com.ansh.tai.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void upload(MultipartFile doc, String s3key);

    void delete(String s3key);
}
