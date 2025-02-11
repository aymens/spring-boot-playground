package io.playground.helper;

import io.playground.exception.BusinessException;
import io.playground.test.configuration.TestUtilsAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static io.playground.helper.FakerHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {FakerHelper.class, TestUtilsAutoConfiguration.class})
class FakerHelperTest {

    @Autowired
    private FakerHelper fakerHelper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9.]+@[a-z0-9.]+\\.[a-z]{2,}$");

    @Test
    void generateEmail_WithNames_ReturnsValidEmail() {
        val email = fakerHelper.generateEmail("John", "Doe", ANY);
        assertThat(email)
                .matches(EMAIL_PATTERN)
                .startsWith("john.doe");
    }

    @Test
    void generateEmail_WithSpecialChars_ReturnsCleanEmail() {
        val email = fakerHelper.generateEmail("John-Paul", "O'Doe", ANY);

        assertThat(email)
                .matches(EMAIL_PATTERN)
                .startsWith("johnpaul.odoe");
    }

    @Test
    void generateEmail_WhenDuplicate_AddsNumericSuffix() {
        val email1 = fakerHelper.generateEmail("john", "doe", ANY);
        val email2 = fakerHelper.generateEmail("john", "doe", STARTS_WITH.apply("john.doe@").negate());
        assertThat(email1)
                .matches(EMAIL_PATTERN)
                .startsWith("john.doe@")
                .isNotEqualTo(email2);
        assertThat(email2)
                .matches(EMAIL_PATTERN)
                .startsWith("john.doe.");
    }

    @Test
    void generateEmail_WhenMultipleDuplicates_IncrementsUntilUnique() {
        val usedEmails = new HashSet<String>();
        val email1 = fakerHelper.generateEmail("john", "doe", ANY);
        usedEmails.add(email1);
        val email2 = fakerHelper.generateEmail("john", "doe", STARTS_WITH.apply("john.doe."));
        usedEmails.add(email2);
        Predicate<String> thereIsLessThanOneCollision = _ -> usedEmails.stream().filter(STARTS_WITH.apply("john.doe.")).count() <= 1;
        Predicate<String> thereIsMoreThanOneCollision = thereIsLessThanOneCollision.negate();
        var email3 = "";
        while (thereIsLessThanOneCollision.test(null)) {
            email3 = fakerHelper.generateEmail(
                    "john",
                    "doe",
                    thereIsMoreThanOneCollision,
                    usedEmails::add
            );
            usedEmails.add(email3);
        }

        assertThat(email3)
                .matches(EMAIL_PATTERN)
                .startsWith("john.doe.")
                .isNotIn(email1, email2);
    }

    @Test
    void generateEmail_TracksAllAttempts() {
        List<String> attempts = new ArrayList<>();
        val email = fakerHelper.generateEmail(
                "john",
                "doe",
                _ -> attempts.size() >= 3,  // Force 3 attempts
                attempts::add
        );

        assertThat(attempts)
                .hasSize(3)
                .allSatisfy(attempt -> assertThat(attempt).matches(EMAIL_PATTERN))
                .allMatch(attempt -> attempt.startsWith("john.doe"))
                .contains(email);  // Final result should be in attempts
    }

    @Test
    void generateEmail_WhenMaxAttemptsIsReached_ThrowsException() {
        assertThatThrownBy(
                () -> fakerHelper.generateEmail(
                        "john",
                        "doe",
                        NONE  /*Force infinite attempts*/
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Couldn't generate a valid unique email for john.doe in less than 10 attempts");
    }
}