package com.placardo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Тело комментария в ajax-запросе */
public record CommentForm(
        @NotBlank(message = "Комментарий не может быть пустым")
        @Size(max = 2000, message = "Комментарий: до 2000 символов")
        String body
) {
}
