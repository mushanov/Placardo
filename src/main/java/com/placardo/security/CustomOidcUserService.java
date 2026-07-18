package com.placardo.security;

import com.placardo.entity.*;
import com.placardo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ВАЖНО: у Google в scope есть "openid", поэтому Spring Security использует
 * OIDC-вход, а не обычный OAuth2. Для OIDC вызывается ЭТОТ сервис
 * (а CustomOAuth2UserService — только для провайдеров без openid).
 * Логика та же: при первом входе создаём пользователя в нашей БД.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);   // запрос к Google за профилем

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("no_email"),
                    "Google не вернул email");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Первый вход через Google (OIDC): создаём пользователя {}", email);
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

        return new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email");
    }
}
