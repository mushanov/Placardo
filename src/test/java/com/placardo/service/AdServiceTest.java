package com.placardo.service;

import com.placardo.dto.AdForm;
import com.placardo.entity.*;
import com.placardo.mapper.AdMapper;
import com.placardo.repository.AdImageRepository;
import com.placardo.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Юнит-тесты бизнес-логики объявлений (Mockito, без Spring-контекста и БД) */
@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock private AdRepository adRepository;
    @Mock private AdImageRepository adImageRepository;
    @Mock private AdMapper adMapper;
    @Mock private CategoryService categoryService;
    @Mock private ImageStorageService imageStorage;

    @InjectMocks
    private AdService adService;

    private User author;
    private Category category;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adService, "adLifetimeDays", 30);
        author = User.builder().id(1L).email("demo@placardo.local").name("Демо")
                .role(Role.USER).status(UserStatus.ACTIVE).provider(AuthProvider.LOCAL).build();
        category = Category.builder().id(10L).name("Ноутбуки").slug("laptops").build();
    }

    @Test
    @DisplayName("Новое объявление уходит на модерацию со сроком жизни 30 дней")
    void createSetsPendingStatusAndExpiry() {
        AdForm form = new AdForm();
        form.setTitle("Ноутбук ThinkPad");
        form.setDescription("Отличное состояние, полный комплект");
        form.setPrice(new BigDecimal("45000"));
        form.setCategoryId(10L);

        when(categoryService.getOrThrow(10L)).thenReturn(category);
        when(adRepository.save(any(Ad.class))).thenAnswer(inv -> inv.getArgument(0));

        adService.create(form, author, null);

        ArgumentCaptor<Ad> captor = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(captor.capture());
        Ad saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(AdStatus.PENDING);
        assertThat(saved.getUser()).isEqualTo(author);
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(29));
    }

    @Test
    @DisplayName("Чужое объявление нельзя удалить")
    void deleteByStrangerIsForbidden() {
        User stranger = User.builder().id(2L).role(Role.USER).build();
        Ad ad = Ad.builder().id(5L).user(author).status(AdStatus.ACTIVE).build();
        when(adRepository.findById(5L)).thenReturn(Optional.of(ad));

        assertThatThrownBy(() -> adService.delete(5L, stranger))
                .isInstanceOf(AccessDeniedException.class);
        verify(adRepository, never()).delete(any(Ad.class));
    }

    @Test
    @DisplayName("Администратор может управлять любым объявлением")
    void adminCanManageAnyAd() {
        User admin = User.builder().id(99L).role(Role.ADMIN).build();
        Ad ad = Ad.builder().id(5L).user(author).build();

        assertThat(adService.canManage(ad, admin)).isTrue();
        assertThat(adService.canManage(ad, author)).isTrue();
        assertThat(adService.canManage(ad, User.builder().id(2L).role(Role.USER).build())).isFalse();
    }

    @Test
    @DisplayName("При одобрении модератором объявление становится активным и получает новый срок")
    void approveActivatesAd() {
        Ad ad = Ad.builder().id(7L).user(author).status(AdStatus.PENDING).build();
        when(adRepository.findById(7L)).thenReturn(Optional.of(ad));

        adService.changeStatus(7L, AdStatus.ACTIVE);

        assertThat(ad.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(ad.getExpiresAt()).isNotNull();
    }
}
