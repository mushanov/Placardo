package com.placardo.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Хранение фотографий на локальном диске.
 * В БД хранится только имя файла; сам файл лежит в app.upload-dir.
 */
@Slf4j
@Service
public class ImageStorageService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadDir;

    public ImageStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Каталог для фотографий: {}", uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось создать каталог " + uploadDir, e);
        }
    }

    /** Сохраняет файл под случайным именем и возвращает это имя */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Пустой файл");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Файл больше 5 МБ");
        }
        String ext = ALLOWED_TYPES.get(file.getContentType());
        if (ext == null) {
            throw new IllegalArgumentException("Допустимы только JPEG, PNG и WebP");
        }
        // Никогда не используем оригинальное имя файла: это и коллизии, и дыра в безопасности
        String filename = UUID.randomUUID() + ext;
        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить файл", e);
        }
        log.debug("Сохранено фото {}", filename);
        return filename;
    }

    public void delete(String filename) {
        try {
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException e) {
            log.warn("Не удалось удалить файл {}", filename, e);
        }
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}
