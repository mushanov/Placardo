package com.placardo.service;

import com.placardo.entity.Category;
import com.placardo.exception.NotFoundException;
import com.placardo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** Дерево категорий кешируется в Caffeine: меняется редко, читается на каждой странице */
    @Cacheable("categoryTree")
    @Transactional(readOnly = true)
    public List<Category> getTree() {
        return categoryRepository.findTree();
    }

    @Transactional(readOnly = true)
    public Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена: " + id));
    }

    /** id категории вместе с id всех её подкатегорий (для фильтра каталога) */
    @Transactional(readOnly = true)
    public List<Long> getWithDescendantIds(Long categoryId) {
        Category root = getOrThrow(categoryId);
        List<Long> ids = new ArrayList<>();
        collect(root, ids);
        return ids;
    }

    private void collect(Category category, List<Long> ids) {
        ids.add(category.getId());
        category.getChildren().forEach(child -> collect(child, ids));
    }

    @CacheEvict(value = "categoryTree", allEntries = true)
    @Transactional
    public Category create(String name, String slug, Long parentId) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug уже занят: " + slug);
        }
        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .parent(parentId != null ? getOrThrow(parentId) : null)
                .build();
        return categoryRepository.save(category);
    }
}
