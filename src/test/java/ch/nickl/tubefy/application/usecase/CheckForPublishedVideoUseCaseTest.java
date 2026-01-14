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
import static org.mockito.Mockito.*;

@QuarkusTest
@TestProfile(CheckForPublishedVideoUseCaseTest.Profile.class)
class CheckForPublishedVideoUseCaseTest {

	public static class Profile implements QuarkusTestProfile {
		@Override
		public Map<String, String> getConfigOverrides() {
			return Map.of(
					"youtube.check.interval", "10m",
					"youtube.api.key", "test-api-key",
					"discord.subscriptions", "http://discord1=UC123;http://discord2=UC456",
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

	@Test
	void shouldNotProcessReplaceMeChannelId() {
		String channelId = "replace_me";
		Optional<ch.nickl.tubefy.domain.event.PublishedVideoEvent> result = useCase.invoke(channelId);
		assertThat(result).isEmpty();
		verify(youtubeClient, never()).fetchLatestVideos(anyString(), anyString(), anyInt(), anyString());
	}

	@Test
	void shouldNotProcessWhenApiKeyIsReplaceMe() {
		CheckForPublishedVideoUseCase manualUseCase = new CheckForPublishedVideoUseCase();
		manualUseCase.apiKey = "REPLACE_ME";
		
		Optional<ch.nickl.tubefy.domain.event.PublishedVideoEvent> result = manualUseCase.invoke("UC123");
		assertThat(result).isEmpty();
	}

	@Test
	void shouldTrackLastVideoIdSeparatelyPerChannel() {
		String channel1 = "UC111";
		String playlist1 = "UU111";
		String channel2 = "UC222";
		String playlist2 = "UU222";

		YouTubeApiModels.YouTubeSearchResponse resp1 = createMockResponse("vid1", "Title 1");
		when(youtubeClient.fetchLatestVideos(anyString(), eq(playlist1), anyInt(), anyString())).thenReturn(resp1);
		useCase.invoke(channel1);

		YouTubeApiModels.YouTubeSearchResponse resp2 = createMockResponse("vid2", "Title 2");
		when(youtubeClient.fetchLatestVideos(anyString(), eq(playlist2), anyInt(), anyString())).thenReturn(resp2);
		useCase.invoke(channel2);

		assertThat(useCase.invoke(channel1)).isEmpty();

		YouTubeApiModels.YouTubeSearchResponse resp2New = createMockResponse("vid2New", "Title 2 New");
		when(youtubeClient.fetchLatestVideos(anyString(), eq(playlist2), anyInt(), anyString())).thenReturn(resp2New);

		Optional<ch.nickl.tubefy.domain.event.PublishedVideoEvent> event = useCase.invoke(channel2);
		assertThat(event).isPresent();
		assertThat(event.get().videoId()).isEqualTo("vid2New");
		assertThat(event.get().channelId()).isEqualTo(channel2);
	}

	private YouTubeApiModels.YouTubeSearchResponse createMockResponse(String videoId, String title) {
		return new YouTubeApiModels.YouTubeSearchResponse(
				List.of(new YouTubeApiModels.YouTubeSearchResult(
						new YouTubeApiModels.Snippet(title, Instant.now().minus(Duration.ofMinutes(1)).toString(),
								new YouTubeApiModels.ResourceId(videoId),
								new YouTubeApiModels.Thumbnails(new YouTubeApiModels.Thumbnail("http://thumb")))
				))
		);
	}
}
