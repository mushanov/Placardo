package com.placardo.dto;

import java.time.LocalDateTime;

/** Пользователь в админке */
public record UserDto(
        Long id,
        String email,
        String name,
        String provider,
        String role,
        String status,
        LocalDateTime createdAt
) {
}
