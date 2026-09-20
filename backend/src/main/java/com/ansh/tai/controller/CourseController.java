package com.ansh.tai.controller;

import com.ansh.tai.domain.UpdateCourseRequest;
import com.ansh.tai.domain.dto.UpdateCourseRequestDto;
import jakarta.validation.Valid;
import com.ansh.tai.domain.CreateCourseRequest;
import com.ansh.tai.domain.dto.CourseDto;
import com.ansh.tai.domain.dto.CreateCourseRequestDto;
import com.ansh.tai.domain.entity.Course;
import com.ansh.tai.mapper.CourseMapper;
import com.ansh.tai.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<List<CourseDto>> listCourses() {
        List<Course> courses = serve.listCourses();
        List<CourseDto> courseDtos = courses.stream().map(map::toDto).toList();
        return ResponseEntity.ok(courseDtos);
    }

    @PutMapping(path = "/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody UpdateCourseRequestDto reqDto
    ) {
        UpdateCourseRequest req = map.fromDto(reqDto);
        Course course = serve.updateCourse(courseId, req);
        CourseDto dto = map.toDto(course);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping(path = "/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID courseId
    ){
        serve.deleteCourse(courseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
