package com.placardo.repository;

import com.placardo.entity.Ad;
import com.placardo.entity.AdStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/** Динамические условия поиска для каталога объявлений */
public final class AdSpecifications {

    private AdSpecifications() {
    }

    public static Specification<Ad> hasStatus(AdStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Ad> inCategories(List<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Ad> textLike(String q) {
        return (root, query, cb) -> {
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
