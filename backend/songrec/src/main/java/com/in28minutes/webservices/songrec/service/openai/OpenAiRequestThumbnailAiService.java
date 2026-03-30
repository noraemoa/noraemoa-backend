package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiImageClient;
import com.in28minutes.webservices.songrec.service.RequestThumbnailAiService;
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
      Create a square thumbnail image for a music playlist app.

      Goal:
      - emotionally matched to the playlist
      - visually distinctive
      - clear and recognizable at small thumbnail size

      Visual style:
      - clean and modern
      - cinematic but minimal
      - polished digital illustration or stylized scene
      - soft lighting with clear contrast
      - not generic stock-art feeling
      - not overly abstract unless needed by the mood

      Composition rules:
      - square composition
      - one strong focal subject or one clear scene
      - balanced layout
      - strong visual anchor
      - simple background
      - suitable for music app thumbnail cards

      Strict constraints:
      - no text
      - no letters
      - no typography
      - no logo
      - no watermark
      - no collage
      - no split layout
      - no crowded composition

      Use the title and keywords as mood guidance only.
      Do not render any words inside the image.

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