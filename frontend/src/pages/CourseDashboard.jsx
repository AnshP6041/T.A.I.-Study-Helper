import React from 'react';
import {useNavigate} from "react-router";
import {UserAuth} from "@/context/AuthContext.jsx";
import CourseDash from "@/components/course-management"
import { LogOut } from "lucide-react";
import {CourseProvider} from "@/providers/course-provider"

const CourseDashboard = () => {
    const { session, logout } = UserAuth();
    const navigate = useNavigate();

    const handleSignOut = async (e) => {
        e.preventDefault();

        try {
            await logout();
            navigate("/");
        } catch (err) {
            console.error("An unexpected error occurred."); // Catch unexpected errors
        }
    };
    return (
            <div className="bg-blue-50 min-h-screen">
                <div>
                    <p onClick={handleSignOut} className="hover:cursor-pointer hover:opacity-50 inline-block px-4 py-3 mt-4">
                        <LogOut color="#fb2c36"/>
                    </p>
                </div>
                <CourseDash/>
            </div>
    );
};

export default CourseDashboard;