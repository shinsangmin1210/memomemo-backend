package com.memomemo.domain.file.controller;

import com.memomemo.domain.file.dto.FileUploadResponse;
import com.memomemo.domain.file.service.FileService;
import com.memomemo.domain.message.entity.Attachment;
import com.memomemo.domain.message.repository.AttachmentRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final AttachmentRepository attachmentRepository;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam MultipartFile file) {
        return ResponseEntity.ok(fileService.upload(file));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        Resource resource = fileService.download(fileId);
        Attachment attachment = attachmentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("파일", fileId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, attachment.getMimeType())
                .body(resource);
    }
}
