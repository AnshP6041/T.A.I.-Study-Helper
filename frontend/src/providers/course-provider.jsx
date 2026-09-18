import {createContext, useContext, useState, useCallback, useEffect, ReactNode} from "react";
import {createCourse as callCreateCourse, listCourses as callListCourses} from "@/lib/api.js";

const CourseContext = createContext(undefined);

export function DashboardProvider({children}) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [courses, setCourses] = useState([]);

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


  useEffect(() => {
      refresh();
  }, [refresh]);

  return (
      <CourseContext.Provider
        value={{
            loading, error, courses,
            refresh,
            createCourse
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