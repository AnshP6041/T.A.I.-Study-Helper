package com.ansh.tai.controller;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.dto.CreateDocumentRequestDto;
import com.ansh.tai.domain.dto.DocumentDto;
import com.ansh.tai.domain.entity.Document;
import com.ansh.tai.mapper.DocumentMapper;
import com.ansh.tai.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/v1/documents")
public class DocumentController {

    private final DocumentMapper map;
    private final DocumentService serve;

    public DocumentController(DocumentMapper map, DocumentService serve) {
        this.map = map;
        this.serve = serve;
    }

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(
            @Valid @RequestBody CreateDocumentRequestDto reqDto
    ) {
        CreateDocumentRequest req = map.fromDto(reqDto);
        Document doc = serve.createDoc(req);
        DocumentDto dto = map.toDto(doc);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> listDocs() {
        List<Document> docs = serve.listDocs();
        List<DocumentDto> docDtos = docs.stream().map(map::toDto).toList();
        return ResponseEntity.ok(docDtos);
    }

    @DeleteMapping(path = "/{docId}")
    public ResponseEntity<Void> deleteDoc(
            @PathVariable UUID docId
    ){
        serve.deleteDoc(docId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
