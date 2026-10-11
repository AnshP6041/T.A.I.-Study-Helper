package com.ansh.tai.service.impl;

import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.UpdateCourseRequest;
import com.ansh.tai.domain.entity.Course;
import com.ansh.tai.exception.CourseNotFoundException;
import com.ansh.tai.repository.CourseRepository;
import com.ansh.tai.repository.DocumentRepository;
import com.ansh.tai.service.CourseService;
import com.ansh.tai.service.StorageService;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repo;
    private final DocumentRepository docRepo;
    private final StorageService serve;


    public CourseServiceImpl(CourseRepository repo, DocumentRepository docRepo, StorageService serve) {
        this.repo = repo;
        this.docRepo = docRepo;
        this.serve = serve;
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

    @Override
    public Course updateCourse(UUID courseId, UpdateCourseRequest req) {
        Course course = repo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        course.setTitle(req.title());
        course.setColor(req.color());

        return repo.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        serve.delete("course/" + courseId + "/");
        docRepo.deleteAllByCourseId(courseId);
        repo.deleteById(courseId);
    }
}
