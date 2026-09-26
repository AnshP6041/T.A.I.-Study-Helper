package com.ansh.tai.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException {
    private final UUID id;

    public DocumentNotFoundException(UUID id) {
        super(String.format("Document with ID '%s' does not exist.", id));
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
