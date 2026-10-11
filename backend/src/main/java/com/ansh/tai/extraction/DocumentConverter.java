package com.ansh.tai.extraction;

import java.io.IOException;

public interface DocumentConverter {
    byte[] toPdf(byte[] file, String extension) throws IOException, InterruptedException;
}
