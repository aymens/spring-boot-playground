package io.playground.common;

import io.playground.junit.DatabaseExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@ExtendWith(DatabaseExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

}
