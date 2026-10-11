package com.ansh.tai.controller;

import com.ansh.tai.domain.dto.DuplicateErrorDto;
import com.ansh.tai.domain.dto.ErrorDto;
import com.ansh.tai.exception.DuplicateDocumentException;
import com.ansh.tai.exception.UnsupportedFileTypeException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex){
        String errorMessage = ex.getBindingResult().getFieldErrors().stream().findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation Failed");

        ErrorDto errorDto = new ErrorDto(errorMessage);
        return new ResponseEntity<>(errorDto,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorDto> handleUnsupportedType(UnsupportedFileTypeException ex) {
        return new ResponseEntity<>(new ErrorDto(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<DuplicateErrorDto> handleDuplicate(DuplicateDocumentException ex) {
        return new ResponseEntity<>(new DuplicateErrorDto(ex.getMessage(), ex.getExistingId()), HttpStatus.CONFLICT);
    }
}
