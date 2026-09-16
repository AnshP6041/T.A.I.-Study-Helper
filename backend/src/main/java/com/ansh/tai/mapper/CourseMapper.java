package main.java.com.ansh.tai.mapper;

import main.java.com.ansh.tai.domain.CreateCourseRequest;
import main.java.com.ansh.tai.domain.dto.CourseDto;
import main.java.com.ansh.tai.domain.dto.CreateCourseRequestDto;
import main.java.com.ansh.tai.domain.entity.Course;

public interface CourseMapper {

    CreateCourseRequest fromDto(CreateCourseRequestDto dto);
    CourseDto toDto(Course course);
}
