import { Label } from "./ui/label";


const CourseCard = ({ course }) => {

  return (
    <div className="flex flex-col items-center justify-center border w-32 rounded-xl">
        <div className="w-full aspect-square shrink-0 rounded-t-xl" style={{ backgroundColor: course.color }}></div>
        <div className="w-full h-16 p-2 bg-blue-100 flex items-center justify-center rounded-b-xl">
            <Label className="w-full min-w-0 truncate text-md text-center">
                {course.title}
            </Label>
        </div>
    </div>
  );
};

export default CourseCard;
