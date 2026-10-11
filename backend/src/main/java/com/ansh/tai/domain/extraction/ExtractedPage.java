package com.ansh.tai.domain.extraction;

import java.util.List;

public record ExtractedPage(
        int pageNumber,
        String title,
        String text,
        List<String> tables,
        String notes,
        boolean likelyVisual,
        byte[] pageImagePng,
        boolean hasMath,
        List<byte[]> equationImages
) {

}