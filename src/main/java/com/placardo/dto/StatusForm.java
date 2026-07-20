package com.placardo.dto;

import com.placardo.entity.AdStatus;
import jakarta.validation.constraints.NotNull;

/** Смена статуса объявления модератором */
public record StatusForm(@NotNull AdStatus status) {
}
