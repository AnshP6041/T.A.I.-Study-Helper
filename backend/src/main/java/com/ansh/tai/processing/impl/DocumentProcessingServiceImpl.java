package com.ansh.tai.processing.impl;

import com.ansh.tai.domain.entity.DocStatus;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.domain.extraction.DocumentChunk;
import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.extraction.DocumentConverter;
import com.ansh.tai.extraction.ExtractionService;
import com.ansh.tai.extraction.PdfPageRenderer;
import com.ansh.tai.processing.ChunkingService;
import com.ansh.tai.processing.DocumentProcessingService;
import com.ansh.tai.processing.EmbeddingService;
import com.ansh.tai.processing.VisionService;
import com.ansh.tai.repository.DocumentChunkRepository;
import com.ansh.tai.repository.DocumentRepository;
import com.ansh.tai.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingServiceImpl.class);

    private final DocumentRepository repo;
    private final StorageService storage;
    private final ExtractionService extraction;
    private final VisionService vision;
    private final EmbeddingService embeddings;
    private final ChunkingService chunker;
    private final DocumentConverter converter;
    private final DocumentChunkRepository cRepo;
    private final Semaphore visionLimit = new Semaphore(8);
    private final double minSimilarity;
    private final double minShare;
    private record PageResult(ExtractedPage page, String imageKey, String description) {}


    public DocumentProcessingServiceImpl(DocumentRepository repo, StorageService storage,
                                         ExtractionService extraction, VisionService vision,
                                         EmbeddingService embeddings, ChunkingService chunker,
                                         DocumentConverter converter, DocumentChunkRepository cRepo,
                                         @Value("${duplicates.min-similarity}") double minSimilarity,
                                         @Value("${duplicates.min-share}") double minShare) {
        this.repo = repo;
        this.storage = storage;
        this.extraction = extraction;
        this.vision = vision;
        this.embeddings = embeddings;
        this.chunker = chunker;
        this.converter = converter;
        this.cRepo = cRepo;
        this.minSimilarity = minSimilarity;
        this.minShare = minShare;
    }

    @Override
    public void process(UUID docId) {
        Document doc = repo.findById(docId).orElse(null);
        if (doc == null) {
            log.info("Document {} was deleted before processing started; skipping", docId);
            return;
        }

        try {
            runPipeline(doc);
            doc.setStatus(DocStatus.READY);
            markDuplicates(doc);
        } catch (Exception e) {
            Throwable cause = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause() : e;
            log.error("Processing failed for document {}", docId, cause);
            doc.setStatus(DocStatus.FAILED);
        }

        if (!repo.existsById(docId)) {
            log.info("Document {} was deleted during processing; discarding results", docId);
            return;
        }
        repo.save(doc);
    }

    private void markDuplicates(Document doc) {
        cRepo.findDuplicateOf(doc.getId(), minSimilarity, minShare).ifPresent(otherId -> {
            Document other = repo.findById(otherId).orElse(null);
            if (other == null) return;

            boolean keepNew = doc.getFileType().equals("pptx") && !other.getFileType().equals("pptx");
            Document keep = keepNew ? doc : other;
            Document drop = keepNew ? other : doc;

            cRepo.deleteChunks(drop.getId());
            drop.setStatus(DocStatus.DUPLICATE);
            drop.setDuplicateOfId(keep.getId());
            if (drop != doc) repo.save(drop);
            log.info("Document {} is a duplicate of {}; removed it from search", drop.getId(), keep.getId());
        });
    }

    private void runPipeline(Document doc) throws Exception {
        String ext = extension(doc.getS3Key());
        String unit = extraction.unitLabel(ext);
        byte[] original = storage.download(doc.getS3Key());
        String folder = doc.getS3Key().substring(0, doc.getS3Key().lastIndexOf('/') + 1);

        List<ExtractedPage> pages = extraction.extract(original, ext);

        if (!ext.equals("pdf")) {
            byte[] previewPdf = converter.toPdf(original, ext);
            String previewKey = folder + "preview.pdf";
            storage.upload(previewPdf, "application/pdf", previewKey);
            doc.setPreviewS3Key(previewKey);
            repo.save(doc);

            List<byte[]> images = PdfPageRenderer.render(previewPdf, 110);
            if (images.size() != pages.size()) {
                throw new IllegalStateException("Slide count mismatch: extracted " + pages.size()
                        + " slides but the PDF has " + images.size() + " pages");
            }
            for (int i = 0; i < pages.size(); i++) {
                ExtractedPage p = pages.get(i);
                pages.set(i, new ExtractedPage(p.pageNumber(), p.title(), p.text(), p.tables(), p.notes(),
                        p.likelyVisual(), images.get(i), p.hasMath(), p.equationImages()));
            }
        }

        String prefix = folder + "pages/";

        List<PageResult> results = new ArrayList<>();
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<PageResult>> futures = pages.stream()
                    .map(p -> exec.submit(() -> processPage(p, prefix)))
                    .toList();
            for (Future<PageResult> f : futures) {
                PageResult r = f.get();
                results.add(r);
            }
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        record Pending(int page, int idx, String content, String imageKey) {}
        List<Pending> pending = new ArrayList<>();

        for (PageResult r : results) {
            List<String> pieces = chunker.chunk(doc.getFileName(), unit, r.page(), r.description());
            for (int i = 0; i < pieces.size(); i++) {
                pending.add(new Pending(r.page().pageNumber(), i, pieces.get(i), r.imageKey()));
                texts.add(pieces.get(i));
            }
        }
        if (pending.isEmpty()) {
            throw new IllegalStateException("No content could be extracted from " + doc.getFileName());
        }

        List<float[]> vectors = embeddings.embedDocuments(texts);
        for (int i = 0; i < pending.size(); i++) {
            Pending p = pending.get(i);
            chunks.add(new DocumentChunk(doc.getCourseId(), p.page(), p.idx(), p.content(), p.imageKey(), vectors.get(i)));
        }

        cRepo.replaceChunks(doc.getId(), chunks);
    }


    private PageResult processPage(ExtractedPage p, String prefix) throws InterruptedException {
        String key = prefix + p.pageNumber() + ".png";
        storage.upload(p.pageImagePng(), "image/png", key);

        if (!p.likelyVisual()) {
            return new PageResult(p, key, null);
        }

        visionLimit.acquire();
        try {
            if (p.hasMath()) {
                String transcript = vision.transcribe(p.pageImagePng(), p.equationImages(), p.text(), !p.tables().isEmpty());
                ExtractedPage replaced = new ExtractedPage(p.pageNumber(), p.title(), transcript,
                        p.tables(), p.notes(), true, p.pageImagePng(), true, p.equationImages());
                return new PageResult(replaced, key, null);
            }
            return new PageResult(p, key, vision.describe(p.pageImagePng(), p.text()));
        } finally {
            visionLimit.release();
        }
    }

    private static String extension(String key) {
        return key.substring(key.lastIndexOf('.') + 1).toLowerCase();
    }

}
