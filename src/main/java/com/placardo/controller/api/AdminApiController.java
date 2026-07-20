package com.placardo.controller.api;

import com.placardo.dto.StatusForm;
import com.placardo.service.AdService;
import com.placardo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Администрирование", description = "Модерация объявлений и управление пользователями")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdService adService;
    private final UserService userService;

    @PatchMapping("/ads/{id}/status")
    @Operation(summary = "Сменить статус объявления (одобрить / отклонить / архивировать)")
    public void changeStatus(@PathVariable Long id, @Valid @RequestBody StatusForm form) {
        adService.changeStatus(id, form.status());
    }

    @PatchMapping("/users/{id}/ban")
    @Operation(summary = "Забанить или разбанить пользователя")
    public Map<String, Boolean> toggleBan(@PathVariable Long id) {
        return Map.of("banned", userService.toggleBan(id));
    }
}
