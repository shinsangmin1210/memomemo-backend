package com.memomemo.domain.file.service;

import com.memomemo.domain.file.dto.FileUploadResponse;
import com.memomemo.domain.message.entity.Attachment;
import com.memomemo.domain.message.repository.AttachmentRepository;
import com.memomemo.global.config.FileStorageConfig;
import com.memomemo.global.exception.FileStorageException;
import com.memomemo.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageConfig fileStorageConfig;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public FileUploadResponse upload(MultipartFile file) {
        validate(file);

        String storagePath = store(file);

        Attachment attachment = attachmentRepository.save(
                Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .mimeType(file.getContentType())
                        .storagePath(storagePath)
                        .build()
        );

        return new FileUploadResponse(
                attachment.getId(),
                attachment.getFileName(),
                "/api/v1/files/" + attachment.getId(),
                attachment.getMimeType(),
                attachment.getFileSize()
        );
    }

    @Transactional(readOnly = true)
    public Resource download(Long fileId) {
        Attachment attachment = attachmentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("파일", fileId));
        try {
            Path path = Paths.get(attachment.getStoragePath());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("파일이 존재하지 않습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileStorageException("파일 경로가 올바르지 않습니다.");
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > fileStorageConfig.getMaxSize()) {
            throw new FileStorageException("파일 크기가 50MB를 초과합니다.");
        }
        if (!FileStorageConfig.ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new FileStorageException("허용되지 않는 파일 형식입니다: " + file.getContentType());
        }
    }

    private String store(MultipartFile file) {
        LocalDate today = LocalDate.now();
        Path dir = Paths.get(
                fileStorageConfig.getRootPath(),
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                String.format("%02d", today.getDayOfMonth())
        );

        try {
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return target.toString();
        } catch (IOException e) {
            throw new FileStorageException("파일 저장 중 오류가 발생했습니다.");
        }
    }
}
