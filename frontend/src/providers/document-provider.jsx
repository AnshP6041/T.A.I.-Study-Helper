import {createContext, useContext, useState, useCallback, useEffect, ReactNode} from "react";
import {createDocument as callCreateDocument,
    listDocuments as callListDocuments,
    deleteDocument as callDeleteDocument} from "@/lib/api.js";

const DocumentContext = createContext(undefined);

export function DocumentProvider({children}) {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [documents, setDocuments] = useState([]);
    const [selectedDocument, setSelectedDocument] = useState();

    const refresh = useCallback(
        async () => {
            try {
                setLoading(true);
                setError(undefined);
                setDocuments(await callListDocuments());
            } catch (err) {
                setError(err ? err.message : "Failed to fetch documents");
            } finally {
                setLoading(false);
            }
        }, []
    );

    const createDocument = useCallback(
        async (req) => {
            try{
                setError(undefined);
                const newList = await callCreateDocument(req);
                await refresh();
                return newList;
            } catch (err) {
                setError(err ? err.message : "Failed to add document");
                throw err;
            }
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
        refresh();
    }, [refresh]);

    return (
        <DocumentContext.Provider
            value={{
                loading, error, documents,
                refresh,
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