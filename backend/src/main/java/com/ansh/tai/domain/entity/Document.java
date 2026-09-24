package com.ansh.tai.domain.entity;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String s3Key;

    @Column(name = "status", nullable = false)
    private DocStatus status;

    public Document() {
    }

    public Document(UUID id, UUID courseId, String fileName, String fileType, String s3Key, DocStatus status) {
        this.id = id;
        this.courseId = courseId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.s3Key = s3Key;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public DocStatus getStatus() {
        return status;
    }

    public void setStatus(DocStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", s3Key='" + s3Key + '\'' +
                ", status=" + status +
                '}';
    }
}
