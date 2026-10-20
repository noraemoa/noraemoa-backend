package com.noraemoa.songrec.repository.projection;

public interface RequestKeywordRow {
    Long getRequestId();
    Long getKeywordId();
    String getRawText();
}
