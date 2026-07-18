package com.placardo.repository;

import com.placardo.entity.Ad;
import com.placardo.entity.AdStatus;
import com.placardo.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long>, JpaSpecificationExecutor<Ad> {

    List<Ad> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Ad> findByStatusOrderByCreatedAtAsc(AdStatus status);

    long countByStatus(AdStatus status);

    /** Автоархивация просроченных объявлений (вызывается по расписанию) */
    @Modifying
    @Query("update Ad a set a.status = :archived where a.status = :active and a.expiresAt < :now")
    int archiveExpired(@Param("active") AdStatus active,
                       @Param("archived") AdStatus archived,
                       @Param("now") LocalDateTime now);
}
