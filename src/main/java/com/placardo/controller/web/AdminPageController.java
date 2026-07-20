package com.placardo.controller.web;

import com.placardo.mapper.UserMapper;
import com.placardo.service.AdService;
import com.placardo.service.CategoryService;
import com.placardo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")     // второй уровень защиты в дополнение к SecurityFilterChain
@RequiredArgsConstructor
public class AdminPageController {

    private final AdService adService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final CategoryService categoryService;

    @GetMapping
    public String index() {
        return "redirect:/admin/moderation";
    }

    @GetMapping("/moderation")
    public String moderation(Model model) {
        model.addAttribute("ads", adService.findPendingModeration());
        return "admin/moderation";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userMapper.toDtos(userService.findAll()));
        return "admin/users";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getTree());
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam String slug,
                                 @RequestParam(required = false) Long parentId,
                                 RedirectAttributes redirect) {
        try {
            categoryService.create(name.trim(), slug.trim().toLowerCase(), parentId);
            redirect.addFlashAttribute("message", "Категория создана");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
