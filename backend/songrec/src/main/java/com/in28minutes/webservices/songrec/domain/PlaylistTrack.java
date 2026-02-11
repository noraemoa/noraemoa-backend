package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "playlist_track",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"playlist_id", "track_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Builder.Default
    @Column(nullable = false)
    private Boolean trackDeleted = false;
}
