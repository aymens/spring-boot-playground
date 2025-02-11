package io.playground.test.configuration;

import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Profile("test")
@AutoConfiguration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class TestUtilsAutoConfiguration {
    @ConditionalOnMissingBean(Faker.class)
    @Bean
    public Faker faker() {
        return new Faker();
    }

//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer testUtilsPageableCustomizer() {
//        return builder ->
//                builder.postConfigurer(mapper ->
//                        mapper.registerModule(new Jackson2HalModule()));
//
//    }
}