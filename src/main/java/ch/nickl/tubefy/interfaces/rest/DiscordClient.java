package ch.nickl.tubefy.interfaces.rest;

import jakarta.ws.rs.POST;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "discord-api")
public interface DiscordClient {

	@POST
	@Retry(maxRetries = 3, delay = 1000)
	void postMessage(DiscordMessage message);

	record DiscordMessage(String content, List<Embed> embeds) {
	}

	record Embed(String title, String description, String url, Image image) {
	}

	record Image(String url) {
	}
}
