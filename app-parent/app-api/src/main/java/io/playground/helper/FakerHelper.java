package io.playground.helper;

import io.playground.configuration.ConditionalOnDataGeneratorEnabled;
import io.playground.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@ConditionalOnDataGeneratorEnabled
@RequiredArgsConstructor
@Slf4j
public class FakerHelper {
    public static final int MAX_ATTEMPTS = 10;
    private static final Consumer<String> NO_OP_CONSUMER = _ -> {
    };
    private static final String UNIQUE_EMAIL_ERROR_MESSAGE =
            "Couldn''t generate a valid unique email for {0} under {1} max attempts";

    private final Faker faker;

    /**
     * Continuously generates email addresses using the provided first name and last name
     * as the basis, appending additional elements until the generated email satisfies
     * the validation criteria (i.e., the isNotValid predicate returns false).
     * Each generated email attempt is passed to the provided Consumer (attemptListener)
     * for acknowledgment/notifications.
     *
     * @param firstName       the first name of the user
     * @param lastName        the last name of the user
     * @param isNotValid      a predicate that validates the email address; should return true
     *                        for invalid email addresses and false for valid ones
     * @param attemptListener a consumer to handle or log each attempted email address
     * @return a valid email address based on the provided first name, last name, and validation rules
     */
    public String generateEmail(String firstName,
                                String lastName,
                                Predicate<String> isNotValid,
                                Consumer<String> attemptListener) {
        val localPart = String
                .join(".", firstName.toLowerCase(), lastName.toLowerCase())
                .replaceAll("[^a-z0-9.]", "");
        return generateEmail(localPart, isNotValid, attemptListener);
    }

    public String generateEmail(String localPart,
                                Predicate<String> isNotValid,
                                Consumer<String> attemptListener) {
        AtomicInteger generated = new AtomicInteger();
        return Stream.iterate(
                        generateEmail(localPart, attemptListener),
                        _ -> generated.getAndIncrement() < MAX_ATTEMPTS,
                        _ -> generateAnotherEmailWithSuffix(localPart, attemptListener)
                )
                .filter(isNotValid.negate())
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
     * Generates an email address using the provided first name and last name. The method
     * continuously modifies the generated email until it satisfies the validation criteria
     * defined by the isNotValid predicate.
     *
     * @param firstName  the first name of the user, used as part of the email generation process
     * @param lastName   the last name of the user, used as part of the email generation process
     * @param isNotValid a predicate that checks if the generated email is invalid; returns true
     *                   for invalid emails and false for valid ones
     * @return a valid email address that meets the specified validation criteria
     */
    public String generateEmail(String firstName,
                                String lastName,
                                Predicate<String> isNotValid) {
        return generateEmail(firstName, lastName, isNotValid, NO_OP_CONSUMER);
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
