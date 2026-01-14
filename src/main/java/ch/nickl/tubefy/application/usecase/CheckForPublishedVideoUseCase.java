package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import ch.nickl.tubefy.infrastructure.annotation.UseCase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Objects;
import java.util.Optional;

@UseCase
@ApplicationScoped
public class CheckForPublishedVideoUseCase {

    private String lastVideoId = null;

    public Optional<PublishedVideoEvent> invoke(String channelId) {
        String currentId = fetchLatestIdFromYouTube(channelId);

        if (Objects.equals(lastVideoId, currentId)) {
            return Optional.empty();
        }

        boolean isInitialRun = (lastVideoId == null);
        lastVideoId = currentId;

        return isInitialRun
                ? Optional.empty()
                : Optional.of(new PublishedVideoEvent("Neu neu neu neu neu!", currentId));
    }

    // TODO implement yt api gedöhns
    private String fetchLatestIdFromYouTube(String channelId) {
        return "vid_123_" + System.currentTimeMillis() / 10000;
    }
}