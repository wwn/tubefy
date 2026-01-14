package ch.nickl.tubefy.infrastructure.scheduler;

import ch.nickl.tubefy.application.usecase.CheckForPublishedVideoUseCase;
import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class CheckForPublishedVideoScheduler {

    @Inject
    CheckForPublishedVideoUseCase checkForPublishedVideoUseCase;

    @Inject
    Event<PublishedVideoEvent> eventEmitter;

    @Scheduled(every = "10s")
    void checkYoutubeJob() {
        checkForPublishedVideoUseCase.invoke("UC_MY_CHANNEL_ID")
                .ifPresent(event -> {
                    eventEmitter.fireAsync(event);
                });
    }
}