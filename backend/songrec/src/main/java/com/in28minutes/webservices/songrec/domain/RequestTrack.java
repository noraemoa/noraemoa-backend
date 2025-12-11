package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_track",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"request_id", "track_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RequestTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id",nullable = false)
    private Request request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "track_id",nullable = false)
    private Track track;

}
