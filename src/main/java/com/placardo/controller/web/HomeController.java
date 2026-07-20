package com.placardo.controller.web;

import com.placardo.service.AdService;
import com.placardo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AdService adService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", categoryService.getTree());
        model.addAttribute("ads", adService.search(null, null, 0, 8));
        return "index";
    }
}
