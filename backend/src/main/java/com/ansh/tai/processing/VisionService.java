package com.ansh.tai.processing;

import java.util.List;

public interface VisionService {
    String describe(byte[] png, String extractedText);
    String transcribe(byte[] png, List<byte[]> equationImages, String extractedText, boolean hasTables);
}
