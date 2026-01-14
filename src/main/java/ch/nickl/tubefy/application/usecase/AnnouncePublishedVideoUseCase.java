package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.infrastructure.annotation.UseCase;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@ApplicationScoped
public class AnnouncePublishedVideoUseCase {

    // TODO implement discord schrott
    public void invoke() {
        log.info("Let Discord wissen... new video von Raphi ist da");
    }
}
