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
import java.util.Map;
import java.util.stream.Collectors;
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

		DiscordClient.DiscordMessage message = createMessage(event);

		parseMappings().forEach((url, channelIds) -> {
			if (channelIds.isEmpty() || channelIds.contains(event.channelId())) {
				try {
					DiscordClient client = discordClientFactory.create(url);
					client.postMessage(message);
					log.info("successfully announced video to discord webhook: {}", url);
				} catch (Exception e) {
					log.error("failed to announce video to discord webhook {}: {}", url, e.getMessage());
				}
			}
		});
	}

	public List<String> getAllTargetChannelIds() {
		return parseMappings().values().stream()
				.flatMap(List::stream)
				.distinct()
				.toList();
	}

	Map<String, List<String>> parseMappings() {
		if (subscriptionsMapping == null || subscriptionsMapping.isBlank()) {
			return Collections.emptyMap();
		}

		return Stream.of(subscriptionsMapping.split(";"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(s -> s.split("=", 2))
				.collect(Collectors.toMap(
						parts -> parts[0].trim(),
						parts -> parts.length > 1 ? Stream.of(parts[1].split(",")).map(String::trim).filter(id -> !id.isEmpty() && !id.equalsIgnoreCase("REPLACE_ME")).toList() : Collections.emptyList()
				));
	}

	private DiscordClient.DiscordMessage createMessage(PublishedVideoEvent event) {
		String content = notificationMessage + " \n" + event.videoUrl();

		DiscordClient.Embed embed = new DiscordClient.Embed(
				event.title(),
				"Published at: " + event.publishedAt(),
				event.videoUrl(),
				new DiscordClient.Image(event.thumbnailUrl())
		);

		return new DiscordClient.DiscordMessage(content, List.of(embed));
	}
}
