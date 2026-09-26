package com.ansh.tai.service.impl;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.entity.DocStatus;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.exception.DocumentNotFoundException;
import com.ansh.tai.repository.DocumentRepository;
import com.ansh.tai.service.DocumentService;
import com.ansh.tai.service.StorageService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository repo;
    private final StorageService s3Serve;

    public DocumentServiceImpl(DocumentRepository repo, StorageService s3Serve) {
        this.repo = repo;
        this.s3Serve = s3Serve;
    }


    @Override
    public Document createDoc(CreateDocumentRequest req) {
        //create file validator and validate req.file()
        UUID documentId = UUID.randomUUID();
        String s3key = "course/" + req.courseId() + "/documents/" + documentId + "/" + req.fileName();
        s3Serve.upload(req.file(), s3key);
        Document doc = new Document(documentId, req.courseId(), req.fileName(), req.fileType(), s3key, DocStatus.PROCESSING);
        repo.save(doc);
        //process doc asynchronously, so frontend gets processing status, extracting text and whatnot then set status to ready or failed in method once done

        return doc;
    }

    @Override
    public List<Document> listDocs() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "title"));
    }

    @Override
    public void deleteDoc(UUID docId) {
        Document doc = repo.findById(docId)
                        .orElseThrow(() -> new DocumentNotFoundException(docId));
        s3Serve.delete(doc.getS3Key());
        repo.deleteById(docId);
    }
}
