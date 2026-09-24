package com.ansh.tai.service.impl;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.entity.DocStatus;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.repository.DocumentRepository;
import com.ansh.tai.service.DocumentService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository repo;

    public DocumentServiceImpl(DocumentRepository repo) {
        this.repo = repo;
    }


    @Override
    public Document createDoc(CreateDocumentRequest req) {
        //create file validator and validate req.file()
        UUID documentId = UUID.randomUUID();
        String s3key = "course/" + req.courseId() + "documents/" + documentId + ":" + req.fileName();
        //upload req.file() to s3
        Document doc = new Document(documentId, req.courseId(), req.fileName(), req.fileType(), s3key, DocStatus.PROCESSING);
        //process doc, extracting text and whatnot then set status to uploaded or failed

        return repo.save(doc);
    }

    @Override
    public List<Document> listDocs() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "title"));
    }

    @Override
    public void deleteDoc(UUID docId) {
        //find the s3 path and delete that path
        repo.deleteById(docId);
    }
}
