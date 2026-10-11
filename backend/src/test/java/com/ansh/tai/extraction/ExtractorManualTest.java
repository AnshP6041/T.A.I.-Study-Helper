package com.ansh.tai.extraction;

import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.extraction.impl.PdfExtractor;
import com.ansh.tai.extraction.impl.PptxExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ExtractorManualTest {

    private static final Path SAMPLES = Path.of("src/test/resources/samples");
    private static final Path OUTPUT  = Path.of("target/extraction-output");

    @Test
    void extractPdf() throws IOException {
        run(new PdfExtractor(), SAMPLES.resolve("Lecture Notes Sep 9 v2 - Tagged.pdf"));
    }

    @Test
    void extractPptx() throws IOException {
        run(new PptxExtractor(), SAMPLES.resolve("Lecture Notes Sep 9 v2.pptx"));
    }

    private void run(DocumentExtractor extractor, Path file) throws IOException {
        assumeTrue(Files.exists(file), "Put a file at " + file.toAbsolutePath() + " to run this test");

        List<ExtractedPage> pages = extractor.extract(Files.readAllBytes(file));
        assertFalse(pages.isEmpty(), "No pages extracted");

        Path outDir = OUTPUT.resolve(file.getFileName().toString());
        Files.createDirectories(outDir);

        StringBuilder report = new StringBuilder();
        int visualCount = 0;

        for (int i = 0; i < pages.size(); i++) {
            ExtractedPage p = pages.get(i);

            assertEquals(i + 1, p.pageNumber(), "Page numbers should be 1, 2, 3...");
            assertNotNull(p.pageImagePng(), "Page " + p.pageNumber() + " has no image");
            assertTrue(p.pageImagePng().length > 0, "Page " + p.pageNumber() + " image is empty");

            Files.write(outDir.resolve(p.pageNumber() + ".png"), p.pageImagePng());
            if (p.likelyVisual()) visualCount++;

            report.append("========== Page ").append(p.pageNumber())
                    .append(p.likelyVisual() ? "  [VISUAL -> would go to vision model]" : "")
                    .append(" ==========\n");
            if (p.title() != null)  report.append("TITLE: ").append(p.title()).append("\n");
            report.append("TEXT:\n").append(p.text() == null || p.text().isBlank() ? "(none)" : p.text()).append("\n");
            for (String table : p.tables()) report.append("TABLE:\n").append(table).append("\n");
            if (p.notes() != null) report.append("NOTES:\n").append(p.notes()).append("\n");
            report.append("\n");
        }

        report.insert(0, "%s: %d pages, %d flagged visual%n%n"
                .formatted(file.getFileName(), pages.size(), visualCount));

        Files.writeString(outDir.resolve("report.txt"), report);
        System.out.println(report);
        System.out.println("Output written to: " + outDir.toAbsolutePath());
    }
}