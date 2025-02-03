package io.playground.test.it;

import io.playground.test.BaseTest;
import io.playground.test.it.junit.DatabaseExtension_Pg16;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DatabaseExtension_Pg16.class)
public abstract class BaseIntegrationTest_Pg16 extends BaseTest {
}
