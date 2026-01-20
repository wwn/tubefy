package ch.nickl.tubefy.infrastructure.config;

import ch.nickl.tubefy.domain.model.SubscriptionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.spi.Converter;

import java.io.IOException;
import java.util.List;

public class SubscriptionConfigConverter implements Converter<SubscriptionConfig> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SubscriptionConfig convert(String value) throws IllegalArgumentException, NullPointerException {
        if (value == null || value.isBlank()) {
            return new SubscriptionConfig(List.of());
        }

        try {
            String trimmedValue = value.trim();
            if (trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
                trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 1)
                        .replace("\\\"", "\"");
            }
            return objectMapper.readValue(trimmedValue, SubscriptionConfig.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to convert SUBSCRIPTION_CONFIG: " + e.getMessage(), e);
        }
    }
}
