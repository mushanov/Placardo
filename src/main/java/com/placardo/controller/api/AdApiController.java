package com.placardo.controller.api;

import com.placardo.dto.AdCardDto;
import com.placardo.dto.AdDto;
import com.placardo.dto.PageDto;
import com.placardo.service.AdService;
import com.placardo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ads")
@Tag(name = "Объявления", description = "Каталог объявлений: поиск, фильтры, пагинация")
@RequiredArgsConstructor
public class AdApiController {

    private final AdService adService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Список активных объявлений",
            description = "Поиск по тексту (q), фильтр по категории, пагинация. Используется кнопкой «Показать ещё»")
    public PageDto<AdCardDto> list(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) Long category,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size) {
        return PageDto.of(adService.search(q, category, page, Math.min(size, 50)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Одно объявление")
    public AdDto get(@PathVariable Long id, Authentication auth) {
        return adService.getForView(id, userService.getCurrentUser(auth));
    }
}
