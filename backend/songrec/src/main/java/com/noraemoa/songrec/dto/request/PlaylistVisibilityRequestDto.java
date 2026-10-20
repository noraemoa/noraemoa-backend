package com.noraemoa.songrec.dto.request;

import com.noraemoa.songrec.domain.playlist.PlaylistVisibility;
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
