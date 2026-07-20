package com.placardo.controller.api;

import com.placardo.service.FavoriteService;
import com.placardo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ads/{adId}/favorite")
@Tag(name = "Избранное")
@RequiredArgsConstructor
public class FavoriteApiController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Добавить объявление в избранное")
    public Map<String, Boolean> add(@PathVariable Long adId, Authentication auth) {
        boolean favorite = favoriteService.add(adId, userService.getCurrentUser(auth));
        return Map.of("favorite", favorite);
    }

    @DeleteMapping
    @Operation(summary = "Убрать объявление из избранного")
    public Map<String, Boolean> remove(@PathVariable Long adId, Authentication auth) {
        boolean favorite = favoriteService.remove(adId, userService.getCurrentUser(auth));
        return Map.of("favorite", favorite);
    }
}
