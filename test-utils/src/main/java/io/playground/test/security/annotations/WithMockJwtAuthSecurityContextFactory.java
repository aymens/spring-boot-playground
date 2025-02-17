package io.playground.test.security.annotations;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockJwtAuthSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtAuth> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtAuth annotation) {
        // Create a mock JWT token
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none") // Algorithm
                .claim("sub", "user") // Subject
                .claim("roles", String.join(" ", annotation.roles())) // Roles as scopes
                .build();

        // Create an authentication object with the JWT token and roles
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                AuthorityUtils.createAuthorityList(annotation.roles())
        );

        // Set the authentication in the SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}