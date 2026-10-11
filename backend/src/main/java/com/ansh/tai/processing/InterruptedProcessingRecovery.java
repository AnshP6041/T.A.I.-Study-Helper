package com.ansh.tai.processing;

import com.ansh.tai.domain.entity.DocStatus;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InterruptedProcessingRecovery {
    private static final Logger log = LoggerFactory.getLogger(InterruptedProcessingRecovery.class);
    private final DocumentRepository repo;

    public InterruptedProcessingRecovery(DocumentRepository repo) {
        this.repo = repo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void markInterruptedAsFailed() {
        List<Document> stuck = repo.findAllByStatus(DocStatus.PROCESSING);
        if (stuck.isEmpty()) return;
        stuck.forEach(d -> d.setStatus(DocStatus.FAILED));
        repo.saveAll(stuck);
        log.warn("Marked {} interrupted document(s) as FAILED; use Retry to process them again", stuck.size());
    }
}