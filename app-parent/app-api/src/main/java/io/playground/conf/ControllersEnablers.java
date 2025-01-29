package io.playground.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Helps autocomplete in yaml.
 *
 */
@Component
@ConfigurationProperties(prefix = "playground.api.rest")
@Getter
@Setter
public class ControllersEnablers {
    @Getter
    @Setter
    public static class Controller {
        boolean enabled;
    }

    Controller dataGenerator;
    Controller sandbox;
}
