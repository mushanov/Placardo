package com.placardo.controller.web;

import com.placardo.dto.AdDto;
import com.placardo.dto.AdForm;
import com.placardo.entity.Ad;
import com.placardo.entity.User;
import com.placardo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdPageController {

    private final AdService adService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final FavoriteService favoriteService;
    private final UserService userService;

    /** Каталог с фильтрами и пагинацией */
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("categories", categoryService.getTree());
        model.addAttribute("ads", adService.search(q, category, page, 12));
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        return "ads/list";
    }

    /** Страница объявления */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        AdDto ad = adService.getForView(id, currentUser);
        model.addAttribute("ad", ad);
        model.addAttribute("comments", commentService.listByAd(id));
        model.addAttribute("isFavorite", favoriteService.isFavorite(id, currentUser));
        model.addAttribute("canManage", currentUser != null
                && adService.canManage(adService.getOrThrow(id), currentUser));
        return "ads/view";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("adForm", new AdForm());
        model.addAttribute("categories", categoryService.getTree());
        model.addAttribute("editing", false);
        return "ads/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("adForm") AdForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         Authentication auth, Model model, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getTree());
            model.addAttribute("editing", false);
            return "ads/form";
        }
        Ad ad = adService.create(form, userService.getCurrentUser(auth), images);
        redirect.addFlashAttribute("message",
                "Объявление отправлено на модерацию — оно появится в каталоге после проверки");
        return "redirect:/ads/" + ad.getId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@adPermissions.isOwner(#id, authentication)")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        AdDto ad = adService.getForView(id, userService.getCurrentUser(auth));
        AdForm form = new AdForm();
        form.setTitle(ad.title());
        form.setDescription(ad.description());
        form.setPrice(ad.price());
        form.setCity(ad.city());
        form.setCategoryId(ad.categoryId());
        model.addAttribute("adForm", form);
        model.addAttribute("categories", categoryService.getTree());
        model.addAttribute("editing", true);
        model.addAttribute("adId", id);
        model.addAttribute("existingImages", ad.images());
        return "ads/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("@adPermissions.isOwner(#id, authentication)")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("adForm") AdForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         Authentication auth, Model model, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getTree());
            model.addAttribute("editing", true);
            model.addAttribute("adId", id);
            return "ads/form";
        }
        adService.update(id, form, userService.getCurrentUser(auth), images);
        redirect.addFlashAttribute("message", "Изменения сохранены, объявление снова на модерации");
        return "redirect:/ads/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("@adPermissions.isOwner(#id, authentication)")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        adService.delete(id, userService.getCurrentUser(auth));
        redirect.addFlashAttribute("message", "Объявление удалено");
        return "redirect:/profile/ads";
    }
}
