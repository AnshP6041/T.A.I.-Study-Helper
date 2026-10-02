package com.ansh.tai.mapper.impl;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.dto.CreateDocumentRequestDto;
import com.ansh.tai.domain.dto.DocumentDto;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.mapper.DocumentMapper;
import com.ansh.tai.service.StorageService;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapperImpl implements DocumentMapper {
    private final StorageService s3Serve;

    public DocumentMapperImpl(StorageService s3Serve) {
        this.s3Serve = s3Serve;
    }

    @Override
    public CreateDocumentRequest fromDto(CreateDocumentRequestDto dto) {
        return new CreateDocumentRequest(dto.courseId(), dto.fileName(), dto.fileType(), dto.file());
    }

    @Override
    public DocumentDto toDto(Document doc) {
        return new DocumentDto(doc.getId(), doc.getCourseId(), doc.getFileName(), doc.getFileType(), doc.getStatus(), s3Serve.generatePresignedUrl(doc.getS3Key()));
    }
}
