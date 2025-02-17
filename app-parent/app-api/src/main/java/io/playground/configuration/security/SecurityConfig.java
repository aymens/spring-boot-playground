package io.playground.configuration.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Configuration
@Import({OAuth2AuthorizationServerProperties.class})
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String OAUTH_SCHEME_NAME = "oauth2";

    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    private final OAuth2AuthorizationServerProperties oAuth2AuthorizationServerProperties;
    private final SwaggerUiOAuthProperties swaggerUiOAuthProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
//                        .jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())) //TODO ia why that & not this?
                );
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri());
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                                // Define security scheme named "oauth2"
                                .addSecuritySchemes(OAUTH_SCHEME_NAME, new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl(oAuth2AuthorizationServerProperties.getEndpoint().getAuthorizationUri())
                                                        .tokenUrl(oAuth2AuthorizationServerProperties.getEndpoint().getTokenUri())
                                                        .scopes(new Scopes() {{
                                                            emptyIfNull(swaggerUiOAuthProperties.getScopes())
                                                                    .forEach(scope -> addString(scope, ""));
                                                        }}))
                                        )
                                )
                        // Apply this security scheme globally
                ).addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME));
    }
}