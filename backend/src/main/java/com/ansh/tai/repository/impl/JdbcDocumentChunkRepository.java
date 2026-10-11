package com.ansh.tai.repository.impl;

import com.ansh.tai.domain.extraction.DocumentChunk;
import com.ansh.tai.repository.DocumentChunkRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcDocumentChunkRepository implements DocumentChunkRepository {
    private final JdbcTemplate jdbc;

    public JdbcDocumentChunkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void replaceChunks(UUID documentId, List<DocumentChunk> chunks) {
        jdbc.update("delete from document_chunks where document_id = ?", documentId);

        jdbc.batchUpdate("""
            insert into document_chunks
              (id, document_id, course_id, page_number, chunk_index, content, page_image_key, embedding)
            values (?, ?, ?, ?, ?, ?, ?, ?::vector)
            """,
                chunks.stream().map(c -> new Object[]{
                        UUID.randomUUID(), documentId, c.courseId(),
                        c.pageNumber(), c.chunkIndex(), c.content(),
                        c.pageImageKey(), toPgVector(c.embedding())
                }).toList());
    }

    @Override
    public void deleteChunks(UUID documentId) {
        jdbc.update("delete from document_chunks where document_id = ?", documentId);
    }

    @Override
    public Optional<UUID> findDuplicateOf(UUID documentId, double minSimilarity, double minShareOfChunks) {
        Integer total = jdbc.queryForObject(
                "select count(*) from document_chunks where document_id = ?", Integer.class, documentId);
        if (total == null || total == 0) return Optional.empty();

        List<Map<String, Object>> rows = jdbc.queryForList("""
            select best.document_id, count(*) as matched
            from document_chunks c
            cross join lateral (
                select o.document_id, 1 - (o.embedding <=> c.embedding) as similarity
                from document_chunks o
                where o.course_id = c.course_id and o.document_id <> c.document_id
                order by o.embedding <=> c.embedding
                limit 1
            ) best
            where c.document_id = ? and best.similarity >= ?
            group by best.document_id
            order by matched desc
            limit 1
            """, documentId, minSimilarity);

        if (rows.isEmpty()) return Optional.empty();
        long matched = ((Number) rows.getFirst().get("matched")).longValue();
        return matched >= minShareOfChunks * total
                ? Optional.of((UUID) rows.getFirst().get("document_id"))
                : Optional.empty();
    }

    static String toPgVector(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        return sb.append(']').toString();
    }
}
