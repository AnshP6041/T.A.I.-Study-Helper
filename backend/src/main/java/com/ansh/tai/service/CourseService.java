package main.java.com.ansh.tai.service;

import main.java.com.ansh.tai.domain.CreateCourseRequest;
import main.java.com.ansh.tai.domain.entity.Course;

public interface CourseService {

    Course createCourse(CreateCourseRequest req);

}
