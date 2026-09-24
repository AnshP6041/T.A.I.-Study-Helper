package com.ansh.tai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateDocumentRequestDto(
        @NotBlank(message = "Must have a connected course")
        UUID courseId,
        @NotBlank(message = "Name cannot be empty")
        @Length(min = 3, max = 100, message = "Name must be > 3 and < 100")
        String fileName,
        @NotBlank(message = "Must have file type/extension")
        String fileType,
        @NotNull(message = "Must have a file attached")
        MultipartFile file
) {
}
