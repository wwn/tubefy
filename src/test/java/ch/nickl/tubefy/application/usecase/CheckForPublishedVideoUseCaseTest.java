package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.interfaces.rest.YouTubeApiModels;
import ch.nickl.tubefy.interfaces.rest.YouTubeClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(CheckForPublishedVideoUseCaseTest.Profile.class)
class CheckForPublishedVideoUseCaseTest {

	public static class Profile implements QuarkusTestProfile {
		@Override
		public Map<String, String> getConfigOverrides() {
			return Map.of(
					"youtube.check.interval", "10m",
					"youtube.api.key", "test-api-key",
					"youtube.channel.ids", "UC123,UC456",
					"discord.webhook.urls", "http://discord1,http://discord2",
					"quarkus.scheduler.enabled", "false"
			);
		}
	}

	@Inject
	CheckForPublishedVideoUseCase useCase;

	@InjectMock
	@RestClient
	YouTubeClient youtubeClient;

	@Test
	void shouldFetchLatestVideoFromYouTube() {
		String channelId = "UC12345";
		String playlistId = "UU12345";
		YouTubeApiModels.YouTubeSearchResponse mockResponse = new YouTubeApiModels.YouTubeSearchResponse(
				List.of(new YouTubeApiModels.YouTubeSearchResult(
						new YouTubeApiModels.Snippet("Title", "2024-01-01T00:00:00Z",
								new YouTubeApiModels.ResourceId("vid123"),
								new YouTubeApiModels.Thumbnails(new YouTubeApiModels.Thumbnail("http://thumb")))
				))
		);

		when(youtubeClient.fetchLatestVideos(anyString(), eq(playlistId), anyInt(), anyString()))
				.thenReturn(mockResponse);

		Optional<YouTubeApiModels.YouTubeSearchResponse> result = useCase.fetchLatestVideoFromYouTube(channelId);

		assertThat(result).isPresent();
		assertThat(result.get().items()).hasSize(1);
		assertThat(result.get().items().getFirst().snippet().title()).isEqualTo("Title");
	}

	@Test
	void shouldReturnEmptyWhenYouTubeClientThrowsException() {
		String channelId = "UC12345";
		when(youtubeClient.fetchLatestVideos(anyString(), anyString(), anyInt(), anyString()))
				.thenThrow(new RuntimeException("API Error"));

		Optional<YouTubeApiModels.YouTubeSearchResponse> result = useCase.fetchLatestVideoFromYouTube(channelId);

		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnFalseWhenVideoIsNew() {
		String publishedAt = Instant.now().minus(Duration.ofMinutes(5)).toString();

		assertThat(useCase.isTooOld(publishedAt))
				.as("5mins is fine")
				.isFalse();
	}

	@Test
	void shouldReturnTrueWhenVideoIsTooOld() {
		String publishedAt = Instant.now().minus(Duration.ofMinutes(31)).toString();

		assertThat(useCase.isTooOld(publishedAt))
				.as("31min is too old")
				.isTrue();
	}

	@Test
	void shouldReturnFalseWhenVideoIsExactlyAtLimit() {
		String publishedAt = Instant.now().minus(Duration.ofMinutes(30)).plusSeconds(1).toString();

		assertThat(useCase.isTooOld(publishedAt))
				.as("edge video is fine")
				.isFalse();
	}

	@Test
	void shouldReturnTrueWhenPublishedAtIsNull() {
		assertThat(useCase.isTooOld(null))
				.as("null is not valid")
				.isTrue();
	}

	@Test
	void shouldReturnTrueWhenPublishedAtIsInvalid() {
		assertThat(useCase.isTooOld("invalid-date"))
				.as("non valid date format is treated as too old")
				.isTrue();
	}

	@Test
	void shouldReturnTrueForVeryOldVideo() {
		String publishedAt = "2020-01-01T00:00:00Z";
		assertThat(useCase.isTooOld(publishedAt))
				.as("super too old")
				.isTrue();
	}
}
