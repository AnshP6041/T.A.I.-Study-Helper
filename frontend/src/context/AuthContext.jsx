import {createContext, useContext, useEffect, useState} from "react";
import {supabase} from "../lib/supabase.js";

const AuthContext = createContext();

export const AuthContextProvider = ({children}) => {
    const [session, setSession] = useState(undefined);

    useEffect(() => {
        supabase.auth.getSession().then(({data: {session}}) => {
            setSession(session);
        });

        supabase.auth.onAuthStateChange((_event, session) => {
            setSession(session);
        });
    }, []);

    const registerUser = async (email, password) => {
        const {data, error} = await supabase.auth.signUp({
            email: email.toLowerCase(),
            password: password,
        });

        if (error) {
            console.error("Error signing up: ", error);
            return {success: false, error};
        }

        return {success: true, data};
    };

    const loginUser = async (email, password) => {
        try {
            const {data, error} = await supabase.auth.signInWithPassword({
                email: email.toLowerCase(),
                password: password,
            });
            if (error) {
                console.error("Sign-in error:", error.message);
                return {success: false, error: error.message};
            }

            console.log("Sign-in success:", data);
            return {success: true, data};
        } catch (error) {
            console.error("Unexpected error during sign-in:", error.message);
            return {
                success: false,
                error: "An unexpected error occurred. Please try again.",
            };
        }
    };

    async function logout() {
        const {error} = await supabase.auth.signOut();
        if (error) {
            console.error("Error signing out:", error);
        }
    }

    return (
        <AuthContext.Provider
            value={{registerUser, loginUser, session, logout}}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const UserAuth = () => {
    return useContext(AuthContext);
};