package ch.nickl.tubefy.infrastructure.config;

import ch.nickl.tubefy.domain.model.SubscriptionConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class SubscriptionConfigTest {

    @Inject
    @ConfigProperty(name = "subscription.config")
    SubscriptionConfig config;

    @Test
    void shouldLoadConfig() {
        assertThat(config).isNotNull();
        assertThat(config.subscriptions()).isNotEmpty();
        System.out.println("[DEBUG_LOG] Loaded config: " + config);
    }
}
