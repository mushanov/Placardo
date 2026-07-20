package com.placardo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private ImageStorageService service;

    @BeforeEach
    void setUp() {
        service = new ImageStorageService(tempDir.toString());
        service.init();
    }

    @Test
    @DisplayName("JPEG сохраняется под случайным именем с расширением .jpg")
    void storesJpeg() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "images", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String filename = service.store(file);

        assertThat(filename).endsWith(".jpg").doesNotContain("photo");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    @DisplayName("Файлы недопустимого типа отклоняются")
    void rejectsWrongType() {
        MockMultipartFile file = new MockMultipartFile(
                "images", "malware.exe", "application/octet-stream", new byte[]{1});

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPEG");
    }

    @Test
    @DisplayName("Пустой файл отклоняется")
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "images", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
