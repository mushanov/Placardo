package com.placardo.controller.web;

import com.placardo.entity.User;
import com.placardo.service.AdService;
import com.placardo.service.FavoriteService;
import com.placardo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AdService adService;
    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping("/ads")
    public String myAds(Authentication auth, Model model) {
        User user = userService.getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("ads", adService.findByAuthor(user));
        return "profile/my-ads";
    }

    @GetMapping("/favorites")
    public String favorites(Authentication auth, Model model) {
        User user = userService.getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("ads", favoriteService.getFavorites(user));
        return "profile/favorites";
    }

    @GetMapping("/settings")
    public String settings(Authentication auth, Model model) {
        model.addAttribute("user", userService.getCurrentUser(auth));
        return "profile/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam String name, Authentication auth,
                                 RedirectAttributes redirect) {
        User user = userService.getCurrentUser(auth);
        if (name != null && name.trim().length() >= 2) {
            userService.updateName(user.getId(), name.trim());
            redirect.addFlashAttribute("message", "Имя обновлено");
        }
        return "redirect:/profile/settings";
    }
}
