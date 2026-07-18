package com.placardo.service;

import com.placardo.dto.RegisterForm;
import com.placardo.entity.*;
import com.placardo.exception.NotFoundException;
import com.placardo.repository.UserRepository;
import com.placardo.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Email уже зарегистрирован");
        }
        User user = User.builder()
                .email(form.getEmail())
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .name(form.getName())
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);
        log.info("Зарегистрирован пользователь {}", user.getEmail());
        return user;
    }

    @Transactional(readOnly = true)
    public User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    /**
     * Достаёт доменного пользователя из Authentication.
     * Работает для обоих способов входа: форма (AppUserDetails) и Google (OAuth2User/OidcUser).
     * Страховка: если OAuth2-пользователь вошёл, но в нашей БД его почему-то нет —
     * создаём запись на лету, чтобы сайт не падал с NULL user_id.
     */
    @Transactional
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails details) {
            return userRepository.findByEmail(details.getUsername()).orElse(null);
        }
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email == null) {
                return null;
            }
            return userRepository.findByEmail(email).orElseGet(() -> {
                String name = oauth2User.getAttribute("name");
                log.warn("OAuth2-пользователь {} отсутствовал в БД — создаём запись", email);
                return userRepository.save(User.builder()
                        .email(email)
                        .name(name != null ? name : email)
                        .provider(AuthProvider.GOOGLE)
                        .role(Role.USER)
                        .status(UserStatus.ACTIVE)
                        .build());
            });
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** Бан/разбан пользователя администратором. Возвращает true, если пользователь теперь забанен */
    @Transactional
    public boolean toggleBan(Long userId) {
        User user = getOrThrow(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Нельзя забанить администратора");
        }
        user.setStatus(user.getStatus() == UserStatus.BANNED ? UserStatus.ACTIVE : UserStatus.BANNED);
        log.info("Пользователь {} теперь {}", user.getEmail(), user.getStatus());
        return user.getStatus() == UserStatus.BANNED;
    }

    @Transactional
    public void updateName(Long userId, String name) {
        getOrThrow(userId).setName(name);
    }
}
