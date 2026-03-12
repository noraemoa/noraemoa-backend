package com.in28minutes.webservices.songrec.dto.response.dailyTrack;

import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DailyTrackResponseDto {
  private Long id;
  private Long userId;
  private Long trackId;
  private String spotifyId;
  private String name;
  private String artist;
  private String imageUrl;
  private LocalDate selectedDate;

  public static DailyTrackResponseDto from(DailyTrack dailyTrack) {
    return DailyTrackResponseDto.builder()
        .id(dailyTrack.getId())
        .userId(dailyTrack.getUser().getId())
        .trackId(dailyTrack.getTrack().getId())
        .spotifyId(dailyTrack.getTrack().getSpotifyId())
        .name(dailyTrack.getTrack().getName())
        .artist(dailyTrack.getTrack().getArtist())
        .imageUrl(dailyTrack.getTrack().getImageUrl())
        .selectedDate(dailyTrack.getSelectedDate()).build();
  }
}
