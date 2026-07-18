package com.placardo.controller.web;

import com.placardo.entity.User;
import com.placardo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Кладёт текущего пользователя в модель КАЖДОЙ web-страницы
 * под именем "currentUser" — шапка показывает аватар и имя.
 * На REST API (/api/v1) не распространяется, чтобы не делать
 * лишний запрос к БД на каждый ajax-вызов.
 */
@ControllerAdvice(basePackages = "com.placardo.controller.web")
@RequiredArgsConstructor
public class CurrentUserAdvice {

    private final UserService userService;

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }
}
