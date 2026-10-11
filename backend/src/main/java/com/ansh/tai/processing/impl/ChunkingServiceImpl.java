package com.ansh.tai.processing.impl;

import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.processing.ChunkingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingServiceImpl implements ChunkingService {
    private static final int MAX_CHARS = 2000;
    private static final int OVERLAP   = 200;

    @Override
    public List<String> chunk(String fileName, String unit, ExtractedPage p, String visualDescription) {
        String header = "[%s | %s %d%s]".formatted(
                fileName, unit, p.pageNumber(), p.title() != null ? " | " + p.title() : "");

        StringBuilder body = new StringBuilder(p.text() == null ? "" : p.text());
        p.tables().forEach(t -> body.append("\n\nTable:\n").append(t));
        if (p.notes() != null) body.append("\n\nSpeaker notes:\n").append(p.notes());
        if (visualDescription != null) body.append("\n\nVisual content:\n").append(visualDescription);

        String full = body.toString().strip();
        if (full.isEmpty()) return List.of();
        if (full.length() <= MAX_CHARS) return List.of(header + "\n" + full);

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < full.length()) {
            int end = Math.min(start + MAX_CHARS, full.length());
            int para = full.lastIndexOf("\n\n", end);
            if (end < full.length() && para > start + MAX_CHARS / 2) end = para;
            chunks.add(header + "\n" + full.substring(start, end).strip());
            if (end == full.length()) break;
            start = Math.max(end - OVERLAP, start + 1);
        }
        return chunks;
    }
}
