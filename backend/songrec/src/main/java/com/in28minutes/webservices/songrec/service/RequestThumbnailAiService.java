package com.in28minutes.webservices.songrec.service;

import java.util.List;

public interface RequestThumbnailAiService {
  byte[] generateThumbnail(String originalPrompt, String title, List<String> keywords);
}