package com.ansh.tai.processing.impl;

import com.ansh.tai.configuration.VoyageProperties;
import com.ansh.tai.processing.EmbeddingService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VoyageEmbeddingService implements EmbeddingService {
    private static final int BATCH_SIZE = 128;
    private static final int DIMENSIONS = 1024;

    private final RestClient http;
    private final String model;

    public VoyageEmbeddingService(VoyageProperties props) {
        this.model = props.model();
        this.http = RestClient.builder()
                .baseUrl("https://api.voyageai.com/v1")
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .build();
    }

    @Override
    public List<float[]> embedDocuments(List<String> texts) {
        List<float[]> out = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
            out.addAll(embed(texts.subList(i, Math.min(i + BATCH_SIZE, texts.size())), "document"));
        }
        return out;
    }

    @Override
    public float[] embedQuery(String text) {
        return embed(List.of(text), "query").getFirst();
    }

    private List<float[]> embed(List<String> batch, String inputType) {
        if (batch.isEmpty()) return List.of();

        Map<String, Object> body = Map.of(
                "input", batch,
                "model", model,
                "input_type", inputType,
                "output_dimension", DIMENSIONS);

        JsonNode res = http.post().uri("/embeddings").body(body).retrieve().body(JsonNode.class);
        assert res != null;
        JsonNode data = res.path("data");
        if (data.size() != batch.size()) {
            throw new IllegalStateException("Voyage returned " + data.size() + " embeddings for " + batch.size() + " inputs");
        }

        float[][] vectors = new float[batch.size()][];
        for (JsonNode item : data) {
            JsonNode e = item.path("embedding");
            float[] v = new float[e.size()];
            for (int j = 0; j < v.length; j++) v[j] = (float) e.get(j).asDouble();
            vectors[item.path("index").asInt()] = v;
        }
        return List.of(vectors);
    }
}
