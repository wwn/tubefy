package ch.nickl.tubefy.infrastructure.config;

import ch.nickl.tubefy.domain.model.SubscriptionConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(SubscriptionConfigTest.Profile.class)
class SubscriptionConfigTest {

    public static class Profile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "subscription.config", "[{\"discord_webhook_url\":\"http://test\",\"yt_subscriptions\":[{\"yt_channel_id\":\"UC123\",\"greetingText\":\"test\"}]}]"
            );
        }
    }

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
