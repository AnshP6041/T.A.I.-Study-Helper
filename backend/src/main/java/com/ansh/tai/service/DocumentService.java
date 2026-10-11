package com.ansh.tai.service;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.entity.Document;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    Document createDoc(CreateDocumentRequest req);

    Document reprocessDoc(UUID docId);

    List<Document> listDocs(UUID courseId);

    void deleteDoc(UUID docId);
}
