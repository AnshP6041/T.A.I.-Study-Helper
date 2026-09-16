package main.java.com.ansh.tai.domain.dto;


import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateCourseRequestDto(
        @NotBlank(message = "Title cannot be empty")
        @Length(max = 100, message = "Title cannot be too long")
        String title,
        @NotBlank(message = "Must select a color")
        String color
) {
}
