package io.playground.test.configuration;

import io.playground.test.configuration.security.SecurityMocksConfig;
import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("test")
@AutoConfiguration
@Import(SecurityMocksConfig.class)
public class TestUtilsAutoConfig {
    @ConditionalOnMissingBean(Faker.class)
    @Bean
    public Faker faker() {
        return new Faker();
    }
}