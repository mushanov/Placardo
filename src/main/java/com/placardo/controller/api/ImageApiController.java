package com.placardo.controller.api;

import com.placardo.service.AdService;
import com.placardo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Фотографии")
@RequiredArgsConstructor
public class ImageApiController {

    private final AdService adService;
    private final UserService userService;

    @PostMapping("/ads/{adId}/images")
    @Operation(summary = "Загрузить фотографии к объявлению (multipart)")
    public void upload(@PathVariable Long adId,
                       @RequestParam("images") List<MultipartFile> images,
                       Authentication auth) {
        adService.addImages(adId, userService.getCurrentUser(auth), images);
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Удалить фотографию (только автор объявления)")
    public void delete(@PathVariable Long imageId, Authentication auth) {
        adService.deleteImage(imageId, userService.getCurrentUser(auth));
    }
}
