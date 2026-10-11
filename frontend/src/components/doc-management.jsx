import { PDFViewer } from '@embedpdf/react-pdf-viewer';
import { useDocuments } from "@/providers/document-provider";
import { useState, useRef, useEffect} from "react";
import { useParams } from "react-router";

export default function DocManagement() {
    const viewerRef = useRef(null);
    const uploadingRef = useRef(false);
    const pendingOpenRef = useRef(new Set());
    const { courseId } = useParams();

    const loadAllDocs = async () => {
        for (const doc of documents) {
            await addDocToTab(doc);
        }
    };
    const unloadAllDocs = async () => {
        for (const doc of [...inTabDocuments]) {
            await closeTab(doc.id);
        }
    };
    const loadAllDocsRef = useRef(loadAllDocs);
    loadAllDocsRef.current = loadAllDocs;
    const unloadAllDocsRef = useRef(unloadAllDocs);
    unloadAllDocsRef.current = unloadAllDocs;

    const {
        documents,
        selectDocument,
        selectedDocument,
        loading,
        error,
        deleteDocument,
        createDocument,
        reprocessDocument
    } = useDocuments();


    const [inTabDocuments, setInTabDocuments] = useState([]);
    const [showDocumentPicker, setShowDocumentPicker] = useState(false);
    const [err, setErr] = useState();

    useEffect(() => {
        if (!err) return;
        const timer = setTimeout(() => setErr(undefined), 5000);
        return () => clearTimeout(timer);
    }, [err]);

    const getDocumentManager = async () => {
        const registry = await viewerRef.current?.registry;
        if (!registry) return null;

        return registry
            .getPlugin("document-manager")
            .provides();
    };

    const openCustomFile = async () => {
        if (uploadingRef.current) {
            console.log("Already uploading");
            return;
        }
        const input = document.createElement("input");
        input.type = "file";
        input.accept = ".pdf,.pptx,application/pdf,application/vnd.openxmlformats-officedocument.presentationml.presentation";

        input.onchange = async (event) => {
            const file = event.target.files?.[0];
            if (!file) return;

            const baseName = (name) => name.trim().replace(/\.(pdf|pptx)$/i, "").trim().toLowerCase();

            const existing = documents.find((d) => baseName(d.fileName) === baseName(file.name));
            if (existing) {
                await addDocToTab(existing);
                setErr(`"${existing.fileName}" is already uploaded, so it was opened instead.`);
                return;
            }
            uploadingRef.current = true;
            try {
                const createdDoc = await createDocument({
                    courseId,
                    fileName: file.name,
                    fileType: file.type,
                    file: file
                });
                await addDocToTab(createdDoc);
            } catch (e) {
                if (e.existingDocumentId) {
                    const existing = documents.find((d) => d.id === e.existingDocumentId);
                    if (existing) await addDocToTab(existing);
                }
                setErr(e.message || "Failed to create document");
            } finally {
                uploadingRef.current = false;
            }
        };

        input.click();
    };

    const openCustomFileRef = useRef(openCustomFile);
    openCustomFileRef.current = openCustomFile;

    const setupCustomOpenFile = async (registry) => {
        if (!registry) return;

        const commands = registry.getPlugin('commands').provides();
        const ui = registry.getPlugin("ui").provides();
        const docMan = registry.getPlugin("document-manager").provides();

        commands.registerCommand({
            id: 'custom.openFile',
            label: 'Add Document to List',
            icon: 'fileImport',
            action: () => openCustomFileRef.current()
        });


        commands.registerCommand({
            id: 'custom.loadAll',
            label: 'Load All',
            action: () => loadAllDocsRef.current()
        });

        commands.registerCommand({
            id: 'custom.unloadAll',
            label: 'Unload All',
            action: () => unloadAllDocsRef.current()
        });


        commands.registerCommand({
            id: 'custom.loadRecommended',
            label: 'Load Recommended',
            action: () => alert('Will load recommended docs from AI once set up!')
        });

        const schema = ui.getSchema();
        const docMenu = schema.menus["document-menu"];
        if(docMenu?.items) {
            const openIdx = docMenu.items.findIndex(
                (item) => item.commandId === "document:open"
            );
            if(openIdx !== -1) {
                const items = JSON.parse(
                    JSON.stringify(docMenu.items)
                );

                items[openIdx].commandId = "custom.openFile";
                const take = (id) => {
                    const idx = items.findIndex((item) => item.id === id);
                    return idx === -1 ? null : items.splice(idx, 1)[0];
                };

                const fullscreen = take("document:fullscreen");
                const secondDividerIdx = items.findIndex((item) => item.id === "divider-11");
                if (fullscreen && secondDividerIdx !== -1) {
                    items.splice(secondDividerIdx, 0, fullscreen);

                    const divider = items[secondDividerIdx + 1];
                    divider.visibilityDependsOn?.itemIds.push("document:fullscreen");
                }

                items.push(
                    { type: "command", id: "custom.loadAll", commandId: "custom.loadAll" },
                    { type: "command", id: "custom.unloadAll", commandId: "custom.unloadAll" },
                    { type: "command", id: "custom.loadRecommended", commandId: "custom.loadRecommended" }
                );
                ui.mergeSchema({
                    menus: {
                        "document-menu": {
                            ...docMenu,
                            items
                        }
                    }
                });
            }
        }
    };

    const handleRetry = async (docId) => {
        try {
            await reprocessDocument(docId);
        } catch (e) {
            setErr(e.message);
        }
    };

    const addDocToTab = async (doc) => {
        const docMan = await getDocumentManager();
        if(!docMan) return;

        if (inTabDocuments.some((d) => d.id === doc.id)) {
            await docMan.setActiveDocument(doc.id);
            selectDocument(doc.id);
            setShowDocumentPicker(false);
            return;
        }
        if (!doc.previewUrl) {
            pendingOpenRef.current.add(doc.id);
            setErr("This presentation is still being converted. It will open once converted.");
            return;
        }

        await docMan.openDocumentUrl({
            url: doc.previewUrl,
            documentId: doc.id,
            autoActivate: true,
        });
        setInTabDocuments((current) => [...current, doc]);
        selectDocument(doc.id);
        setShowDocumentPicker(false);
    };

    useEffect(() => {
        for (const id of [...pendingOpenRef.current]) {
            const doc = documents.find((d) => d.id === id);

            if (!doc) {
                pendingOpenRef.current.delete(id);
            } else if (doc.previewUrl) {
                pendingOpenRef.current.delete(id);
                addDocToTab(doc);
            } else if (doc.status === "FAILED") {
                pendingOpenRef.current.delete(id);
                setErr(`Couldn't convert "${doc.fileName}". Try uploading it again.`);
            }
        }
    }, [documents]);

    const closeTab = async(docId) => {
        const docMan = await getDocumentManager();

        if(!docMan) return;

        const doc = inTabDocuments.find((d) => d.id === docId);
        if (!doc) return;
        await docMan.closeDocument(docId);

        setInTabDocuments((current) =>
            current.filter(
                (d) => d.id !== docId
            )
        );

        if(selectedDocument?.id === docId){
            const remaining = inTabDocuments.filter(
                (d) => d.id !== docId
            );

            const nextDoc = remaining.length > 0 ? remaining[remaining.length - 1]
                : null;

            if(nextDoc) {
                await docMan.setActiveDocument(nextDoc.id);
                selectDocument(nextDoc.id);
            } else {
                selectDocument(undefined);
            }

        }
    };

    const changeActiveTab = async(docId) => {
        const docMan = await getDocumentManager();
        if(!docMan) return;

        const doc = inTabDocuments.find((d) => d.id === docId);
        await docMan.setActiveDocument(docId);
        selectDocument(docId);
    };

    const handleDelete = async(docId) => {
        await deleteDocument(docId);

        if(inTabDocuments.some(
            (d) => d.id === docId
        ))
            await closeTab(docId);
    };

    return (
        <div className="relative h-full w-full flex flex-col">
            <div className="h-12 shrink-0 border-b flex items-stretch">
                <div className="flex-1 flex overflow-x-auto">
                    {inTabDocuments.map((doc) => (
                        <div
                            key={doc.id}
                            title={doc.fileName}
                            className={`flex items-center gap-2 px-4 border-r cursor-pointer shrink-0 ${selectedDocument?.id === doc.id ? "bg-gray-100" : "bg-white"}`}
                            onClick={() =>
                                changeActiveTab(doc.id)
                            }>
                            <span className="truncate max-w-48">
                                {doc.fileName}
                            </span>
                            <button
                                type="button"
                                onClick={(event) => {
                                    event.stopPropagation();
                                    closeTab(doc.id);
                                }}
                                className="text-gray-500 hover:text-black">
                                ×
                            </button>
                        </div>
                    ))}
                </div>
                <button
                    type="button"
                    onClick={() =>
                        setShowDocumentPicker(!showDocumentPicker)
                    }
                    className="w-12 shrink-0 border-l border-r hover:bg-gray-100">
                    {showDocumentPicker ? '-' : '+'}
                </button>
            </div>
            {err && (
                <div className="absolute top-14 left-1/2 -translate-x-1/2 z-[60] flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700 shadow">
                    <span>{err}</span>
                    <button
                        type="button"
                        onClick={() => setErr(undefined)}
                        className="text-red-400 hover:text-red-700">
                        ×
                    </button>
                </div>
            )}
            {showDocumentPicker && (
                <div className="absolute top-12 right-0 z-50 w-80 border rounded-lg bg-white shadow-lg">
                    <div className="p-3 font-semibold border-b">
                        Course Documents
                    </div>
                    {loading && (
                        <div className="p-4 text-sm">
                            Loading documents...
                        </div>
                    )}
                    {error && (
                        <div className="p-4 text-sm text-red-500">
                            {error}
                        </div>
                    )}
                    {!loading &&
                        !error &&
                        documents.length === 0 && (
                            <div className="p-4 text-sm text-gray-500">
                                No documents available.
                            </div>
                        )}
                    {!loading &&
                        !error &&
                        documents.map((doc) => {
                            const isInTab =
                                inTabDocuments.some(
                                    (d) =>
                                        d.id === doc.id
                                );
                            return (
                                <div
                                    key={doc.id}
                                    className="flex items-center border-b last:border-b-0">
                                    <button
                                        type="button"
                                        onClick={() =>
                                            addDocToTab(doc)
                                        }
                                        className="flex-1 text-left px-3 py-3 hover:bg-gray-100">
                                        <div>
                                            {doc.fileName}
                                        </div>
                                        <div className="flex flex-row gap-2">
                                        {doc.status === "PROCESSING" && <span className="text-xs text-gray-500">Processing…</span>}
                                        {doc.status === "FAILED" && <span className="text-xs text-red-500">Failed</span>}
                                        {doc.status === "READY" && <span className="text-xs text-blue-500">Ready for AI</span>}
                                        {doc.status === "DUPLICATE" && (<span className="text-xs text-amber-600">Duplicate of {documents.find((d) => d.id === doc.duplicateOfId)?.fileName ?? "another file"}</span>)}
                                        {isInTab && (
                                            <div className="text-xs text-gray-500">
                                                - In Tab
                                            </div>
                                        )}
                                        </div>
                                    </button>
                                    {(doc.status === "FAILED" || (!doc.previewUrl && doc.status !== "PROCESSING")) && (
                                        <button
                                            type="button"
                                            onClick={() => handleRetry(doc.id)}
                                            className="px-3 py-3 text-blue-600 hover:bg-blue-50">
                                            Retry
                                        </button>
                                    )}
                                    <button
                                        type="button"
                                        onClick={() =>
                                            handleDelete(
                                                doc.id
                                            )
                                        }
                                        className="px-3 py-3 text-red-500 hover:bg-red-50">
                                        Del
                                    </button>
                                </div>
                            );
                        })}
                </div>
            )}
            <div className="relative flex-1 min-h-0">
                <PDFViewer
                    ref={viewerRef}
                    onReady={setupCustomOpenFile}
                    config={{
                        tabBar: "never",
                        theme: {
                            preference: "light",
                        },
                        disabledCategories: ['panel-comment', 'document-close', 'document-protect']
                    }}

                    style={{
                        width: "100%",
                        height: "100%",
                    }}
                />
                {documents.length === 0 ?
                    (
                        <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-blue-50">
                            <h1 className="text-3xl font-semibold mb-10">There are no documents to select from</h1>
                            <button
                                type="button"
                                onClick={openCustomFile}
                                className="rounded-lg border px-4 py-2 hover:bg-gray-100"
                            >
                                Open Document
                            </button>
                        </div>
                    ) : inTabDocuments.length === 0 ?
                        (
                            <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-6 bg-blue-50">
                                <h1 className="text-2xl font-semibold">No documents open</h1>
                                <div className="flex gap-3">
                                    <button
                                        type="button"
                                        onClick={() => setShowDocumentPicker(true)}
                                        className="rounded-lg border px-4 py-2 hover:bg-gray-100"
                                    >
                                        Choose Document
                                    </button>
                                    <button
                                        type="button"
                                        onClick={loadAllDocs}
                                        className="rounded-lg border px-4 py-2 hover:bg-gray-100"
                                    >
                                        Load All
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => alert('Will load recommended docs from AI once set up!')}
                                        className="rounded-lg border px-4 py-2 hover:bg-gray-100"
                                    >
                                        Load Recommended
                                    </button>
                                    <button
                                        type="button"
                                        onClick={openCustomFile}
                                        className="rounded-lg border px-4 py-2 hover:bg-gray-100"
                                    >
                                        Upload New
                                    </button>
                                </div>
                            </div>
                        ) : null}
            </div>
        </div>
    );
}