package com.ansh.tai.processing;

import com.ansh.tai.configuration.AnthropicProperties;
import com.ansh.tai.configuration.VoyageProperties;
import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.extraction.impl.PptxExtractor;
import com.ansh.tai.processing.impl.ClaudeVisionService;
import com.ansh.tai.processing.impl.VoyageEmbeddingService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ApiClientsManualTest {

    private static final Path PAGES  = Path.of("target/extraction-output/Lecture Notes Sep 9 v2.pptx");
    private static final Path OUTPUT = Path.of("target/api-output");

    @Test
    void vision() throws IOException {
        String key = System.getenv("ANTHROPIC_API_KEY");
        assumeTrue(key != null && !key.isBlank(), "Set ANTHROPIC_API_KEY to run this test");
        assumeTrue(Files.exists(PAGES.resolve("9.png")), "Run ExtractorManualTest first");

        VisionService vision = new ClaudeVisionService(new AnthropicProperties(key, "claude-sonnet-5-5"));
        Files.createDirectories(OUTPUT);

        String description = vision.describe(Files.readAllBytes(PAGES.resolve("5.png")), "");

        Files.writeString(OUTPUT.resolve("slide5-describe.txt"), String.valueOf(description));
        System.out.println("=== Slide 5 description ===\n" + description);

        assertNotNull(description, "Slide 5 has a diagram, so describe() should not return null");
    }

    @Test
    void transcribeMathSlide() throws IOException {
        String key = System.getenv("ANTHROPIC_API_KEY");
        assumeTrue(key != null && !key.isBlank(), "Set ANTHROPIC_API_KEY to run this test");

        byte[] pptx = Files.readAllBytes(Path.of("src/test/resources/samples/Lecture Notes Sep 9 v2.pptx"));
        ExtractedPage slide9 = new PptxExtractor().extract(pptx).get(8);
        assertTrue(slide9.hasMath());
        assertEquals(2, slide9.equationImages().size(), "Slide 9 has two equation text boxes");

        VisionService vision = new ClaudeVisionService(new AnthropicProperties(key, "claude-sonnet-5-5"));
        String transcript = vision.transcribe(slide9.pageImagePng(), slide9.equationImages(),
                slide9.text(), !slide9.tables().isEmpty());

        Files.createDirectories(OUTPUT);
        Files.writeString(OUTPUT.resolve("slide9-transcribe.txt"), transcript);

        System.out.println(transcript);
        assertTrue(transcript.contains("10^6"), "Expected the equations to be transcribed");
    }

    @Test
    void embeddings() {
        String key = System.getenv("VOYAGE_API_KEY");
        assumeTrue(key != null && !key.isBlank(), "Set VOYAGE_API_KEY to run this test");

        EmbeddingService embeddings = new VoyageEmbeddingService(new VoyageProperties(key, "voyage-4-large"));

        List<String> docs = List.of(
                "Breadth-first search stores the frontier in a FIFO queue.",
                "A* expands the node with the lowest f(n) = g(n) + h(n).",
                "The Krebs cycle produces ATP in the mitochondria.");
        List<float[]> docVectors = embeddings.embedDocuments(docs);
        float[] query = embeddings.embedQuery("Which search algorithm uses a queue?");

        assertEquals(3, docVectors.size());
        assertEquals(1024, query.length);

        for (int i = 0; i < docs.size(); i++) {
            System.out.printf("%.3f  %s%n", cosine(query, docVectors.get(i)), docs.get(i));
        }
        assertTrue(cosine(query, docVectors.get(0)) > cosine(query, docVectors.get(2)));
    }

    private static double cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i]; }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}