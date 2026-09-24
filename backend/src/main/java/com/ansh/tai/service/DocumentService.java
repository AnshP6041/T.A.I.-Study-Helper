package com.ansh.tai.service;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.entity.Document;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    Document createDoc(CreateDocumentRequest req);

    List<Document> listDocs();

    void deleteDoc(UUID docId);
}
