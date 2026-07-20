package com.placardo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Форма создания/редактирования объявления (Thymeleaf) */
@Getter
@Setter
public class AdForm {

    @NotBlank(message = "Укажите заголовок")
    @Size(min = 5, max = 150, message = "Заголовок: от 5 до 150 символов")
    private String title;

    @NotBlank(message = "Добавьте описание")
    @Size(min = 10, max = 5000, message = "Описание: от 10 до 5000 символов")
    private String description;

    @DecimalMin(value = "0", message = "Цена не может быть отрицательной")
    @Digits(integer = 10, fraction = 2, message = "Некорректная цена")
    private BigDecimal price;

    @Size(max = 100)
    private String city;

    @NotNull(message = "Выберите категорию")
    private Long categoryId;
}
