package com.ansh.tai.service.impl;

import com.ansh.tai.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3StorageImpl implements StorageService {
    @Override
    public String upload(MultipartFile doc, String s3key) {
        return "";
    }

    @Override
    public void delete(String s3key) {

    }
}
