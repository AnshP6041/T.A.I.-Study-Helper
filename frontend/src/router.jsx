import {createBrowserRouter} from "react-router";
import CourseDashboard from "./pages/CourseDashboard.jsx";
import Login from "./pages/Login.jsx";
import App from "./App.jsx";
import Register from "./pages/Register.jsx";
import Course from "./pages/Course.jsx"
import NotFound from "./pages/NotFound.jsx"

export const router = createBrowserRouter([
    {path: "/", element: <App/>},
    {path: "/register", element: <Register/>},
    {path: "/login", element: <Login/>},
    {path: "/courses", element: <CourseDashboard/>},
    {path: "/courses/:courseId", element: <Course/>},
    {path: "*", element: <NotFound/>},
])
