package com.ansh.tai.exception;

import java.util.UUID;

public class CourseNotFoundException extends RuntimeException{
    private final UUID id;

    public CourseNotFoundException(UUID id) {
        super(String.format("Course with ID '%s' does not exist.", id));
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
