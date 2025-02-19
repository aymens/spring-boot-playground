package io.playground.test.configuration.security.annotations;

import io.playground.test.configuration.security.NoSecurityConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to disable security configuration for a Spring application.
 * <p><p>
 * When applied to a class, this annotation imports the {@code NoSecurityConfig}
 * configuration, which disables CSRF protection and allows unrestricted access
 * to all endpoints. This is typically used in testing environments to bypass
 * security constraints.
 * <p><p>
 * The associated configuration, {@code NoSecurityConfig}, is activated only
 * under the "test" profile.
 * <p><p>
 * Target: This annotation can only be applied at the class level.
 * Retention: This annotation is retained at runtime for reflective access.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NoSecurityConfig.class)
public @interface DisableSecurity {
}