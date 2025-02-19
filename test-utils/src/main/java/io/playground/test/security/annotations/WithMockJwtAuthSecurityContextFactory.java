package io.playground.test.security.annotations;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.commons.lang3.ArrayUtils.addAll;

public class WithMockJwtAuthSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtAuth> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtAuth annotation) {
        // Create a mock JWT token
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none") // Algorithm
                .claim("sub", "user") // Subject
                .claim("roles", String.join(" ", annotation.roles())) // Roles as scopes
                .claim("scope", String.join(" ", annotation.scopes())) // Scopes
                .build();

        // Create an authentication object with the JWT token and roles
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                AuthorityUtils.createAuthorityList(
                        Arrays.stream(addAll(annotation.roles(), annotation.scopes()))
                                .filter(Objects::nonNull)
                                .toArray(String[]::new)));

        // Set the authentication in the SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}