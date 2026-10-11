package com.ansh.tai.processing.impl;

import com.ansh.tai.configuration.AnthropicProperties;
import com.ansh.tai.processing.VisionService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeVisionService implements VisionService {
    private final RestClient http;
    private final String model;

    public ClaudeVisionService(
            AnthropicProperties props) {
        this.model = props.visionModel();
        this.http = RestClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", props.apiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    @Override
    public String describe(byte[] png, String extractedText) {
        String prompt = """
        This is one page/slide from a student's course material. Its extracted text is below.
        Describe ONLY the visual content that the text doesn't already capture: diagrams, charts
        (axes, trends, key values), images, equations, flowcharts, and any text that appears inside images.
        If the page contains tables, transcribe them as markdown tables.
        Ignore decorative template elements such as background images, logos, and footers.
        Be specific and factual so a student could answer exam questions from your description.
        If there is no meaningful visual content beyond the text, reply with exactly: NONE

        Extracted text:
        """ + nullToEmpty(extractedText);

        String out = call(List.of(png), prompt, 800);
        return out.isEmpty() || out.equals("NONE") ? null : out;
    }

    @Override
    public String transcribe(byte[] png, List<byte[]> equationImages, String extractedText, boolean hasTables) {
        String tableRule = hasTables
                ? "Do NOT transcribe tables; they are already extracted separately."
                : "If there are tables, write them as markdown tables.";

        String imageNote = equationImages.isEmpty() ? "" : """
            The first image is the whole slide. The additional images are high-resolution
            renderings of text boxes on this slide that contain equations. The slide image may
            show those areas blank or incomplete, so take the text and equations from the
            additional images, placing them where they belong on the slide.
            """;

        String prompt = imageNote + """
            Transcribe ALL content on this slide as plain text, in reading order.
            Write math in plain-text notation:
            - superscripts with ^ : b^d, 10^6, b_1^d
            - subscripts with _ : b_1, x_i
            - fractions as (numerator)/(denominator): (N × 1 KB)/(1024 × 1024)
            - keep symbols like ≈, ×, ≤, Σ, √ as they are
            %s
            After the transcription, if there are diagrams, charts, or images, add a section
            "Visual content:" describing them.
            Ignore decorative template elements such as background images, logos, and footers.
            Output only the transcription, with no introduction.

            The extracted text below has broken math formatting; use it only to check spelling:
            %s
            """.formatted(tableRule, nullToEmpty(extractedText));

        List<byte[]> images = new ArrayList<>();
        images.add(png);
        images.addAll(equationImages);
        return call(images, prompt, 2000);
    }

    private String call(List<byte[]> images, String prompt, int maxTokens) {
        List<Map<String, Object>> content = new ArrayList<>();
        for (byte[] img : images) {
            content.add(Map.of("type", "image", "source", Map.of(
                    "type", "base64", "media_type", "image/png",
                    "data", Base64.getEncoder().encodeToString(img))));
        }
        content.add(Map.of("type", "text", "text", prompt));

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(Map.of("role", "user", "content", content)));

        JsonNode res = http.post().uri("/messages").body(body).retrieve().body(JsonNode.class);
        if (res == null) throw new IllegalStateException("Empty response from Claude API");
        return res.path("content").path(0).path("text").asString("").strip();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
