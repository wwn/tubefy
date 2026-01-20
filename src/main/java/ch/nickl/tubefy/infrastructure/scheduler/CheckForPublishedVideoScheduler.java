package ch.nickl.tubefy.infrastructure.scheduler;

import ch.nickl.tubefy.application.usecase.AnnouncePublishedVideoUseCase;
import ch.nickl.tubefy.application.usecase.CheckForPublishedVideoUseCase;
import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class CheckForPublishedVideoScheduler {

    @Inject
    CheckForPublishedVideoUseCase checkForPublishedVideoUseCase;

    @Inject
    Event<PublishedVideoEvent> eventEmitter;

    @Inject
    AnnouncePublishedVideoUseCase announcePublishedVideoUseCase;

    @ConfigProperty(name = "youtube.check.interval")
    java.time.Duration interval;

    @Scheduled(every = "{youtube.check.interval}", identity = "youtube-check-job")
    void checkYoutubeJob() {
        log.info("Starting check for published videos with interval: {}. Channels to check: {}", interval, announcePublishedVideoUseCase.getAllTargetChannelIds().size());
        announcePublishedVideoUseCase.getAllTargetChannelIds().forEach(channelId -> {
            log.debug("checking yt for channel: {}", channelId);
            checkForPublishedVideoUseCase.invoke(channelId)
                    .ifPresent(event -> {
                        log.info("new video found for channel {}: {}", channelId, event.title());
                        eventEmitter.fireAsync(event);
                    });
        });
    }
}