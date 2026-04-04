package com.in28minutes.webservices.songrec.domain.request;

import com.in28minutes.webservices.songrec.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "request_track_rating",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"request_track_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTrackRating {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "request_track_id",nullable = false)
  private RequestTrack requestTrack;

  @ManyToOne(optional = false,fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id",nullable = false)
  private User user;

  @Min(1) @Max(5)
  @Column(nullable = false)
  private Integer rating;
}
