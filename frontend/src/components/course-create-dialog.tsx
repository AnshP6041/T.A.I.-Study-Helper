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
import { SketchPicker } from 'react-color';


const CreateCourseDialogue = ({
  isOpen,
  close,
}) => {
  const [title, setTitle] = useState("");
  const [color, setColor] = useState("#ffffff");
  const [colorPickerOpen, setColorPickerOpen] = useState(false);

  const [errorMessage, setErrorMessage] = useState<string | undefined>();

  const { createCourse } = useCourses();

  useEffect(() => {
    // Reset state
    setTitle("");
    setColor("#ffffff")
  }, [isOpen]);

  const handleCreateCourse = async () => {
    setErrorMessage(undefined);

    try {
      const createCourseRequest = {
        title: title,
        color: color
      };
      await createCourse(createCourseRequest);
      toast.success("Course has been created!");
      close();
    } catch (err) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      } else if (typeof err === "string") {
        setErrorMessage(err);
      } else {
        setErrorMessage(
          "Create course call failed, is the backend running?",
        );
      }
    }
  };

  const isInputValid = () => title !== undefined && title.length > 1;

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && close()}>
      <DialogContent className="sm:max-w-[425px] dark text-white">
        <DialogHeader>
          <DialogTitle>Create a New Course</DialogTitle>
        </DialogHeader>

        {errorMessage && (
          <Alert variant="destructive" className="bg-black">
            <AlertCircleIcon />
            <AlertTitle>Unable to create course.</AlertTitle>
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
                  <SketchPicker
                    color={color}
                    onChangeComplete={(col) => {setColor(col.hex)}}
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
            className="bg-sky-500 text-white hover:bg-sky-600"
            disabled={!isInputValid()}
            onClick={handleCreateCourse}
          >
            Create Course
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default CreateCourseDialogue;
