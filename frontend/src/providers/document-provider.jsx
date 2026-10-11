import {createContext, useContext, useState, useCallback, useEffect, ReactNode} from "react";
import {createDocument as callCreateDocument,
    listDocuments as callListDocuments,
    deleteDocument as callDeleteDocument,
    reprocessDocument as callReprocessDocument} from "@/lib/api.js";

const DocumentContext = createContext(undefined);

export function DocumentProvider({children, courseId}) {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [documents, setDocuments] = useState([]);
    const [selectedDocument, setSelectedDocument] = useState();

    const refresh = useCallback(
        async ({ silent = false } = {}) => {
            try {
                if (!silent) setLoading(true);
                setError(undefined);
                setDocuments(await callListDocuments(courseId));
            } catch (err) {
                setError(err ? err.message : "Failed to fetch documents");
            } finally {
                if (!silent) setLoading(false);
            }
        }, [courseId]
    );

    useEffect(() => {
        if (!documents.some((d) => d.status === "PROCESSING")) return;
        const timer = setTimeout(() => refresh({ silent: true }), 4000);
        return () => clearTimeout(timer);
    }, [documents, refresh]);

    const createDocument = useCallback(
        async (req) => {
            const created = await callCreateDocument(req);
            await refresh();
            return created;
        }, [refresh]
    );

    const reprocessDocument = useCallback(
        async (id) => {
            await callReprocessDocument(id);
            await refresh({ silent: true });
        }, [refresh]
    );

    const deleteDocument = useCallback(
        async (id) => {
            try{
                setError(undefined);
                await callDeleteDocument(id);
                await refresh();
                setSelectedDocument(undefined);
            } catch (err) {
                setError(err ? err.message : "Failed to delete document");
                throw err;
            }
        }, [refresh]
    );

    const selectDocument = useCallback(
        (id) => {
            if(id === undefined) {
                setSelectedDocument(undefined);
            }
            else {
                const doc = documents.find((d) => d.id === id);
                setSelectedDocument(doc);
            }
        }, [documents]
    );

    useEffect(() => {
        if (courseId) {
            refresh();
        }
    }, [courseId, refresh]);

    return (
        <DocumentContext.Provider
            value={{
                loading, error, documents,
                refresh, reprocessDocument,
                createDocument, deleteDocument,
                selectDocument, selectedDocument
            }}>
            {children}
        </DocumentContext.Provider>
    );
};

export function useDocuments() {
    const context = useContext(DocumentContext);
    if (context === undefined) {
        throw new Error("useDocuments must be used within a DocumentProvider");
    }
    return context;
}