package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import ch.nickl.tubefy.domain.model.SubscriptionConfig;
import ch.nickl.tubefy.infrastructure.annotation.UseCase;
import ch.nickl.tubefy.interfaces.rest.DiscordClient;
import ch.nickl.tubefy.interfaces.rest.DiscordClientFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Slf4j
@UseCase
@ApplicationScoped
public class AnnouncePublishedVideoUseCase {

    @ConfigProperty(name = "subscription.config")
    SubscriptionConfig subscriptionConfig;

    private final DiscordClientFactory discordClientFactory;

    @Inject
    public AnnouncePublishedVideoUseCase(DiscordClientFactory discordClientFactory) {
        this.discordClientFactory = discordClientFactory;
    }

    public void invoke(PublishedVideoEvent event) {
        log.info("new video to be announced: {} ({}) published at {} for channel {}",
                event.title(), event.videoId(), event.publishedAt(), event.channelId());

        subscriptionConfig.subscriptions().forEach(subscription -> subscription.ytSubscriptions().stream()
                .filter(ytSubscription -> ytSubscription.ytChannelId().equals(event.channelId()))
                .forEach(ytSub -> {
                    try {
                        String greeting = ytSub.greetingText();
                        if (greeting == null || greeting.isBlank()) {
                            greeting = "New video is out!";
                        }
                        DiscordClient.DiscordMessage message = createMessage(event, greeting);
                        DiscordClient client = discordClientFactory.create(subscription.discordWebhookUrl());
                        client.postMessage(message);
                        log.info("successfully announced video to discord webhook: {}", subscription.discordWebhookUrl());
                    } catch (Exception e) {
                        log.error("failed to announce video to discord webhook {}: {}", subscription.discordWebhookUrl(), e.getMessage());
                    }
                }));
    }

    public List<String> getAllTargetChannelIds() {
        return subscriptionConfig.subscriptions().stream()
                .flatMap(s -> s.ytSubscriptions().stream())
                .map(SubscriptionConfig.YtSubscription::ytChannelId)
                .distinct()
                .toList();
    }

    private DiscordClient.DiscordMessage createMessage(PublishedVideoEvent event, String greetingText) {
        String content = greetingText + " \n" + event.videoUrl();

        DiscordClient.Embed embed = new DiscordClient.Embed(
                event.title(),
                "Published at: " + event.publishedAt(),
                event.videoUrl(),
                new DiscordClient.Image(event.thumbnailUrl())
        );

        return new DiscordClient.DiscordMessage(content, List.of(embed));
    }
}
