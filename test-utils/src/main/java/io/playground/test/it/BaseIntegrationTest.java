package io.playground.test.it;

import io.playground.SpringBootTestApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = SpringBootTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class BaseIntegrationTest {
}
