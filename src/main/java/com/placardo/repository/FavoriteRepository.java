package com.placardo.repository;

import com.placardo.entity.Ad;
import com.placardo.entity.Favorite;
import com.placardo.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    @Query("select f.ad from Favorite f where f.user.id = :userId order by f.createdAt desc")
    List<Ad> findAdsByUserId(@Param("userId") Long userId);

    long countByIdAdId(Long adId);
}
