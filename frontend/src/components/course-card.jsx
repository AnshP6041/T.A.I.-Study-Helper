import { Edit, Trash } from "lucide-react"
import { Button } from "./ui/button"
import { useNavigate, Link } from "react-router";



const CourseCard = ({ course, editCourse, deleteCourse }) => {
    const navigate = useNavigate();

    return (
        <div className="overflow-hidden border w-32 rounded-xl">
            <div className="w-full aspect-square rounded-t-xl" style={{ backgroundColor: course.color }}></div>
            <div className="p-2 bg-blue-100 flex flex-col items-center gap-2 min-w-0 rounded-b-xl">
                <Link onClick={() => navigate(`/courses/${course.id}`)} className="w-full min-w-0 truncate text-md font-medium text-center hover:cursor-pointer focus:opacity-50">
                    {course.title}
                </Link>
                <div className="flex gap-2">
                    <Button
                        variant={"outline"}
                        className="hover:bg-blue-300"
                        data-testid="button-edit-course"
                        onClick={() => editCourse(course)}
                    >
                        <Edit />
                    </Button>
                    <Button
                        data-testid="button-delete-course"
                        className="hover:bg-red-500"
                        variant={"outline"}
                        onClick={() => deleteCourse(course)}
                    >
                        <Trash />
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default CourseCard;
