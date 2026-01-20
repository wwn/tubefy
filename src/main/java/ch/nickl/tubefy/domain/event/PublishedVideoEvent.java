package ch.nickl.tubefy.domain.event;

public record PublishedVideoEvent(String title, String videoId, String channelId, String publishedAt,
                                  String thumbnailUrl) {
    public String videoUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}