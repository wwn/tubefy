package ch.nickl.tubefy.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public record SubscriptionConfig(
        @JsonValue
        List<Subscription> subscriptions
) {
    @JsonCreator
    public SubscriptionConfig(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public record Subscription(
            @JsonProperty("discord_webhook_url")
            String discordWebhookUrl,
            @JsonProperty("yt_subscriptions")
            List<YtSubscription> ytSubscriptions
    ) {}

    public record YtSubscription(
            @JsonProperty("yt_channel_id")
            String ytChannelId,
            String greetingText

    ) {}
}
