package ch.nickl.tubefy.interfaces.rest.mapper;

import ch.nickl.tubefy.application.dto.YouTubeSearchDto;
import ch.nickl.tubefy.application.dto.YouTubeVideoDto;
import ch.nickl.tubefy.interfaces.rest.YouTubeApiModels;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface YouTubeMapper {

    @Mapping(target = "videos", source = "items")
    YouTubeSearchDto toSearchDto(YouTubeApiModels.YouTubeSearchResponse response);

    @Mapping(target = "videoId", source = "snippet.resourceId.videoId")
    @Mapping(target = "title", source = "snippet.title")
    @Mapping(target = "publishedAt", source = "snippet.publishedAt")
    @Mapping(target = "thumbnailUrl", source = "snippet.thumbnails.high.url")
    YouTubeVideoDto toDto(YouTubeApiModels.YouTubeSearchResult searchResult);
}
