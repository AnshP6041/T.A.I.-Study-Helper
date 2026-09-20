package com.ansh.tai.service;

import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.UpdateCourseRequest;
import com.ansh.tai.domain.entity.Course;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    Course createCourse(CreateCourseRequest req);

    List<Course> listCourses();

    Course updateCourse(UUID courseId, UpdateCourseRequest req);

    void deleteCourse(UUID courseId);
}
