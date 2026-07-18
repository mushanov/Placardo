package com.placardo.service;

import com.placardo.dto.AdCardDto;
import com.placardo.dto.AdDto;
import com.placardo.dto.AdForm;
import com.placardo.entity.*;
import com.placardo.exception.NotFoundException;
import com.placardo.mapper.AdMapper;
import com.placardo.repository.AdImageRepository;
import com.placardo.repository.AdRepository;
import com.placardo.repository.AdSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final AdImageRepository adImageRepository;
    private final AdMapper adMapper;
    private final CategoryService categoryService;
    private final ImageStorageService imageStorage;

    @Value("${app.ad-lifetime-days}")
    private int adLifetimeDays;

    /** Каталог: только активные объявления + фильтры категории и текстового поиска */
    @Transactional(readOnly = true)
    public Page<AdCardDto> search(String q, Long categoryId, int page, int size) {
        Specification<Ad> spec = AdSpecifications.hasStatus(AdStatus.ACTIVE);
        if (categoryId != null) {
            spec = spec.and(AdSpecifications.inCategories(categoryService.getWithDescendantIds(categoryId)));
        }
        if (StringUtils.hasText(q)) {
            spec = spec.and(AdSpecifications.textLike(q.trim()));
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return adRepository.findAll(spec, pageRequest).map(adMapper::toCard);
    }

    @Transactional(readOnly = true)
    public Ad getOrThrow(Long id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено: " + id));
    }

    /**
     * Объявление для страницы просмотра.
     * Неактивные видят только автор и администратор.
     */
    @Transactional(readOnly = true)
    public AdDto getForView(Long id, User currentUser) {
        Ad ad = getOrThrow(id);
        if (ad.getStatus() != AdStatus.ACTIVE && !canManage(ad, currentUser)) {
            throw new NotFoundException("Объявление не найдено: " + id);
        }
        return adMapper.toDto(ad);
    }

    @Transactional
    public Ad create(AdForm form, User author, List<MultipartFile> images) {
        Ad ad = Ad.builder()
                .user(author)
                .category(categoryService.getOrThrow(form.getCategoryId()))
                .title(form.getTitle())
                .description(form.getDescription())
                .price(form.getPrice())
                .city(form.getCity())
                .status(AdStatus.PENDING)                       // новое объявление ждёт модерацию
                .expiresAt(LocalDateTime.now().plusDays(adLifetimeDays))
                .build();
        attachImages(ad, images);
        ad = adRepository.save(ad);
        log.info("Создано объявление #{} пользователем {}", ad.getId(), author.getEmail());
        return ad;
    }

    @Transactional
    public void update(Long id, AdForm form, User currentUser, List<MultipartFile> images) {
        Ad ad = getOrThrow(id);
        requireCanManage(ad, currentUser);
        ad.setTitle(form.getTitle());
        ad.setDescription(form.getDescription());
        ad.setPrice(form.getPrice());
        ad.setCity(form.getCity());
        ad.setCategory(categoryService.getOrThrow(form.getCategoryId()));
        ad.setStatus(AdStatus.PENDING);                          // после правок — снова на модерацию
        attachImages(ad, images);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Ad ad = getOrThrow(id);
        requireCanManage(ad, currentUser);
        ad.getImages().forEach(img -> imageStorage.delete(img.getFilePath()));
        adRepository.delete(ad);
        log.info("Удалено объявление #{}", id);
    }

    @Transactional
    public void deleteImage(Long imageId, User currentUser) {
        AdImage image = adImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Фото не найдено: " + imageId));
        requireCanManage(image.getAd(), currentUser);
        image.getAd().getImages().remove(image);                 // orphanRemoval удалит строку из БД
        imageStorage.delete(image.getFilePath());
    }

    @Transactional
    public void addImages(Long adId, User currentUser, List<MultipartFile> images) {
        Ad ad = getOrThrow(adId);
        requireCanManage(ad, currentUser);
        attachImages(ad, images);
    }

    /** Модерация (только ADMIN — проверяется в контроллере через @PreAuthorize) */
    @Transactional
    public void changeStatus(Long id, AdStatus status) {
        Ad ad = getOrThrow(id);
        ad.setStatus(status);
        if (status == AdStatus.ACTIVE) {
            ad.setExpiresAt(LocalDateTime.now().plusDays(adLifetimeDays));
        }
        log.info("Объявление #{} переведено в статус {}", id, status);
    }

    @Transactional(readOnly = true)
    public List<Ad> findPendingModeration() {
        return adRepository.findByStatusOrderByCreatedAtAsc(AdStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Ad> findByAuthor(User user) {
        return adRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /** Каждый час переводим просроченные активные объявления в архив */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void archiveExpired() {
        int archived = adRepository.archiveExpired(AdStatus.ACTIVE, AdStatus.ARCHIVED, LocalDateTime.now());
        if (archived > 0) {
            log.info("Автоархивация: {} объявлений отправлено в архив", archived);
        }
    }

    public boolean canManage(Ad ad, User user) {
        return user != null && (user.getRole() == Role.ADMIN || ad.getUser().getId().equals(user.getId()));
    }

    private void requireCanManage(Ad ad, User user) {
        if (!canManage(ad, user)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Объявление можно менять только его автору");
        }
    }

    private void attachImages(Ad ad, List<MultipartFile> images) {
        if (images == null) {
            return;
        }
        int order = ad.getImages().size();
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (order >= 10) {
                throw new IllegalArgumentException("Не больше 10 фотографий на объявление");
            }
            String filename = imageStorage.store(file);
            ad.getImages().add(AdImage.builder().ad(ad).filePath(filename).sortOrder(order++).build());
        }
    }
}
