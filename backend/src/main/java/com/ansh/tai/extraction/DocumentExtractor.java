package com.ansh.tai.extraction;

import com.ansh.tai.domain.extraction.ExtractedPage;

import java.io.IOException;
import java.util.List;

public interface DocumentExtractor {
    boolean supports(String extension);
    List<ExtractedPage> extract(byte[] file) throws IOException;

}