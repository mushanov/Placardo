package com.placardo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI placardoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Placardo API")
                .description("REST API доски объявлений Placardo: каталог, избранное, комментарии, модерация")
                .version("1.0.0"));
    }

    /** В Swagger попадает только REST-часть (/api/**), web-контроллеры Thymeleaf не документируем */
    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("placardo-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
