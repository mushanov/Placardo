package com.placardo.controller.web;

import com.placardo.dto.RegisterForm;
import com.placardo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult, RedirectAttributes redirect) {
        if (!bindingResult.hasErrors()) {
            try {
                userService.register(form);
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("email", "duplicate", e.getMessage());
            }
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        redirect.addFlashAttribute("registered", true);
        return "redirect:/login";
    }
}
