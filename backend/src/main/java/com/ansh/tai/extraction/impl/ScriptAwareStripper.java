package com.ansh.tai.extraction.impl;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

class ScriptAwareStripper extends PDFTextStripper {

    @Override
    protected void writeString(String text, List<TextPosition> glyphs) throws IOException {
        if (glyphs.size() < 2) { super.writeString(text, glyphs); return; }

        TextPosition normal = glyphs.stream()
                .max(Comparator.comparingDouble(TextPosition::getHeightDir)).get();
        float normalHeight = normal.getHeightDir();
        float baseline = normal.getYDirAdj();

        StringBuilder out = new StringBuilder();
        String mode = "";
        for (TextPosition g : glyphs) {
            boolean small = g.getHeightDir() < 0.8f * normalHeight;
            float dy = g.getYDirAdj() - baseline;
            String m = !small ? "" : dy < -1f ? "^" : dy > 1f ? "_" : "";
            if (!m.equals(mode)) {
                if (!mode.isEmpty()) out.append(')');
                if (!m.isEmpty())    out.append(m).append('(');
                mode = m;
            }
            out.append(g.getUnicode());
        }
        if (!mode.isEmpty()) out.append(')');

        writeString(out.toString().replaceAll("([\\^_])\\((.)\\)", "$1$2"));
    }
}