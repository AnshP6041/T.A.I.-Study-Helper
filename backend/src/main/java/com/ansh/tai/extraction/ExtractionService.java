package com.ansh.tai.extraction;

import com.ansh.tai.domain.extraction.ExtractedPage;

import java.io.IOException;
import java.util.List;

public interface ExtractionService {
    List<ExtractedPage> extract(byte[] file, String extension) throws IOException;
    String unitLabel(String extension);
}
