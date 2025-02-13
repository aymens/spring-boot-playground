package io.playground.test.annotations;

import io.playground.SpringBootTestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest(classes = SpringBootTestApplication.class)
@ActiveProfiles("test")
//@Transactional
//@ExtendWith(DatabaseExtension_Pg16.class)
//TODO make it work
public @interface IntegrationTest {
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {};
}

