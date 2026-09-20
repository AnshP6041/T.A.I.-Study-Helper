import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useEffect, useState } from "react";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { AlertCircleIcon, ChevronDownIcon } from "lucide-react";
import { toast } from "sonner";
import { useCourses } from "@/providers/course-provider";
import { Popover, PopoverContent, PopoverTrigger } from "./ui/popover";
import { HexColorPicker } from "react-colorful";


const UpdateCourseDialogue = ({course, isOpen, close}) => {
    const [title, setTitle] = useState(course.title);
    const [color, setColor] = useState(course.color);
    const [colorPickerOpen, setColorPickerOpen] = useState(false);

    const [errorMessage, setErrorMessage] = useState();

    const { updateCourse } = useCourses();

    useEffect(() => {
        // Reset state
        setTitle(course.title);
        setColor(course.color)
    }, [isOpen]);

    const handleUpdateCourse = async () => {
        setErrorMessage(undefined);

        try {
            await updateCourse(course.id, {
                title: title,
                color: color
            })
            toast.success("Course has been updated!");
            close();
        } catch (err) {
            if (err instanceof Error) {
                setErrorMessage(err.message);
            } else if (typeof err === "string") {
                setErrorMessage(err);
            } else {
                setErrorMessage(
                    "Update course call failed, is the backend running?",
                );
            }
        }
    };

    const isInputValid = () => title !== undefined && title.length > 1;

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && close()}>
            <DialogContent className="sm:max-w-[425px] bg-black text-white">
                <DialogHeader>
                    <DialogTitle>Update Course</DialogTitle>
                </DialogHeader>

                {errorMessage && (
                    <Alert variant="destructive" className="bg-black">
                        <AlertCircleIcon />
                        <AlertTitle>Unable to update course.</AlertTitle>
                        <AlertDescription>
                            <p>{errorMessage}</p>
                        </AlertDescription>
                    </Alert>
                )}

                <div className="grid gap-4">
                    <div className="grid gap-3">
                        <Label htmlFor="name-1">Title</Label>
                        <Input
                            name="name"
                            value={title}
                            data-testid="input-course-name"
                            onChange={(e) => setTitle(e.target.value)}
                        />
                    </div>

                    <div className="flex gap-4">
                        <div>
                            <Label htmlFor="color">Color</Label>
                            <Popover open={colorPickerOpen} onOpenChange={setColorPickerOpen}>
                                <PopoverTrigger asChild>
                                    <Button
                                        data-testid="button-open-color-picker"
                                        variant="outline"
                                        id="color"
                                        className={`w-48 justify-between font-normal bg-[${color}]`}
                                    >
                                        {color}
                                        <ChevronDownIcon />
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent
                                    className="w-auto overflow-hidden p-0"
                                    align="start"
                                >
                                    <HexColorPicker
                                        color={color}
                                        onChange={(col) => {
                                            setColor(col)}}
                                    />
                                </PopoverContent>
                            </Popover>
                        </div>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={close} variant="outline">
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        className="bg-sky-600 text-white"
                        disabled={!isInputValid()}
                        onClick={handleUpdateCourse}
                    >
                        Update
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default UpdateCourseDialogue;
