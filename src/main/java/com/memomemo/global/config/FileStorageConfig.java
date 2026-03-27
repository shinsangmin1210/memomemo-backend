package com.memomemo.global.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Getter
@Configuration
public class FileStorageConfig {

    @Value("${file.storage.root-path:./data/files}")
    private String rootPath;

    @Value("${file.storage.max-size:52428800}") // 50MB
    private long maxSize;

    public static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "text/plain", "text/markdown",
            "application/pdf", "application/zip",
            "application/json",
            "text/x-java-source", "text/javascript", "text/typescript",
            "text/x-python", "text/x-go", "text/x-sql"
    );

    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(rootPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
