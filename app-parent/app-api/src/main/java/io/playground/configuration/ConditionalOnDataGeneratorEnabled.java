package io.playground.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
        prefix = "playground.api.rest",
        name = "data-generator.enabled",
        havingValue = "true"
)
public @interface ConditionalOnDataGeneratorEnabled {
}