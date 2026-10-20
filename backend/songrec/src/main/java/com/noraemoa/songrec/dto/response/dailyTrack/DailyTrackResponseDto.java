package com.noraemoa.songrec.dto.response.dailyTrack;

import com.noraemoa.songrec.domain.dailyTrack.DailyTrack;
import com.noraemoa.songrec.domain.dailyTrack.DailyTrackEmotion;
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
  private String emotion;

  public static DailyTrackResponseDto from(DailyTrack dailyTrack) {
    if(dailyTrack.getEmotion()==null) dailyTrack.setEmotion(DailyTrackEmotion.NONE);
    return DailyTrackResponseDto.builder()
        .id(dailyTrack.getId())
        .userId(dailyTrack.getUser().getId())
        .trackId(dailyTrack.getTrack().getId())
        .spotifyId(dailyTrack.getTrack().getSpotifyId())
        .name(dailyTrack.getTrack().getName())
        .artist(dailyTrack.getTrack().getArtist())
        .imageUrl(dailyTrack.getTrack().getImageUrl())
        .selectedDate(dailyTrack.getSelectedDate())
        .emotion(dailyTrack.getEmotion().name()).build();
  }
}
