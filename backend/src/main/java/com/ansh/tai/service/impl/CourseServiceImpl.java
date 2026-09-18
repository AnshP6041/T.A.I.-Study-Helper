package com.ansh.tai.service.impl;

import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.entity.Course;
import com.ansh.tai.repository.CourseRepository;
import com.ansh.tai.service.CourseService;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<Course> listCourses() {
        return repo.findAll(Sort.by(Direction.ASC, "title"));
    }
}
