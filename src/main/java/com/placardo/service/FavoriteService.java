package com.placardo.service;

import com.placardo.dto.AdCardDto;
import com.placardo.entity.*;
import com.placardo.mapper.AdMapper;
import com.placardo.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AdService adService;
    private final AdMapper adMapper;

    @Transactional
    public boolean add(Long adId, User user) {
        FavoriteId id = new FavoriteId(user.getId(), adId);
        if (!favoriteRepository.existsById(id)) {
            Ad ad = adService.getOrThrow(adId);
            favoriteRepository.save(Favorite.builder().id(id).user(user).ad(ad).build());
        }
        return true;
    }

    @Transactional
    public boolean remove(Long adId, User user) {
        favoriteRepository.findById(new FavoriteId(user.getId(), adId))
                .ifPresent(favoriteRepository::delete);
        return false;
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long adId, User user) {
        return user != null && favoriteRepository.existsById(new FavoriteId(user.getId(), adId));
    }

    @Transactional(readOnly = true)
    public List<AdCardDto> getFavorites(User user) {
        return adMapper.toCards(favoriteRepository.findAdsByUserId(user.getId()));
    }
}
