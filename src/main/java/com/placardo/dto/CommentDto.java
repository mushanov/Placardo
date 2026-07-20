package com.placardo.dto;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        Long authorId,
        String authorName,
        String body,
        LocalDateTime createdAt
) {
}
