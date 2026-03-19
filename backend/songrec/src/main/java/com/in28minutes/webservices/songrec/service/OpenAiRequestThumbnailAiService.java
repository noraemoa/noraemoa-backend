package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiImageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiRequestThumbnailAiService implements RequestThumbnailAiService {

  private final OpenAiImageClient openAiImageClient;

  @Override
  public byte[] generateThumbnail(String originalPrompt, String title, List<String> keywords) {

    String prompt = buildPrompt(originalPrompt, title, keywords);

    return openAiImageClient.generate(prompt).getImageBytes();
  }

  private String buildPrompt(String originalPrompt, String title, List<String> keywords) {

    return """
        Create a square playlist thumbnail for a music app.

        Style:
        - soft, dreamy, pastel tones
        - minimal and clean composition
        - emotional, warm lighting
        - no text, no typography
        - modern UI friendly

        Playlist title:
        %s

        Keywords:
        %s

        User request:
        %s
        """.formatted(
        title,
        String.join(", ", keywords),
        originalPrompt
    );
  }
}