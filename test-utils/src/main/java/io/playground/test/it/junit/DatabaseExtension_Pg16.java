package io.playground.test.it.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static java.util.Objects.requireNonNullElse;

@Slf4j
public class DatabaseExtension_Pg16 implements BeforeAllCallback {

    private static PostgreSQLContainer<?> POSTGRES;
    private static boolean shutdownHookAdded = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        log.info("Initializing ...");
        POSTGRES = requireNonNullElse(POSTGRES, new PostgreSQLContainer<>(
                "postgres:16-alpine"
        ).withReuse(true));

        if (!POSTGRES.isRunning()) {
            log.info("Starting database ...");
            POSTGRES.start(); // don't rely on @Container anymore as it prevents containers reuse by killing them when test class finishes
        } else {
            log.info("Database is already up.");
        }

        log.info("Database infos: jdbc:postgresql://{}:{}/{}; user/pwd: {}/{}",
                POSTGRES.getHost(),
                POSTGRES.getFirstMappedPort(),
                POSTGRES.getDatabaseName(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());

        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());

        log.info("Set system properties 'spring.datasource.*'.");

        System.getProperties().entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith("spring.datasource."))
                .forEach(e -> log.info("{}={}", e.getKey(), e.getValue()));

        if (!shutdownHookAdded) {
            Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
            shutdownHookAdded = true;
            log.info("Database shutdown hook set.");
        }
    }
}
