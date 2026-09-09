import React, { useEffect, useState } from "react";
import { UserAuth } from "../context/AuthContext";
import { Navigate } from "react-router";

const PrivateRoute = ({ children }) => {
    const { session } = UserAuth();

    if (session === undefined) {
        return <div>Loading...</div>;
    }

    return <div>{session ? <>{children}</> : <Navigate to="/register" />}</div>;
};

export default PrivateRoute;