package ch.nickl.tubefy.interfaces.rest.dto;

import java.util.List;

public record YouTubeSearchDto(List<YouTubeVideoDto> videos) {
}
