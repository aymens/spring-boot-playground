package io.playground.configuration;

import io.playground.configuration.annotations.ConditionalOnDataGeneratorEnabled;
import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@ConditionalOnDataGeneratorEnabled
@AutoConfiguration
public class FakerAutoConfig {
    @ConditionalOnMissingBean(Faker.class)
    @Bean
    public Faker faker() {
        return new Faker();
    }
}