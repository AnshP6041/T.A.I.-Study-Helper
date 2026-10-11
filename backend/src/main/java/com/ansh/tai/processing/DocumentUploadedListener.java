package com.ansh.tai.processing;

import com.ansh.tai.domain.event.DocumentUploadedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DocumentUploadedListener {
    private final DocumentProcessingService processor;

    public DocumentUploadedListener(DocumentProcessingService processor) {
        this.processor = processor;
    }

    @Async("documentProcessingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUploaded(DocumentUploadedEvent event) {
        processor.process(event.docId());
    }
}
