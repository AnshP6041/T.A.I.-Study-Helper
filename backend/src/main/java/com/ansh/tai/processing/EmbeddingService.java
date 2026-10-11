package com.ansh.tai.processing;

import java.util.List;

public interface EmbeddingService {
    List<float[]> embedDocuments(List<String> texts);
    float[] embedQuery(String text);
}
