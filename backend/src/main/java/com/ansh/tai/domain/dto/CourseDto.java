package com.ansh.tai.domain.dto;

import java.util.UUID;

public record CourseDto(
        UUID id,
        String title,
        String color
) {

}
