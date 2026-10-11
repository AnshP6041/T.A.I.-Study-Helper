package com.ansh.tai.extraction.impl;

import com.ansh.tai.extraction.DocumentExtractor;
import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.extraction.ExtractionService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExtractionServiceImpl implements ExtractionService {
    private final List<DocumentExtractor> extractors;

    public ExtractionServiceImpl(List<DocumentExtractor> extractors) {
        this.extractors = extractors;
    }

    @Override
    public List<ExtractedPage> extract(byte[] file, String ext) throws IOException {
        return extractors.stream().filter(x -> x.supports(ext)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file type: " + ext))
                .extract(file);
    }

    @Override
    public String unitLabel(String ext) {
        return ext.equals("pptx") ? "Slide" : "Page";
    }
}
