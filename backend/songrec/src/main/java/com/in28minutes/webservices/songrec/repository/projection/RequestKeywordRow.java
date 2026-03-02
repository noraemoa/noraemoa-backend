package com.in28minutes.webservices.songrec.repository.projection;

public interface RequestKeywordRow {
    Long getRequestId();
    Long getKeywordId();
    String getRawText();
}
