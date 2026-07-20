package com.placardo.repository;

import com.placardo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Корневые категории вместе с детьми одним запросом (без N+1) */
    @Query("select distinct c from Category c left join fetch c.children where c.parent is null order by c.name")
    List<Category> findTree();

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
