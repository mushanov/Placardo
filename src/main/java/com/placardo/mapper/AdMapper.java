package com.placardo.mapper;

import com.placardo.dto.AdCardDto;
import com.placardo.dto.AdDto;
import com.placardo.dto.ImageDto;
import com.placardo.entity.Ad;
import com.placardo.entity.AdImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdMapper {

    @Mapping(target = "coverImage", expression = "java(coverUrl(ad))")
    AdCardDto toCard(Ad ad);

    List<AdCardDto> toCards(List<Ad> ads);

    @Mapping(target = "statusLabel", expression = "java(ad.getStatus().getLabel())")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "authorId", source = "user.id")
    @Mapping(target = "authorName", source = "user.name")
    AdDto toDto(Ad ad);

    default ImageDto toImageDto(AdImage image) {
        return new ImageDto(image.getId(), "/uploads/" + image.getFilePath());
    }

    default String coverUrl(Ad ad) {
        if (ad.getImages() == null || ad.getImages().isEmpty()) {
            return null;
        }
        return "/uploads/" + ad.getImages().get(0).getFilePath();
    }
}
