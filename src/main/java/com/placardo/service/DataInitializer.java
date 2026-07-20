package com.placardo.service;

import com.placardo.entity.*;
import com.placardo.repository.AdRepository;
import com.placardo.repository.CategoryRepository;
import com.placardo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * При первом запуске на пустой базе создаёт админа, демо-пользователя
 * и несколько объявлений — чтобы проверяющий сразу видел живой сайт.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = userRepository.save(User.builder()
                .email("admin@placardo.local")
                .passwordHash(passwordEncoder.encode("admin123"))
                .name("Администратор")
                .provider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        User demo = userRepository.save(User.builder()
                .email("demo@placardo.local")
                .passwordHash(passwordEncoder.encode("demo123"))
                .name("Андрей К.")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build());

        createAd(demo, "laptops", "Ноутбук Lenovo ThinkPad T14 Gen 3",
                "Отличное состояние, куплен год назад. Ryzen 7 PRO, 16 ГБ, SSD 512. Батарея держит 8 часов.",
                new BigDecimal("45000"), "Москва", AdStatus.ACTIVE);
        createAd(demo, "bikes", "Велосипед Merida Big.Nine 29\"",
                "Рама L, обслужен в этом сезоне, заменены колодки и цепь. Пробег небольшой, катался по выходным.",
                new BigDecimal("18500"), "Казань", AdStatus.ACTIVE);
        createAd(demo, "pets", "Котята в добрые руки",
                "Два рыжих котёнка, 2 месяца, к лотку приучены. Отдаём ответственным хозяевам.",
                null, "Санкт-Петербург", AdStatus.ACTIVE);
        createAd(demo, "phones", "iPhone 13, 128 ГБ",
                "Состояние хорошее, аккумулятор 87%. Комплект: коробка, кабель. Торг у капота.",
                new BigDecimal("32000"), "Москва", AdStatus.PENDING);

        log.info("=== Демо-данные созданы ===");
        log.info("Админ:        admin@placardo.local / admin123");
        log.info("Пользователь: demo@placardo.local  / demo123");
    }

    private void createAd(User user, String categorySlug, String title, String description,
                          BigDecimal price, String city, AdStatus status) {
        Category category = categoryRepository.findBySlug(categorySlug).orElseThrow();
        adRepository.save(Ad.builder()
                .user(user)
                .category(category)
                .title(title)
                .description(description)
                .price(price)
                .city(city)
                .status(status)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build());
    }
}
