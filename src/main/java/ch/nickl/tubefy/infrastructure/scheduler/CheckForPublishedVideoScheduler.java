package ch.nickl.tubefy.infrastructure.scheduler;

import ch.nickl.tubefy.application.usecase.CheckForPublishedVideoUseCase;
import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CheckForPublishedVideoScheduler {

	@Inject
	CheckForPublishedVideoUseCase checkForPublishedVideoUseCase;

	@Inject
	Event<PublishedVideoEvent> eventEmitter;

	@ConfigProperty(name = "youtube.channel.ids")
	java.util.List<String> channelIds;

	@Scheduled(every = "{youtube.check.interval}", identity = "youtube-check-job")
	void checkYoutubeJob() {
		channelIds.forEach(channelId ->
				checkForPublishedVideoUseCase.invoke(channelId)
						.ifPresent(eventEmitter::fireAsync)
		);
	}
}