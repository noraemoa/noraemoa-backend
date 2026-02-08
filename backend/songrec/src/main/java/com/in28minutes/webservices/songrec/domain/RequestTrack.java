package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Builder.Default
    @Column(nullable = false)
    private Boolean trackDeleted = false;

    @Min(1) @Max(5)
    private Integer rating;
}
