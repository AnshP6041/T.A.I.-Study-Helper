import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { useEffect, useState } from "react";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { AlertCircleIcon, ChevronDownIcon } from "lucide-react";
import { toast } from "sonner";
import { useCourses } from "@/providers/course-provider";


const DeleteCourseDialogue = ({course, isOpen, close}) => {
    const [title, setTitle] = useState("");
    const [color, setColor] = useState("#ffffff");
    const [colorPickerOpen, setColorPickerOpen] = useState(false);

    const [errorMessage, setErrorMessage] = useState();

    const { deleteCourse } = useCourses();

    const handleDeleteCourse = async () => {
        setErrorMessage(undefined);

        try {
            await deleteCourse(course.id);
            toast.success(`Course "${course.title}" has been deleted!`);
            close();
        } catch (err) {
            if (err instanceof Error) {
                setErrorMessage(err.message);
            } else if (typeof err === "string") {
                setErrorMessage(err);
            } else {
                setErrorMessage(
                    "Delete course call failed, is the backend running?",
                );
            }
        }
    };

    const isInputValid = () => title !== undefined && title.length > 1;

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && close()}>
            <DialogContent className="sm:max-w-[425px] bg-black text-white">
                <DialogHeader>
                    <DialogTitle>Delete Course</DialogTitle>
                </DialogHeader>

                {errorMessage && (
                    <Alert variant="destructive" className="bg-black">
                        <AlertCircleIcon />
                        <AlertTitle>Unable to delete course.</AlertTitle>
                        <AlertDescription>
                            <p>{errorMessage}</p>
                        </AlertDescription>
                    </Alert>
                )}

                <div className="space-y-1">
                    <p>Are you sure you want to delete "{course.title}"?</p>
                    <p>This cannot be undone!</p>
                </div>
                <DialogFooter>
                    <Button onClick={close} variant="outline">
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="destructive"
                        className="bg-red-700"
                        onClick={handleDeleteCourse}
                    >
                        Delete
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default DeleteCourseDialogue;
