package ch.nickl.tubefy.interfaces.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "youtube-api")
public interface YouTubeClient {

    @GET
    @Path("/playlistItems")
    @Retry(maxRetries = 3, delay = 1000)
    YouTubeApiModels.YouTubeSearchResponse fetchLatestVideos(
            @QueryParam("part") String part,
            @QueryParam("playlistId") String playlistId,
            @QueryParam("maxResults") int maxResults,
            @QueryParam("key") String apiKey
    );
}
