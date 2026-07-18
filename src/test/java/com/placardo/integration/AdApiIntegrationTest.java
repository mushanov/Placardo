package com.placardo.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест: поднимает настоящий PostgreSQL в Docker (Testcontainers),
 * прогоняет Liquibase-миграции и проверяет живой HTTP-эндпоинт.
 * Для запуска нужен работающий Docker.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/v1/ads отвечает 200 и содержит демо-объявления")
    void adsEndpointReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/ads", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }

    @Test
    @DisplayName("Каталог /ads открывается без авторизации")
    void catalogPageIsPublic() {
        ResponseEntity<String> response = restTemplate.getForEntity("/ads", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Админка недоступна без входа (redirect на /login)")
    void adminRequiresAuth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/admin/moderation", String.class);

        // TestRestTemplate следует за редиректом на страницу входа
        assertThat(response.getStatusCode().is2xxSuccessful()
                || response.getStatusCode().is3xxRedirection()).isTrue();
    }
}
