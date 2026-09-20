import { useCourses } from "@/providers/course-provider";
import { useState } from "react";
import { Button } from "./ui/button";
import { PlusCircle } from "lucide-react";
import CreateCourseDialogue from "./course-create-dialog";
import UpdateCourseDialogue from "./course-edit-dialog";
import DeleteCourseDialogue from "./course-delete-dialog"
import CourseCard from "./course-card";

function CourseDash() {
  const [isCreateCourseOpen, setIsCreateCourseOpen] = useState(false);
  const [isUpdateCourseOpen, setIsUpdateCourseOpen] = useState(false);
  const [isDeleteCourseOpen, setIsDeleteCourseOpen] = useState(false);

  const { courses, selectedCourse, selectCourse } = useCourses();

  return (
    <div>
      <div className="px-4 py-2 w-full flex flex-col h-full pb-12">
        {/* Header */}
        <div className={`flex justify-between w-full pt-4`}>
          <div className="mb-4">
            <h1 className="text-3xl font-bold mb-2 pr-2">Your Courses: {courses.length}</h1>
          </div>
          <div className="flex gap-x-2">
            <Button
                data-testid="button-create-course"
                onClick={() => setIsCreateCourseOpen(true)}
                className="bg-blue-300"
            >
              <PlusCircle /> Create Course
            </Button>
          </div>
        </div>

        <div className={`flex flex-row flex-wrap gap-2 `}>
          {courses.length != 0 ? (courses.map((course) => (
            <CourseCard
              course={course}
              key={course.id}
              editCourse={(course) => {
                selectCourse(course.id);
                setIsUpdateCourseOpen(true);
              }}
              deleteCourse={(course) => {
                selectCourse(course.id);
                setIsDeleteCourseOpen(true);
              }}
            />
          ))) : (
              <p className="mt-10 text-center w-full font-bold">No Courses Created Yet</p>
              )}
        </div>

        <div className={`py-4`}>

        </div>
      </div>
      <CreateCourseDialogue
        isOpen={isCreateCourseOpen}
        close={() => setIsCreateCourseOpen(false)}
      />
      {selectedCourse && (
          <UpdateCourseDialogue
            isOpen={isUpdateCourseOpen}
            close={() => {
              setIsUpdateCourseOpen(false);
              selectCourse(undefined);
            }}
            course={selectedCourse}
          />
      )}
      {selectedCourse && (
          <DeleteCourseDialogue
            isOpen={isDeleteCourseOpen}
            close={() => {
              setIsDeleteCourseOpen(false);
              selectCourse(undefined);
            }}
            course={selectedCourse}
          />
      )}
    </div>
  );
}

export default CourseDash;
