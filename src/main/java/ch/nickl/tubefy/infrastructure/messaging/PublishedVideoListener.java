package ch.nickl.tubefy.infrastructure.messaging;


import ch.nickl.tubefy.application.usecase.AnnouncePublishedVideoUseCase;
import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PublishedVideoListener {
    @Inject
    AnnouncePublishedVideoUseCase announcePublishedVideoUseCase;

    void onNewVideo(@Observes PublishedVideoEvent event) {
        announcePublishedVideoUseCase.invoke();
    }
}