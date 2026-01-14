package ch.nickl.tubefy.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

@ApplicationScoped
public class DiscordClientFactory {
    public DiscordClient create(String url) {
        return RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .build(DiscordClient.class);
    }
}
