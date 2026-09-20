import {createContext, useContext, useState, useCallback, useEffect, ReactNode} from "react";
import {createCourse as callCreateCourse,
    listCourses as callListCourses,
    updateCourse as callUpdateCourse,
    deleteCourse as callDeleteCourse} from "@/lib/api.js";

const CourseContext = createContext(undefined);

export function CourseProvider({children}) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [courses, setCourses] = useState([]);
  const [selectedCourse, setSelectedCourse] = useState();

  const refresh = useCallback(
      async () => {
          try {
              setLoading(true);
              setError(undefined);
              setCourses(await callListCourses());
          } catch (err) {
              setError(err ? err.message : "Failed to fetch course");
          } finally {
              setLoading(false);
          }
      }, []
  );

  const createCourse = useCallback(
      async (req) => {
          try{
              setError(undefined);
              const newList = await callCreateCourse(req);
              await refresh();
              return newList;
          } catch (err) {
              setError(err ? err.message : "Failed to create course");
              throw err;
          }
      }, [refresh]
  );

  const updateCourse = useCallback(
      async (id, req) => {
          try{
              setError(undefined);
              const newList = await callUpdateCourse(id, req);
              await refresh();
              if (selectedCourse && selectedCourse.id === id) {
                  setSelectedCourse(newList);
              }
              return newList;
          } catch (err) {
              setError(err ? err.message : "Failed to update course");
              throw err;
          }
      }, [refresh]
  );

  const deleteCourse = useCallback(
      async (id) => {
          try{
              setError(undefined);
              await callDeleteCourse(id);
              await refresh();
              setSelectedCourse(undefined);
          } catch (err) {
              setError(err ? err.message : "Failed to delete course");
              throw err;
          }
      }, [refresh]
  );

  const selectCourse = useCallback(
      (id) => {
          if(id === undefined) {
              setSelectedCourse(undefined);
          }
          else {
              const course = courses.find((c) => c.id === id);
              setSelectedCourse(course);
          }
      }, [courses]
  );

  useEffect(() => {
      refresh();
  }, [refresh]);

  return (
      <CourseContext.Provider
        value={{
            loading, error, courses,
            refresh,
            createCourse, updateCourse, deleteCourse,
            selectCourse, selectedCourse
        }}>
          {children}
      </CourseContext.Provider>
  );
};

export function useCourses() {
    const context = useContext(CourseContext);
    if (context === undefined) {
        throw new Error("useCourses must be used within a CourseProvider");
    }
    return context;
}