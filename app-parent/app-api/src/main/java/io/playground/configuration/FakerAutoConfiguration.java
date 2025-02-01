package io.playground.configuration;

import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnDataGeneratorEnabled
public class FakerAutoConfiguration {
    @Bean
    public Faker faker() {
        return new Faker();
    }
}