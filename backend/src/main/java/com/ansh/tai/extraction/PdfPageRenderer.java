package com.ansh.tai.extraction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PdfPageRenderer {
    private PdfPageRenderer() {}

    public static List<byte[]> render(byte[] pdfBytes, float dpi) throws IOException {
        List<byte[]> images = new ArrayList<>();
        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                images.add(ImageUtils.toPng(renderer.renderImageWithDPI(i, dpi, ImageType.RGB)));
            }
        }
        return images;
    }
}