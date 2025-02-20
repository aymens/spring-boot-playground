package io.playground.test.configuration.security;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import static org.mockito.Mockito.mock;

@Profile("test")
@Configuration
@ConditionalOnWebApplication
@Slf4j
public class SecurityMocksConfig {

    @Bean
    @Primary
    JwtAuthenticationConverter mockJwtAuthenticationConverter() {
        return mock(JwtAuthenticationConverter.class);
    }

    @Bean
    @Primary
    public OpenAPI mockOpenAPI() {
        return mock(OpenAPI.class);
    }

    @Bean
    @Primary
    JwtDecoder mockJwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    @Primary
    SwaggerUiOAuthProperties mockSwaggerUiOAuthProperties() {
        return mock(SwaggerUiOAuthProperties.class);
    }
}
