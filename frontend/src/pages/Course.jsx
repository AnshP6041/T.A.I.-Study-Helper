import React from 'react';
import { useCourses } from "@/providers/course-provider";
import { useParams } from "react-router"
import NotFound from "./NotFound.jsx"
import Loading from "./Loading.jsx"

const Course = () => {
    const { courses, loading } = useCourses();
    const { courseId } = useParams();

    console.log("Course rendered");

    if (loading) {
        return <Loading />;
    }

    const course = courses.find((c) => c.id === courseId);

    console.log("Found course:", course);

    if (!course) {
        return <NotFound />;
    }

    return (
        <div>
            <h1>Course {course.title}</h1>
            <p>ID: {course.id}</p>
        </div>
    );
};

export default Course;