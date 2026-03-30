package com.in28minutes.webservices.songrec.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_preference_tags")
@Getter
@Setter
public class UserPreferenceTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY,optional = false)
  @JoinColumn(name="user_id",nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false,length = 30)
  private PreferenceTagCategory category;

  @Column(nullable = false,length = 100)
  private String tagValue;

  @Column(nullable = false)
  private Double weight;

  @Column(nullable = false)
  private Boolean disliked;
}
