package com.in28minutes.webservices.songrec.dto.request;

import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistVisibilityRequestDto {
    @NotNull
    private PlaylistVisibility visibility;
}
