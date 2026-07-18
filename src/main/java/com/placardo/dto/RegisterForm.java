package com.placardo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {

    @NotBlank(message = "Укажите имя")
    @Size(min = 2, max = 100, message = "Имя: от 2 до 100 символов")
    private String name;

    @NotBlank(message = "Укажите email")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Придумайте пароль")
    @Size(min = 6, max = 72, message = "Пароль: минимум 6 символов")
    private String password;
}
