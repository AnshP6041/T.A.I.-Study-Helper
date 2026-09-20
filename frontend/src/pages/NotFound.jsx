import React from 'react';
import {useNavigate} from "react-router";
import { Button } from "../components/ui/button.tsx"
import { ArrowLeft } from "lucide-react"

const NotFound = () => {
    const navigate = useNavigate();

    return (
        <div className="flex flex-col items-center justify-center min-h-screen gap-4 bg-blue-50">
            <p className="font-bold text-5xl mb-10">Page not found</p>
            <div className="flex flex-row gap-5">
                <Button onClick={() => navigate(-1)} variant="outline" className="bg-red-500"><ArrowLeft/></Button>
                <Button onClick={() => navigate("/courses")} variant="outline" className="bg-blue-300">Dashboard</Button>
            </div>
        </div>
    );
};

export default NotFound;