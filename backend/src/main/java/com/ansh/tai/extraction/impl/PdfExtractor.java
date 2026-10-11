package com.ansh.tai.extraction.impl;

import com.ansh.tai.extraction.DocumentExtractor;
import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.extraction.ImageUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PdfExtractor implements DocumentExtractor {
    private record PageGraphics(Set<String> imageKeys, int shapes) {}

    @Override
    public boolean supports(String extension) {
        return extension.equals("pdf");
    }


    @Override
    public List<ExtractedPage> extract(byte[] file) throws IOException {
        List<ExtractedPage> pages = new ArrayList<>();
        try (PDDocument pdf = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new ScriptAwareStripper();
            PDFRenderer renderer = new PDFRenderer(pdf);
            int pageCount = pdf.getNumberOfPages();
            List<PageGraphics> allGfx = new ArrayList<>();
            Map<String, Integer> pagesPerImage = new HashMap<>();
            for (PDPage page : pdf.getPages()) {
                PageGraphics g = countGraphics(page);
                allGfx.add(g);
                g.imageKeys().forEach(k -> pagesPerImage.merge(k, 1, Integer::sum));
            }
            Set<String> templateImages = pagesPerImage.entrySet().stream()
                    .filter(e -> pageCount >= 3 && e.getValue() > pageCount / 2)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String text = stripper.getText(pdf).strip();

                PageGraphics gfx = allGfx.get(i);
                long contentImages = gfx.imageKeys().stream().filter(k -> !templateImages.contains(k)).count();
                boolean visual = contentImages > 0 || gfx.shapes() > 15 || text.isEmpty();
                BufferedImage img = renderer.renderImageWithDPI(i, 110, ImageType.RGB);
                boolean hasMath = text.codePoints().anyMatch(c -> c >= 0x1D400 && c <= 0x1D7FF);

                pages.add(new ExtractedPage(i + 1, null, text, List.of(), null, visual || hasMath, ImageUtils.toPng(img), hasMath, List.of()));
            }
        }
        return pages;
    }

    private PageGraphics countGraphics(PDPage page) throws IOException {
        var engine = new PDFGraphicsStreamEngine(page) {
            final Set<String> images = new HashSet<>();
            int shapes = 0;

            @Override public void drawImage(PDImage img) {
                if (img instanceof PDImageXObject x) {
                    images.add(x.getWidth() + "x" + x.getHeight() + ":" + x.getCOSObject().getLength());
                } else {
                    images.add("inline-" + UUID.randomUUID());
                }
            }
            @Override public void strokePath() { shapes++; }
            @Override public void fillPath(int windingRule) { shapes++; }
            @Override public void fillAndStrokePath(int windingRule) { shapes++; }

            @Override public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {}
            @Override public void clip(int windingRule) {}
            @Override public void moveTo(float x, float y) {}
            @Override public void lineTo(float x, float y) {}
            @Override public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {}
            @Override public Point2D getCurrentPoint() { return new Point2D.Float(); }
            @Override public void closePath() {}
            @Override public void endPath() {}
            @Override public void shadingFill(COSName shadingName) {}
        };
        engine.processPage(page);
        return new PageGraphics(engine.images, engine.shapes);
    }


}

