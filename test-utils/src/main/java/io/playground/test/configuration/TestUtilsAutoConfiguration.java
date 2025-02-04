package io.playground.test.configuration;

import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("test")
@AutoConfiguration
public class TestUtilsAutoConfiguration {
    @ConditionalOnMissingBean(Faker.class)
    @Bean
    public Faker faker() {
        return new Faker();
    }
}