package com.placardo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Полное объявление для страницы просмотра и API */
public record AdDto(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String city,
        String status,
        String statusLabel,
        LocalDateTime createdAt,
        Long categoryId,
        String categoryName,
        Long authorId,
        String authorName,
        List<ImageDto> images
) {
}
