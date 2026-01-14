package ch.nickl.tubefy.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class YouTubeApiModels {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YouTubeSearchResponse(List<YouTubeSearchResult> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YouTubeSearchResult(Snippet snippet) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(String title, String publishedAt, ResourceId resourceId, Thumbnails thumbnails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnails(Thumbnail high) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceId(String videoId) {
    }
}
