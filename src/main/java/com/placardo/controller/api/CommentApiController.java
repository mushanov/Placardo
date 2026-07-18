package com.placardo.controller.api;

import com.placardo.dto.CommentDto;
import com.placardo.dto.CommentForm;
import com.placardo.service.CommentService;
import com.placardo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Комментарии")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final UserService userService;

    @GetMapping("/ads/{adId}/comments")
    @Operation(summary = "Комментарии объявления")
    public List<CommentDto> list(@PathVariable Long adId) {
        return commentService.listByAd(adId);
    }

    @PostMapping("/ads/{adId}/comments")
    @Operation(summary = "Добавить комментарий (ajax, без перезагрузки страницы)")
    public CommentDto add(@PathVariable Long adId,
                          @Valid @RequestBody CommentForm form,
                          Authentication auth) {
        return commentService.add(adId, userService.getCurrentUser(auth), form.body());
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Удалить комментарий (автор или администратор)")
    public void delete(@PathVariable Long commentId, Authentication auth) {
        commentService.delete(commentId, userService.getCurrentUser(auth));
    }
}
