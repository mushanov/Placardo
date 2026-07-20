package com.placardo.security;

import com.placardo.entity.*;
import com.placardo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Обработка входа через Google:
 * при первом входе создаём пользователя в нашей БД, дальше — узнаём его по email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);   // запрос к Google за профилем

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("no_email"),
                    "Google не вернул email");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Первый вход через Google: создаём пользователя {}", email);
                    return userRepository.save(User.builder()
                            .email(email)
                            .name(name != null ? name : email)
                            .provider(AuthProvider.GOOGLE)
                            .role(Role.USER)
                            .status(UserStatus.ACTIVE)
                            .build());
                });

        if (user.getStatus() == UserStatus.BANNED) {
            throw new OAuth2AuthenticationException(new OAuth2Error("banned"),
                    "Аккаунт заблокирован");
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oauth2User.getAttributes(),
                "email");
    }
}
