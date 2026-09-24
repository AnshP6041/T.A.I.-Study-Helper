package com.ansh.tai.mapper;

import com.ansh.tai.domain.CreateDocumentRequest;
import com.ansh.tai.domain.dto.*;
import com.ansh.tai.domain.entity.Document;

public interface DocumentMapper {

    CreateDocumentRequest fromDto(CreateDocumentRequestDto dto);
    DocumentDto toDto(Document doc);
}
