package com.ansh.tai.domain.dto;

import com.ansh.tai.domain.entity.DocStatus;

import java.util.UUID;

public record DocumentDto(
        UUID id,
        UUID courseId,
        String fileName,
        String fileType,
        DocStatus status,
        String url
) {
}
