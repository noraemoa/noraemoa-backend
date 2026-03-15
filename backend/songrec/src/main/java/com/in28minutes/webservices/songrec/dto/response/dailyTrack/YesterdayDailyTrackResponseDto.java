package com.in28minutes.webservices.songrec.dto.response.dailyTrack;

import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrackEmotion;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class YesterdayDailyTrackResponseDto {
  private Long id;
  private Long userId;
  private String username;
  private Long trackId;
  private String spotifyId;
  private String name;
  private String artist;
  private String imageUrl;
  private LocalDate selectedDate;
  private String emotion;

  public static YesterdayDailyTrackResponseDto from(DailyTrack dailyTrack) {
    if(dailyTrack.getEmotion()==null) dailyTrack.setEmotion(DailyTrackEmotion.NONE);

    return YesterdayDailyTrackResponseDto.builder()
        .id(dailyTrack.getId())
        .userId(dailyTrack.getUser().getId())
        .username(dailyTrack.getUser().getUsername())
        .trackId(dailyTrack.getTrack().getId())
        .spotifyId(dailyTrack.getTrack().getSpotifyId())
        .name(dailyTrack.getTrack().getName())
        .artist(dailyTrack.getTrack().getArtist())
        .imageUrl(dailyTrack.getTrack().getImageUrl())
        .selectedDate(dailyTrack.getSelectedDate())
        .emotion(dailyTrack.getEmotion().name()).build();
  }
}
