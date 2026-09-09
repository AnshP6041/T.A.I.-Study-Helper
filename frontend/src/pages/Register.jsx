import React, {useState} from 'react';
import {Link, useNavigate} from "react-router";
import {UserAuth} from "../context/AuthContext.jsx";
import tai from "../assets/tai.png";

const Register = () => {
    const [loading, setLoading] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);

    const {session, registerUser} = UserAuth();
    const navigate = useNavigate();

    const handleRegistration = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const result = await registerUser(email, password);

            if (result.success) {
                navigate("/courses")
            } else {
                setError(result.error.message);
            }
        } catch(err){
            setError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-blue-50 flex">
            <form onSubmit={handleRegistration} className={"max-w-md m-auto my-12 p-12 bg-blue-200 rounded-4xl max-h-fit"}>
                <div>
                    <h1 className="text-center text-6xl font-mono font-semibold">T.A.I. </h1>
                    <img src={tai} className=" object-cover scale-125"  alt="" />
                    <h1 className="text-center text-4xl font-mono font-semibold">Study Helper</h1>
                </div>
                <h1 className="mt-3 text-center text-xl font-mono font-semibold">Register</h1>
                <p className={"text-center mt-1"}>Already have an account? <Link to={"/login"} className={"text-blue-500 focus:opacity-50"}>Log In!</Link></p>
                <div className={"flex flex-col"}>
                    <input onChange={(e) => setEmail(e.target.value)} placeholder="Email" type="email" name="email"
                           id="email" className={"p-3 mt-6 bg-blue-100 placeholder-black rounded-full"}/>
                    <input onChange={(e) => setPassword(e.target.value)} placeholder="password" type="password"
                           name="password" id="password" className={"p-3 mt-3 bg-blue-100 placeholder-black rounded-full"}/>
                    <button type={"submit"} disabled={loading} className={"mt-6 w-fit m-auto p-2 bg-blue-300 rounded-xl hover:cursor-pointer focus:opacity-50"}>
                        Register
                    </button>
                    {error && <p className="text-red-600 text-center pt-4">{error}</p>}
                </div>
            </form>
        </div>
    );
};

export default Register;