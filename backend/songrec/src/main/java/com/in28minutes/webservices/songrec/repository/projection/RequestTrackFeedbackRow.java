package com.in28minutes.webservices.songrec.repository.projection;

public interface RequestTrackFeedbackRow {
  Long getTrackId();
  Long getRequestId();
  Double getAvgRating();
  Integer getRatingCount();
}
