package ch.nickl.tubefy.domain.event;

public record PublishedVideoEvent(String title, String videoId) {
    public String videoUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}