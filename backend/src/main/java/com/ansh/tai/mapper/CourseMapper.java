package com.ansh.tai.mapper;

import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.UpdateCourseRequest;
import com.ansh.tai.domain.dto.CourseDto;
import com.ansh.tai.domain.dto.CreateCourseRequestDto;
import com.ansh.tai.domain.dto.UpdateCourseRequestDto;
import com.ansh.tai.domain.entity.Course;

public interface CourseMapper {

    CreateCourseRequest fromDto(CreateCourseRequestDto dto);
    UpdateCourseRequest fromDto(UpdateCourseRequestDto dto);
    CourseDto toDto(Course course);
}
