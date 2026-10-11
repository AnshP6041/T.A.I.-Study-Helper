package com.ansh.tai.domain.dto;

import java.util.UUID;

public record DuplicateErrorDto(String error, UUID existingDocumentId) {
}
