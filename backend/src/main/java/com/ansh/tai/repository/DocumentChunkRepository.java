package com.ansh.tai.repository;

import com.ansh.tai.domain.extraction.DocumentChunk;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentChunkRepository {
    void replaceChunks(UUID documentId, List<DocumentChunk> chunks);
    void deleteChunks(UUID documentId);
    Optional<UUID> findDuplicateOf(UUID documentId, double minSimilarity, double minShareOfChunks);
}
