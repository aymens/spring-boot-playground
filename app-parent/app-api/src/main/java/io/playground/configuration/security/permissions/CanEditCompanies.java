package io.playground.configuration.security.permissions;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_app_user', 'ROLE_app_admin') or hasAuthority('SCOPE_io.playground.company.read-write')")
public @interface CanEditCompanies {
}