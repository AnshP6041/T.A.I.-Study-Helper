package com.ansh.tai.exception;

import com.ansh.tai.domain.entity.Document;

import java.util.UUID;

public class DuplicateDocumentException extends RuntimeException {
    private final UUID existingId;
    public DuplicateDocumentException(Document existing) {
        super("This file was already uploaded as \"" + existing.getFileName() + "\".");
        this.existingId = existing.getId();
    }
    public UUID getExistingId() { return existingId; }
}
