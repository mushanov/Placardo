package com.placardo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Ошибки в web-контроллерах превращаем в красивые страницы */
@Slf4j
@ControllerAdvice(basePackages = "com.placardo.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NotFoundException e) {
        log.debug("404: {}", e.getMessage());
        return "error/404";
    }
}
