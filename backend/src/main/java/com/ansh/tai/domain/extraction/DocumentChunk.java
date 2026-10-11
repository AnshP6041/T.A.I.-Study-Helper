package com.ansh.tai.domain.extraction;

import java.util.UUID;

public record DocumentChunk(
        UUID courseId,
        int pageNumber,
        int chunkIndex,
        String content,
        String pageImageKey,
        float[] embedding) {

}