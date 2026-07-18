package com.placardo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Карточка объявления в списках (главная, каталог, избранное) */
public record AdCardDto(
        Long id,
        String title,
        BigDecimal price,
        String city,
        LocalDateTime createdAt,
        String coverImage
) {
}
