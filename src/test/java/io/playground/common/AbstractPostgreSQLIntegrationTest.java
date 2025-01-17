package io.playground.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest
public abstract class AbstractPostgreSQLIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ).withReuse(true);

    static boolean shutdownHookAdded = false;

    @BeforeAll
    static void init() {
        if(!POSTGRES.isRunning()) {
            log.info("Starting database ...");
            POSTGRES.start(); // don't rely on @Container anymore as its prevent reuse by killing the container when test class finishes
        }

        log.info("Database is started: jdbc:postgresql://{}:{}/{}; user/pwd: {}/{}",
                POSTGRES.getHost(),
                POSTGRES.getFirstMappedPort(),
                POSTGRES.getDatabaseName(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());

        if(!shutdownHookAdded) {
            Runtime.getRuntime().addShutdownHook(new Thread(AbstractPostgreSQLIntegrationTest::shutdownDatabase));
            shutdownHookAdded = true;
        }
    }

    private static void shutdownDatabase() {
        log.info("The jvm is about to shutdown, stopping database container ...");
        POSTGRES.stop();
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void checkPostgreSQL_WhenTestStarts_IsRunning() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }
}
