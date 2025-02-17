package io.playground.configuration.security;

import io.playground.test.configuration.security.SecurityAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        SecurityConfig.class,
        SecurityAutoConfig.class
})
public @interface EnableSecurity {
}