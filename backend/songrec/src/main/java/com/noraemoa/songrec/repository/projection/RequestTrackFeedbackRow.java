package com.noraemoa.songrec.repository.projection;

public interface RequestTrackFeedbackRow {
  Long getTrackId();
  Long getRequestId();
  Double getAvgRating();
  Integer getRatingCount();
}
