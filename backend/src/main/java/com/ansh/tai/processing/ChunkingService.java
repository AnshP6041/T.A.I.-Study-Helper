package com.ansh.tai.processing;

import com.ansh.tai.domain.extraction.ExtractedPage;

import java.util.List;

public interface ChunkingService {
    List<String> chunk(String fileName, String unit, ExtractedPage page, String visualDescription);
}
