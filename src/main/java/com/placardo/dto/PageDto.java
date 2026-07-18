package com.placardo.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Стабильная обёртка страницы для JSON-ответов API */
public record PageDto<T>(
        List<T> content,
        int page,
        int totalPages,
        long totalElements,
        boolean last
) {
    public static <T> PageDto<T> of(Page<T> page) {
        return new PageDto<>(page.getContent(), page.getNumber(),
                page.getTotalPages(), page.getTotalElements(), page.isLast());
    }
}
