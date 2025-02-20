package io.playground.test.it;

import io.playground.SpringBootTestApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@SpringBootTest(
        classes = SpringBootTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public abstract class BaseMockMvcIntegrationTest_Pg16 extends BaseIntegrationTest_Pg16 {
}
