import React from 'react';
import { useCourses } from "@/providers/course-provider";
import { useParams } from "react-router"
import NotFound from "./NotFound.jsx"
import Loading from "./Loading.jsx"
import DocManager from "@/components/doc-management"
import { DocumentProvider } from "@/providers/document-provider"

const Course = () => {
    const { courses, loading } = useCourses();
    const { courseId } = useParams();

    if (loading) {
        return <Loading />;
    }

    const course = courses.find((c) => c.id === courseId);

    if (!course) {
        return <NotFound />;
    }

    return (
        <DocumentProvider courseId={courseId}>
            <div className="h-[100vh] w-full">
                <div className="h-full w-1/2">
                    <DocManager />
                </div>
            </div>
        </DocumentProvider>
    );
};

export default Course;