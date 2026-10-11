package com.ansh.tai.extraction.impl;

import com.ansh.tai.extraction.DocumentConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class LibreOfficeConverter implements DocumentConverter {
    private final String soffice;

    public LibreOfficeConverter(@Value("${libreoffice.path}") String soffice) {
        this.soffice = soffice;
    }

    @Override
    public byte[] toPdf(byte[] file, String ext) throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("convert-");
        Path profile = Files.createTempDirectory("lo-profile-");
        try {
            Path in = dir.resolve("input." + ext);
            Files.write(in, file);
            Path log = dir.resolve("libreoffice.log");

            ProcessBuilder pb = new ProcessBuilder(
                    soffice, "--headless", "--norestore",
                    "-env:UserInstallation=" + profile.toUri(),
                    "--convert-to", "pdf", "--outdir", dir.toString(), in.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(log.toFile());
            try (Process p = pb.start()) {
                if (!p.waitFor(120, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                    throw new IOException("LibreOffice timed out after 120s");
                }
                Path out = dir.resolve("input.pdf");
                if (p.exitValue() != 0 || !Files.exists(out)) {
                    throw new IOException("LibreOffice conversion failed: " + Files.readString(log));
                }
                return Files.readAllBytes(out);
            }
        } finally {
            FileSystemUtils.deleteRecursively(dir);
            FileSystemUtils.deleteRecursively(profile);
        }
    }
}
