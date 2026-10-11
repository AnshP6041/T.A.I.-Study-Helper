package com.ansh.tai.extraction.impl;

import com.ansh.tai.extraction.DocumentExtractor;
import com.ansh.tai.domain.extraction.ExtractedPage;
import com.ansh.tai.extraction.ImageUtils;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PptxExtractor implements DocumentExtractor {
    private static final Logger log = LoggerFactory.getLogger(PptxExtractor.class);
    private static final Pattern FALLBACK = Pattern.compile("<(?:\\w+:)?Fallback\\b.*?</(?:\\w+:)?Fallback>", Pattern.DOTALL);
    private static final Pattern EMBED    = Pattern.compile("\\bembed=\"([^\"]+)\"");


    @Override
    public boolean supports(String ext) { return ext.equals("pptx"); }

    @Override
    public List<ExtractedPage> extract(byte[] file) throws IOException {
        List<ExtractedPage> pages = new ArrayList<>();
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(file))) {
            Dimension size = ppt.getPageSize();
            int n = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                var x = slide.getXmlObject();
                if (x.isSetShow() && !x.getShow()) continue;

                StringBuilder text = new StringBuilder();
                List<String> tables = new ArrayList<>();
                boolean[] visual = {false};
                walk(slide.getShapes(), text, tables, visual);

                boolean hasMath = slide.getXmlObject().xmlText().contains("oMath");
                List<byte[]> eqImages = hasMath ? equationImages(slide) : List.of();

                pages.add(new ExtractedPage(
                        n++, slide.getTitle(), text.toString().strip(), tables,
                        notesText(slide), visual[0] || hasMath, render(slide, size), hasMath, eqImages));
            }
        }
        return pages;
    }

    private List<byte[]> equationImages(XSLFSlide slide) {
        List<byte[]> images = new ArrayList<>();
        Matcher fallback = FALLBACK.matcher(slide.getXmlObject().xmlText());
        while (fallback.find()) {
            Matcher embed = EMBED.matcher(fallback.group());
            while (embed.find()) {
                if (slide.getRelationById(embed.group(1)) instanceof XSLFPictureData pd) {
                    try {
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(pd.getData()));
                        if (img != null) images.add(ImageUtils.toPng(img));
                    } catch (IOException e) {
                        log.warn("Could not read equation image on slide {}: {}", slide.getSlideNumber(), e.getMessage());
                    }
                }
            }
        }
        return images;
    }

    private void walk(List<XSLFShape> shapes, StringBuilder text, List<String> tables, boolean[] visual) {
        for (XSLFShape s : shapes) {
            switch (s) {
                case XSLFGroupShape g   -> walk(g.getShapes(), text, tables, visual);
                case XSLFTable t        -> tables.add(toMarkdown(t));
                case XSLFTextShape ts   -> { String t = textWithScripts(ts); if (!t.isBlank()) text.append(t).append("\n"); }
                case XSLFPictureShape p -> visual[0] = true;
                case XSLFGraphicFrame f -> visual[0] = true;
                default -> {}
            }
        }
    }

    private String textWithScripts(XSLFTextShape shape) {
        StringBuilder sb = new StringBuilder();
        for (XSLFTextParagraph para : shape.getTextParagraphs()) {
            for (XSLFTextRun run : para.getTextRuns()) {
                String r = run.getRawText();
                if (r == null || r.isEmpty()) continue;
                if (run.isSuperscript())    sb.append('^').append(r.length() > 1 ? "(" + r + ")" : r);
                else if (run.isSubscript()) sb.append('_').append(r.length() > 1 ? "(" + r + ")" : r);
                else                        sb.append(r);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String notesText(XSLFSlide slide) {
        XSLFNotes notes = slide.getNotes();
        if (notes == null) return null;
        StringBuilder sb = new StringBuilder();
        for (XSLFShape s : notes.getShapes()) {
            if (s instanceof XSLFTextShape ts
                    && ts.getTextType() != null
                    && ts.getTextType().equals(Placeholder.BODY)) {
                sb.append(textWithScripts(ts));
            }
        }
        String out = sb.toString().strip();
        return out.isEmpty() ? null : out;
    }

    private String toMarkdown(XSLFTable t) {
        StringBuilder sb = new StringBuilder();
        List<XSLFTableRow> rows = t.getRows();
        for (int r = 0; r < rows.size(); r++) {
            sb.append("| ");
            for (XSLFTableCell c : rows.get(r).getCells())
                sb.append(textWithScripts(c).strip().replace("\n", " ").replace("|", "\\|")).append(" | ");
            sb.append("\n");
            if (r == 0) sb.append("|").repeat(" --- |", rows.getFirst().getCells().size()).append("\n");
        }
        return sb.toString();
    }

    //for testing
    private byte[] render(XSLFSlide slide, Dimension size) throws IOException {
        double scale = 1.5;
        BufferedImage img = new BufferedImage(
                (int) (size.width * scale), (int) (size.height * scale), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.scale(scale, scale);
        try {
            slide.draw(g);
        } catch (Exception e) {
            log.warn("Could not fully render slide {}: {}", slide.getSlideNumber(), e.getMessage());
        } finally {
            g.dispose();
        }
        return ImageUtils.toPng(img);
    }
}
