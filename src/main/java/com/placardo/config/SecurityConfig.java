package com.placardo.config;

import com.placardo.security.CustomOAuth2UserService;
import com.placardo.security.CustomOidcUserService;
import com.placardo.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity      // включает @PreAuthorize на методах
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final CustomOidcUserService oidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ВАЖНО: порядок правил имеет значение — первое совпавшее побеждает.
                // Формы создания/редактирования идут ДО общего разрешения на /ads/*
                .requestMatchers("/ads/new", "/ads/*/edit").authenticated()
                // публичная часть (страницы и статика)
                .requestMatchers("/", "/login", "/register",
                        "/css/**", "/js/**", "/img/**", "/uploads/**", "/favicon.svg",
                        "/error", "/error/**").permitAll()
                // каталог и просмотр объявлений: только чтение (GET)
                .requestMatchers(HttpMethod.GET, "/ads", "/ads/*").permitAll()
                // Swagger доступен без входа — удобно для проверки проекта
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // публичное чтение API
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/ads", "/api/v1/ads/*", "/api/v1/ads/*/comments").permitAll()
                // админка — только ADMIN (второй уровень защиты — @PreAuthorize на методах)
                .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                // всё остальное — только для вошедших
                .anyRequest().authenticated()
            )
            // Способ входа 1: классическая форма email + пароль
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error")
                .permitAll()
            )
            // Способ входа 2: OAuth2 (Google)
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService)      // провайдеры без openid
                        .oidcUserService(oidcUserService))   // Google (scope openid) идёт СЮДА
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?oauthError")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
            )
            // Неавторизованные ajax-запросы к API получают честный 401 (JSON-клиенту
            // бессмысленно отдавать redirect на HTML-страницу входа)
            .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")))
            .userDetailsService(userDetailsService);
        // CSRF-защита включена по умолчанию:
        // формы Thymeleaf добавляют токен сами, ajax берёт его из <meta> (см. main.js)
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
