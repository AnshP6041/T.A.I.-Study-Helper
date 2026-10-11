package com.ansh.tai.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String ext) {
        super("Unsupported file type '" + ext + "'. Upload a .pdf or .pptx file.");
    }
}
