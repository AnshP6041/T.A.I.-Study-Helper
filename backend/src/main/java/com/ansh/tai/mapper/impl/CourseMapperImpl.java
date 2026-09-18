package com.ansh.tai.mapper.impl;

import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.dto.CourseDto;
import com.ansh.tai.domain.dto.CreateCourseRequestDto;
import com.ansh.tai.domain.entity.Course;
import com.ansh.tai.mapper.CourseMapper;
import org.springframework.stereotype.Component;

@Component
public class CourseMapperImpl implements CourseMapper {
    @Override
    public CreateCourseRequest fromDto(CreateCourseRequestDto dto) {
        return new CreateCourseRequest(dto.title(), dto.color());
    }

    @Override
    public CourseDto toDto(Course course) {
        return new CourseDto(course.getId(), course.getTitle(), course.getColor());
    }
}
