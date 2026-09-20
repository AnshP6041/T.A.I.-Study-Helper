import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import {RouterProvider} from "react-router";
import {router} from "./router.jsx";
import {AuthContextProvider} from "./context/AuthContext.jsx"
import {CourseProvider} from "@/providers/course-provider"

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <CourseProvider>
        <AuthContextProvider>
            <RouterProvider router={router}/>
        </AuthContextProvider>
        </CourseProvider>
    </StrictMode>,
)
