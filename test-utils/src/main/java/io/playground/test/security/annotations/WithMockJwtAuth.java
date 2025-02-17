package io.playground.test.security.annotations;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtAuthSecurityContextFactory.class)
public @interface WithMockJwtAuth {
    String[] roles() default {}; // Roles to be assigned to the mock user
}