package io.playground.test.it.junit;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.Locale;

import static java.util.Objects.requireNonNullElse;

@Slf4j
public class DatabaseExtension_Pg16 implements BeforeAllCallback {

    private static PostgreSQLContainer<?> POSTGRES;
    private static volatile boolean IS_NOT_INITIALIZED = true;
    private static final PrettyTime PRETTY_TIME = new PrettyTime(Locale.ENGLISH);
    private static final Object LOCK = new Object();

    static {
        // Remove the "JustNow" unit to ensure small durations are shown
        PRETTY_TIME.removeUnit(JustNow.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        synchronized (LOCK) {
            if (IS_NOT_INITIALIZED) {
                val startTime = Instant.now();
                log.info("Initializing database ...");
                POSTGRES = requireNonNullElse(POSTGRES, new PostgreSQLContainer<>(
                        "postgres:16-alpine"
                ).withReuse(true));

                val dockerImageName = POSTGRES.getDockerImageName();
                if (!POSTGRES.isRunning()) {
                    log.info("Starting database {} ...", dockerImageName);
                    POSTGRES.start(); // don't rely on @Container anymore as it prevents containers reuse by killing them when test class finishes
                } else {
                    log.info("Database {} is already up.", dockerImageName);
                }

                log.info("Database {} infos: jdbc:postgresql://{}:{}/{}; user/pwd: {}/{}",
                        POSTGRES.getDockerImageName(),
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

                Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
                IS_NOT_INITIALIZED = false;
                log.info("Database {} shutdown hook set.", dockerImageName);
                log.info("Database {} initialization took {}.", dockerImageName, PRETTY_TIME.formatDuration(startTime));
            }
        }

    }
}
