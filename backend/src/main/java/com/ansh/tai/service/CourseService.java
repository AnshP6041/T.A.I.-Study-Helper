package com.ansh.tai.service;

import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.entity.Course;

import java.util.List;

public interface CourseService {

    Course createCourse(CreateCourseRequest req);

    List<Course> listCourses();

}
