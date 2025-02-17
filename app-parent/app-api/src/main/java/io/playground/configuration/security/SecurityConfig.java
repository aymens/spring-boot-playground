package io.playground.configuration.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Configuration
@Import({OAuth2AuthorizationServerProperties.class})
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String OAUTH_SCHEME_NAME = "oauth2";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/*/public/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        return JwtDecoders.fromIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri());
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new KeycloakJwtGrantedAuthoritiesConverter()
                        .setAuthorityPrefix("ROLE_"));
        return jwtAuthenticationConverter;
    }

//    @Bean
//    JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
//        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
//
//        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
//
//        return jwtAuthenticationConverter;
//    }

    @Bean
    public OpenAPI customOpenAPI(OAuth2ResourceServerProperties oAuth2ResourceServerProperties,
                                 OAuth2AuthorizationServerProperties oAuth2AuthorizationServerProperties,
                                 SwaggerUiOAuthProperties swaggerUiOAuthProperties) {
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