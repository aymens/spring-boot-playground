package io.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the Spring Boot application. This class is responsible for bootstrapping 
 * and starting the application using Spring Boot's {@code SpringApplication}.
 * <p>
 * This class is typically used as the configuration and initialization entry point when running 
 * the application as a standalone jar or during integration testing. It utilizes the 
 * {@code @SpringBootApplication} annotation, which is a convenience annotation that combines 
 * {@code @Configuration}, {@code @EnableAutoConfiguration}, and {@code @ComponentScan}.
 * <p>
 * Dependencies or sibling classes related to this application may include configuration for 
 * testing, such as database extensions or MockMvc integration, primarily used in extended classes 
 * during comprehensive application testing procedures.
 */
@SpringBootApplication
//@ActiveProfiles("test") // either that, or this: a src/test/resources/application.properties containing "spring.profiles.active=test"
public class SpringBootTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestApplication.class, args);
    }
}
