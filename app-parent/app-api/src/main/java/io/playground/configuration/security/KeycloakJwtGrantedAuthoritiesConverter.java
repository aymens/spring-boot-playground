package io.playground.configuration.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";

    private String authorityPrefix = "SCOPE_";
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    public KeycloakJwtGrantedAuthoritiesConverter setAuthorityPrefix(String authorityPrefix) {
        this.authorityPrefix = Objects.requireNonNull(authorityPrefix, "authorityPrefix cannot be null");
        defaultConverter.setAuthorityPrefix(authorityPrefix);
        return this;
    }

    @Override
    public Collection<GrantedAuthority> convert(@Nullable Jwt jwt) {
        Objects.requireNonNull(jwt, "jwt cannot be null");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(extractKeycloakRoles(jwt));
        authorities.addAll(defaultConverter.convert(jwt));

        return authorities;
    }

    @SuppressWarnings( "unchecked")
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        return Optional.ofNullable(jwt.<Map<String, Object>>getClaim(REALM_ACCESS_CLAIM))
                .map(realmAccess -> (List<String>) realmAccess.get(ROLES_CLAIM))
                .stream().flatMap(Collection::stream)
                .map(this::toGrantedAuthority)
                .toList();
    }

    private GrantedAuthority toGrantedAuthority(String role) {
        return new SimpleGrantedAuthority(authorityPrefix + role);
    }
}