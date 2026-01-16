package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
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

	@ConfigProperty(name = "discord.webhook.urls")
	List<String> webhookUrls;
	
	@ConfigProperty(name = "discord.notification.message")
	String notificationMessage;

	private final DiscordClientFactory discordClientFactory;

	@Inject
	public AnnouncePublishedVideoUseCase(DiscordClientFactory discordClientFactory) {
		this.discordClientFactory = discordClientFactory;
	}

	public void invoke(PublishedVideoEvent event) {
		log.info("new video to be announced: {} ({}) published at {}",
				event.title(), event.videoId(), event.publishedAt());

		DiscordClient.DiscordMessage message = createMessage(event);

		webhookUrls.forEach(url -> {
			try {
				DiscordClient client = discordClientFactory.create(url);
				client.postMessage(message);
				log.info("successfully announced video to discord webhook: {}", url);
			} catch (Exception e) {
				log.error("failed to announce video to discord webhook {}: {}", url, e.getMessage());
			}
		});
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
