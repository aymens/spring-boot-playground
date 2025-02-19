package io.playground.helper;

import io.playground.configuration.annotations.ConditionalOnDataGeneratorEnabled;
import io.playground.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@ConditionalOnDataGeneratorEnabled
@RequiredArgsConstructor
@Slf4j
public class FakerHelper {
    public static final int MAX_ATTEMPTS = 10;

    /**
     * A predicate that tests any given string and always evaluates to {@code true}.
     * This constant can be used whenever a predicate is required but should accept all inputs unconditionally.
     */
    public static final Predicate<String> ANY = _ -> true;
    /**
     * A constant Predicate that always returns false for any input string.
     * This can be used as a default predicate or to express a condition where
     * no input string satisfies the predicate.
     */
    public static final Predicate<String> NONE = _ -> false;
    /**
     * A constant Function that generates a Predicate to test whether a given string
     * starts with a specified prefix. The function takes a prefix string as input
     * and returns a Predicate that checks if any target string begins with the provided prefix.
     */
    public static final Function<String, Predicate<String>> STARTS_WITH = prefix -> email -> email.startsWith(prefix);

    /**
     * A no-operation implementation of {@link Consumer} that accepts a {@link String} input
     * but performs no action. This is typically used as a default or placeholder where
     * a valid {@link Consumer} implementation is required, but no operation is intended.
     */
    private static final Consumer<String> NO_OP_CONSUMER = _ -> {
    };
    private static final String UNIQUE_EMAIL_ERROR_MESSAGE =
            "Couldn''t generate a valid unique email for {0} in less than {1} attempts";

    private final Faker faker;

    /**
     * Generates an email address using the provided first name and last name,
     * and ensures it meets the specified conditions.
     *
     * @param firstName           the first name of the individual, used as part of the email address
     * @param lastName            the last name of the individual, used as part of the email address
     * @param isAnAcceptableEmail a predicate to determine if the generated email address meets the conditions
     * @param attemptListener     a consumer to handle each generated email attempt
     * @return a valid email address that satisfies the specified conditions
     */
    public String generateEmail(String firstName,
                                String lastName,
                                Predicate<String> isAnAcceptableEmail,
                                Consumer<String> attemptListener) {
        val localPart = String
                .join(".", firstName.toLowerCase(), lastName.toLowerCase())
                .replaceAll("[^a-z0-9.]", "");
        return generateEmail(localPart, isAnAcceptableEmail, attemptListener);
    }

    public String generateEmail(String localPart,
                                Predicate<String> isAnAcceptableEmail,
                                Consumer<String> attemptListener) {
        return Stream.iterate(
                        generateEmail(localPart, attemptListener),
//                        _ -> generated.getAndIncrement() < MAX_ATTEMPTS, // replaced below for simplicity by .limit(MAX_ATTEMPTS)
                        _ -> generateAnotherEmailWithSuffix(localPart, attemptListener)
                )
                .limit(MAX_ATTEMPTS)
                .filter(isAnAcceptableEmail)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(
                                MessageFormat.format(
                                        UNIQUE_EMAIL_ERROR_MESSAGE,
                                        localPart,
                                        MAX_ATTEMPTS))

                );
    }

    /**
     * Generates an email address based on the given first name, last name, and email validation logic.
     *
     * @param firstName           the first name of the user, used to create the email address
     * @param lastName            the last name of the user, used to create the email address
     * @param isAnAcceptableEmail a predicate that determines whether a generated email is acceptable
     * @return the generated email address that satisfies the provided predicate
     */
    public String generateEmail(String firstName,
                                String lastName,
                                Predicate<String> isAnAcceptableEmail) {
        return generateEmail(firstName, lastName, isAnAcceptableEmail, NO_OP_CONSUMER);
    }

    private String generateAnotherEmailWithSuffix(String initialLocalPart,
                                                  Consumer<String> attemptListener) {
        String email = generateEmail(suffixLocalPart(initialLocalPart), attemptListener);
        log.info("Attempting: {}", email);
        return email;
    }

    private String suffixLocalPart(String initialLocalPart) {
        return String.join(".", initialLocalPart, faker.number().digits(3));
    }

    private String generateEmail(String localPart,
                                 Consumer<String> attemptAcknowledge) {
        String emailAddress = faker.internet().emailAddress(localPart);
        attemptAcknowledge.accept(emailAddress);
        return emailAddress;
    }
}
