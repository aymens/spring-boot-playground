package io.playground.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class DatabaseExtension implements BeforeAllCallback {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ).withReuse(true);

    private static boolean shutdownHookAdded = false;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!POSTGRES.isRunning()) {
            log.info("Starting database ...");
            POSTGRES.start(); // don't rely on @Container anymore as its prevent reuse by killing the container when test class finishes
        } else {
            log.info("Database is already up.");
        }

        if (!shutdownHookAdded) {
            Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
            shutdownHookAdded = true;
            log.info("Database shutdown hook set.");
        }

        log.info("Database infos: jdbc:postgresql://{}:{}/{}; user/pwd: {}/{}",
                POSTGRES.getHost(),
                POSTGRES.getFirstMappedPort(),
                POSTGRES.getDatabaseName(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
    }
}
