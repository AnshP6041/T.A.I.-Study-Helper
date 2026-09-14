package main.java.com.ansh.tai.service.impl;

import main.java.com.ansh.tai.domain.CreateCourseRequest;
import main.java.com.ansh.tai.domain.entity.Course;
import main.java.com.ansh.tai.repository.CourseRepository;
import main.java.com.ansh.tai.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repo;

    public CourseServiceImpl(CourseRepository repo) {
        this.repo = repo;
    }

    @Override
    public Course createCourse(CreateCourseRequest req) {
        Course course = new Course(
                null,
                req.title(),
                req.color()
        );

        return repo.save(course);
    }
}
