package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import ch.nickl.tubefy.interfaces.rest.DiscordClient;
import ch.nickl.tubefy.interfaces.rest.DiscordClientFactory;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestProfile(AnnouncePublishedVideoUseCaseTest.Profile.class)
class AnnouncePublishedVideoUseCaseTest {

	public static class Profile implements QuarkusTestProfile {
		@Override
		public Map<String, String> getConfigOverrides() {
			return Map.of(
					"discord.subscriptions", "http://webhook1=UC123;http://webhook2=UC456",
					"quarkus.scheduler.enabled", "false",
					"youtube.api.key", "test-api-key",
					"youtube.check.interval", "1h"
			);
		}
	}

	@Inject
	AnnouncePublishedVideoUseCase useCase;

	@InjectMock
	DiscordClientFactory discordClientFactory;

	@Test
	void shouldInvokeDiscordWebhooks() {
		PublishedVideoEvent event = new PublishedVideoEvent(
				"Test Title",
				"vid123",
				"UC123",
				"2024-01-01T00:00:00Z",
				"http://thumb"
		);

		DiscordClient mockClient1 = mock(DiscordClient.class);
		DiscordClient mockClient2 = mock(DiscordClient.class);

		when(discordClientFactory.create("http://webhook1")).thenReturn(mockClient1);
		when(discordClientFactory.create("http://webhook2")).thenReturn(mockClient2);

		useCase.invoke(event);

		verify(mockClient1).postMessage(any(DiscordClient.DiscordMessage.class));
		verify(mockClient2, never()).postMessage(any(DiscordClient.DiscordMessage.class));
	}

	@Test
	void shouldHandleFailureOnOneWebhook() {
		PublishedVideoEvent event = new PublishedVideoEvent(
				"Test Title",
				"vid123",
				"UC123",
				"2024-01-01T00:00:00Z",
				"http://thumb"
		);

		DiscordClient mockClient1 = mock(DiscordClient.class);
		when(discordClientFactory.create("http://webhook1")).thenReturn(mockClient1);

		doThrow(new RuntimeException("Discord Error")).when(mockClient1).postMessage(any());

		useCase.invoke(event);

		verify(mockClient1).postMessage(any());
	}

	@Test
	void shouldInvokeMultipleWebhooksForSameChannel() {
		AnnouncePublishedVideoUseCase manualUseCase = new AnnouncePublishedVideoUseCase(discordClientFactory);
		manualUseCase.subscriptionsMapping = "http://webhook1=UC123;http://webhook2=UC123";

		PublishedVideoEvent event = new PublishedVideoEvent(
				"Shared Video",
				"vid-shared",
				"UC123",
				"2024-01-01T00:00:00Z",
				"http://thumb"
		);

		DiscordClient mockClient1 = mock(DiscordClient.class);
		DiscordClient mockClient2 = mock(DiscordClient.class);

		when(discordClientFactory.create("http://webhook1")).thenReturn(mockClient1);
		when(discordClientFactory.create("http://webhook2")).thenReturn(mockClient2);

		manualUseCase.invoke(event);

		verify(mockClient1).postMessage(any(DiscordClient.DiscordMessage.class));
		verify(mockClient2).postMessage(any(DiscordClient.DiscordMessage.class));
	}

	@Test
	void shouldCorrectBuildMessage() {
		PublishedVideoEvent event = new PublishedVideoEvent(
				"The Magnificent Seven",
				"vid123",
				"UC123",
				"2024-01-01T00:00:00Z",
				"http://thumb"
		);

		DiscordClient mockClient1 = mock(DiscordClient.class);
		when(discordClientFactory.create(anyString())).thenReturn(mockClient1);

		useCase.invoke(event);

		ArgumentCaptor<DiscordClient.DiscordMessage> captor = ArgumentCaptor.forClass(DiscordClient.DiscordMessage.class);
		verify(mockClient1, atLeastOnce()).postMessage(captor.capture());

		DiscordClient.DiscordMessage message = captor.getValue();
		assertThat(message.content()).contains("vid123");
		assertThat(message.embeds()).hasSize(1);
		assertThat(message.embeds().getFirst().title()).isEqualTo("The Magnificent Seven");
		assertThat(message.embeds().getFirst().image().url()).isEqualTo("http://thumb");
	}

	@Test
	void shouldHandleSubscriptionsWithThreeWebhooksCorrectly() {
		AnnouncePublishedVideoUseCase manualUseCase = new AnnouncePublishedVideoUseCase(discordClientFactory);
		manualUseCase.subscriptionsMapping = "url1=UC_A,UC_B;url2=UC_C;url3=UC_A,UC_D";

		PublishedVideoEvent event = new PublishedVideoEvent(
				"Video for A",
				"vidA",
				"UC_A",
				"2024-01-01T00:00:00Z",
				"http://thumb"
		);

		DiscordClient mock1 = mock(DiscordClient.class);
		DiscordClient mock2 = mock(DiscordClient.class);
		DiscordClient mock3 = mock(DiscordClient.class);

		when(discordClientFactory.create("url1")).thenReturn(mock1);
		when(discordClientFactory.create("url2")).thenReturn(mock2);
		when(discordClientFactory.create("url3")).thenReturn(mock3);

		manualUseCase.invoke(event);

		verify(mock1).postMessage(any());
		verify(mock2, never()).postMessage(any());
		verify(mock3).postMessage(any());
	}
}
