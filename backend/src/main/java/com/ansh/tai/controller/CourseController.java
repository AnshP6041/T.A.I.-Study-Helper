package main.java.com.ansh.tai.controller;

import jakarta.validation.Valid;
import main.java.com.ansh.tai.domain.CreateCourseRequest;
import main.java.com.ansh.tai.domain.dto.CourseDto;
import main.java.com.ansh.tai.domain.dto.CreateCourseRequestDto;
import main.java.com.ansh.tai.domain.entity.Course;
import main.java.com.ansh.tai.mapper.CourseMapper;
import main.java.com.ansh.tai.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {

    private final CourseService serve;
    private final CourseMapper map;

    public CourseController(CourseService serve, CourseMapper map) {
        this.serve = serve;
        this.map = map;
    }

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(
            @Valid @RequestBody CreateCourseRequestDto reqDto
    ) {
        CreateCourseRequest req = map.fromDto(reqDto);
        Course course = serve.createCourse(req);
        CourseDto dto = map.toDto(course);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

}
