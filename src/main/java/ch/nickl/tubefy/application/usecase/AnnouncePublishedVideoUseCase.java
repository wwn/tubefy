package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import ch.nickl.tubefy.infrastructure.annotation.UseCase;
import ch.nickl.tubefy.interfaces.rest.DiscordClient;
import ch.nickl.tubefy.interfaces.rest.DiscordClientFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@UseCase
@ApplicationScoped
public class AnnouncePublishedVideoUseCase {

    @ConfigProperty(name = "discord.subscriptions")
    String subscriptionsMapping;

    @ConfigProperty(name = "discord.notification.message")
    String notificationMessage;

    private final DiscordClientFactory discordClientFactory;

    @Inject
    public AnnouncePublishedVideoUseCase(DiscordClientFactory discordClientFactory) {
        this.discordClientFactory = discordClientFactory;
    }

    public void invoke(PublishedVideoEvent event) {
        log.info("new video to be announced: {} ({}) published at {} for channel {}",
                event.title(), event.videoId(), event.publishedAt(), event.channelId());

        parseMappings().forEach(mapping -> {
            if (mapping.channelIds().isEmpty() || mapping.channelIds().contains(event.channelId())) {
                try {
                    DiscordClient.DiscordMessage message = createMessage(event, mapping.notificationMessage());
                    DiscordClient client = discordClientFactory.create(mapping.webhookUrl());
                    client.postMessage(message);
                    log.info("successfully announced video to discord webhook: {}", mapping.webhookUrl());
                } catch (Exception e) {
                    log.error("failed to announce video to discord webhook {}: {}", mapping.webhookUrl(), e.getMessage());
                }
            }
        });
    }

    public List<String> getAllTargetChannelIds() {
        return parseMappings().stream()
                .flatMap(m -> m.channelIds().stream())
                .distinct()
                .toList();
    }

    record SubscriptionMapping(String webhookUrl, List<String> channelIds, String notificationMessage) {
    }

    List<SubscriptionMapping> parseMappings() {
        if (subscriptionsMapping == null || subscriptionsMapping.isBlank()) {
            return Collections.emptyList();
        }

        return Stream.of(subscriptionsMapping.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::mapToSubscription)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private SubscriptionMapping mapToSubscription(String entry) {
        String[] parts = entry.split("=", 2);
        String url = parts[0].trim();

        if (url.isEmpty()) {
            log.warn("Empty webhook URL found in mapping: {}", entry);
            return null;
        }

        List<String> channelIds = Collections.emptyList();
        String message = this.notificationMessage;

        if (parts.length > 1) {
            String rightPart = parts[1].trim();

            if (rightPart.endsWith("]") && rightPart.contains("[")) {
                int openBracket = rightPart.lastIndexOf("[");
                message = rightPart.substring(openBracket + 1, rightPart.length() - 1).trim();
                rightPart = rightPart.substring(0, openBracket).trim();
            }

            channelIds = Stream.of(rightPart.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty() && !id.equalsIgnoreCase("REPLACE_ME"))
                    .toList();
        }

        return new SubscriptionMapping(url, channelIds, message);
    }

    private DiscordClient.DiscordMessage createMessage(PublishedVideoEvent event, String customMessage) {
        String content = (customMessage != null ? customMessage : notificationMessage) + " \n" + event.videoUrl();

        DiscordClient.Embed embed = new DiscordClient.Embed(
                event.title(),
                "Published at: " + event.publishedAt(),
                event.videoUrl(),
                new DiscordClient.Image(event.thumbnailUrl())
        );

        return new DiscordClient.DiscordMessage(content, List.of(embed));
    }
}
