import React from 'react';
import {useNavigate} from "react-router";
import {UserAuth} from "../context/AuthContext.jsx";

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
        <div>
            <p
                onClick={handleSignOut}
                className="hover:cursor-pointer  border inline-block px-4 py-3 mt-4 "
            >
                Sign out
            </p>
        </div>
    );
};

export default CourseDashboard;