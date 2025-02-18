package io.playground.configuration.security;

import io.playground.test.configuration.security.SecurityAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helpful for controller test classes that aren't loading complete spring boot test contexts.
 * <p></p>
 * Helps loading "app" security config, avoiding this way the unwanted defaults from kicking in.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        SecurityConfig.class,
        SecurityAutoConfig.class
})
public @interface EnableSecurity {
}