package com.in28minutes.webservices.songrec.domain.like;

import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "playlist_likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "playlist_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaylistLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
