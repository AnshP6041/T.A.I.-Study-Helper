package com.ansh.tai.repository;

import com.ansh.tai.domain.entity.DocStatus;
import com.ansh.tai.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findAllByCourseIdOrderByFileNameAsc(UUID courseId);

    void deleteAllByCourseId(UUID courseId);

    Optional<Document> findFirstByCourseIdAndContentHash(UUID courseId, String contentHash);

    List<Document> findAllByDuplicateOfId(UUID duplicateOfId);

    List<Document> findAllByStatus(DocStatus status);
}
