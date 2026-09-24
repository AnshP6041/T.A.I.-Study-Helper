package com.ansh.tai.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateDocumentRequest(
        UUID courseId,
        String fileName,
        String fileType,
        MultipartFile file
) {
}
