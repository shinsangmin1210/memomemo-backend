package com.memomemo.domain.file.dto;

public record FileUploadResponse(
        Long fileId,
        String fileName,
        String fileUrl,
        String mimeType,
        Long fileSize
) {}
