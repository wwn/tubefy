package ch.nickl.tubefy.application.usecase;

import ch.nickl.tubefy.application.dto.YouTubeVideoDto;
import ch.nickl.tubefy.domain.event.PublishedVideoEvent;
import ch.nickl.tubefy.infrastructure.annotation.UseCase;
import ch.nickl.tubefy.interfaces.rest.YouTubeApiModels;
import ch.nickl.tubefy.interfaces.rest.YouTubeClient;
import ch.nickl.tubefy.interfaces.rest.mapper.YouTubeMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@UseCase
@ApplicationScoped
public class CheckForPublishedVideoUseCase {

	public static final int MAX_PUBLISHED_VIDEO_AGE_MULTIPLICATOR = 3;
	public static final String YT_PLAYLIST_PREFIX = "UU";
	@Inject
	@RestClient
	YouTubeClient youtubeClient;

	@Inject
	YouTubeMapper youtubeMapper;

	@ConfigProperty(name = "youtube.api.key")
	String apiKey;

	@ConfigProperty(name = "youtube.check.interval")
	Duration checkInterval;

	private final Map<String, String> lastVideoIds = new ConcurrentHashMap<>();

	public Optional<PublishedVideoEvent> invoke(String channelId) {
		if (channelId == null || channelId.isBlank() || channelId.equalsIgnoreCase("REPLACE_ME")) {
			log.warn("invalid channel id");
			return Optional.empty();
		}
		if (apiKey == null || apiKey.isBlank() || apiKey.equalsIgnoreCase("REPLACE_ME")) {
			log.error("YouTube API Key is not configured (currently 'REPLACE_ME')");
			return Optional.empty();
		}
		return fetchLatestVideoFromYouTube(channelId)
				.map(youtubeMapper::toSearchDto)
				.flatMap(searchDto -> {
					if (searchDto.videos().isEmpty()) {
						return Optional.empty();
					}

					YouTubeVideoDto latestVideo = searchDto.videos().getFirst();
					String currentId = latestVideo.videoId();
					String title = latestVideo.title();
					String publishedAt = latestVideo.publishedAt();

					String lastVideoId = lastVideoIds.get(channelId);
					if (Objects.equals(lastVideoId, currentId)) {
						return Optional.empty();
					}

					if (isTooOld(publishedAt)) {
						log.info("video {} is too old (published at: {})", currentId, publishedAt);
						lastVideoIds.put(channelId, currentId);
						return Optional.empty();
					}

					// init run
					if (lastVideoId == null) {
						log.info("init for channel {}. last video: {}", channelId, currentId);
						lastVideoIds.put(channelId, currentId);
						return Optional.empty();
					}

					lastVideoIds.put(channelId, currentId);
					log.info("found new video: {} ({}) {}", title, currentId, publishedAt);
					return Optional.of(new PublishedVideoEvent(title, currentId, channelId, publishedAt, latestVideo.thumbnailUrl()));
				});
	}

	boolean isTooOld(String publishedAt) {
		if (publishedAt == null) {
			return true;
		}

		try {
			Instant publishedInstant = Instant.parse(publishedAt);
			Instant limit = Instant.now().minus(checkInterval.multipliedBy(MAX_PUBLISHED_VIDEO_AGE_MULTIPLICATOR));
			return publishedInstant.isBefore(limit);
		} catch (Exception e) {
			log.info("could not parse publishedAt: {}", publishedAt, e);
			return true;
		}
	}

	Optional<YouTubeApiModels.YouTubeSearchResponse> fetchLatestVideoFromYouTube(String channelId) {
		// searching by playList is cheaper
		// for real yt search keep UC instead of UU and use path /search and @QueryParam("channelId") in client and here
		// real yt search has more params as order and type
		String playListId = getUploadsPlaylistId(channelId);
		try {
			YouTubeApiModels.YouTubeSearchResponse response = youtubeClient.fetchLatestVideos("snippet", playListId, 1, apiKey);
			return Optional.ofNullable(response);
		} catch (Exception e) {
			log.error("could not fetch video for playlist {}: {}", playListId, e.getMessage());
			return Optional.empty();
		}
	}

	private String getUploadsPlaylistId(String channelId) {
		if (channelId != null && channelId.startsWith("UC")) {
			return YT_PLAYLIST_PREFIX + channelId.substring(2);
		}
		return channelId;
	}
}
