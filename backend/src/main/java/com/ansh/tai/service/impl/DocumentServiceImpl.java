package com.ansh.tai.service.impl;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.entity.DocStatus;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.domain.event.DocumentUploadedEvent;
import com.ansh.tai.exception.DocumentNotFoundException;
import com.ansh.tai.exception.DuplicateDocumentException;
import com.ansh.tai.exception.UnsupportedFileTypeException;
import com.ansh.tai.repository.DocumentRepository;
import com.ansh.tai.service.DocumentService;
import com.ansh.tai.service.StorageService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "pptx");

    private final DocumentRepository repo;
    private final StorageService s3Serve;
    private final ApplicationEventPublisher events;

    public DocumentServiceImpl(DocumentRepository repo, StorageService s3Serve, ApplicationEventPublisher events) {
        this.repo = repo;
        this.s3Serve = s3Serve;
        this.events = events;
    }


    @Override
    @Transactional
    public Document createDoc(CreateDocumentRequest req) {
        String ext = extensionOf(req.file().getOriginalFilename());
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new UnsupportedFileTypeException(ext);
        }

        String base = baseName(req.fileName());
        repo.findAllByCourseIdOrderByFileNameAsc(req.courseId()).stream()
                .filter(d -> baseName(d.getFileName()).equals(base))
                .findFirst()
                .ifPresent(existing -> { throw new DuplicateDocumentException(existing); });

        String hash = sha256(req.file());
        repo.findFirstByCourseIdAndContentHash(req.courseId(), hash).ifPresent(existing -> {
            throw new DuplicateDocumentException(existing);
        });

        UUID documentId = UUID.randomUUID();
        String safeName = req.fileName().replaceAll("[/\\\\]", "_");
        if (!safeName.toLowerCase().endsWith("." + ext)) safeName += "." + ext;

        String s3Key = "course/" + req.courseId() + "/documents/" + documentId + "/" + safeName;
        s3Serve.upload(req.file(), s3Key);

        Document doc = new Document(documentId, req.courseId(), req.fileName(), ext, s3Key, DocStatus.PROCESSING, ext.equals("pdf") ? s3Key: null);
        doc.setContentHash(hash);
        repo.save(doc);
        events.publishEvent(new DocumentUploadedEvent(documentId));
        return doc;
    }

    private static String baseName(String fileName) {
        return fileName.trim()
                .replaceFirst("(?i)\\.(pdf|pptx)$", "")
                .trim()
                .toLowerCase();
    }

    private static String sha256(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
            return HexFormat.of().formatHex(md.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not read upload", e);
        }
    }

    @Override
    @Transactional
    public Document reprocessDoc(UUID docId) {
        Document doc = repo.findById(docId)
                .orElseThrow(() -> new DocumentNotFoundException(docId));
        if (doc.getStatus() == DocStatus.PROCESSING) {
            return doc;
        }
        doc.setStatus(DocStatus.PROCESSING);
        doc.setDuplicateOfId(null);
        repo.save(doc);
        events.publishEvent(new DocumentUploadedEvent(docId));
        return doc;
    }

    @Override
    public List<Document> listDocs(UUID courseId) {
        return repo.findAllByCourseIdOrderByFileNameAsc(courseId);
    }

    @Override
    @Transactional
    public void deleteDoc(UUID docId) {
        Document doc = repo.findById(docId)
                        .orElseThrow(() -> new DocumentNotFoundException(docId));
        String docPrefix = "course/" + doc.getCourseId() + "/documents/" + docId + "/";
        s3Serve.delete(docPrefix);
        repo.deleteById(docId);

        for (Document d : repo.findAllByDuplicateOfId(docId)) {
            d.setStatus(DocStatus.PROCESSING);
            d.setDuplicateOfId(null);
            repo.save(d);
            events.publishEvent(new DocumentUploadedEvent(d.getId()));
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
