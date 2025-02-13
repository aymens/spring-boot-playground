package io.playground.test.annotations;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@IntegrationTest
@AutoConfigureMockMvc
//TODO make it work
public @interface MockMvcIntegrationTest {
    @AliasFor(annotation = IntegrationTest.class, attribute = "properties")
    String[] properties() default {};
}


