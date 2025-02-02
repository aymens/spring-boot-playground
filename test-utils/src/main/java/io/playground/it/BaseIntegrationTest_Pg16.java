package io.playground.it;

import io.playground.SpringBootTestApplication;
import io.playground.it.junit.DatabaseExtension_Pg16;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringBootTestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(DatabaseExtension_Pg16.class)
public abstract class BaseIntegrationTest_Pg16 {

}
