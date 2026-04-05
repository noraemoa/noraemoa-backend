package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiImageClient;
import com.in28minutes.webservices.songrec.service.RequestThumbnailAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class OpenAiRequestThumbnailAiService implements RequestThumbnailAiService {

  private final OpenAiImageClient openAiImageClient;

  @Override
  public byte[] generateThumbnail(String originalPrompt, String title, List<String> keywords) {

    String prompt = buildPrompt(originalPrompt, title, keywords);

    return openAiImageClient.generate(prompt).getImageBytes();
  }


  private static final List<String> THUMBNAIL_STYLE_VARIANTS = List.of(
      "soft anime-style illustration",
      "minimal watercolor painting",
      "dreamy pastel illustration",
      "soft cinematic illustration",
      "clean vector-like illustration with soft gradients"
  );

  private final Random random = new Random();

  private String buildPrompt(String originalPrompt, String title, List<String> keywords) {
    String styleHint = THUMBNAIL_STYLE_VARIANTS.get(
        random.nextInt(THUMBNAIL_STYLE_VARIANTS.size())
    );

    return """
    Create a square thumbnail image for a music playlist app.

    Goal:
    - emotionally matched to the playlist
    - visually distinctive
    - clear and recognizable at small thumbnail size

    Visual style:
    - soft, painterly digital illustration (NOT photorealistic)
    - warm and gentle atmosphere
    - smooth gradients and soft lighting
    - slightly dreamy and emotional tone
    - clean and minimal composition
    - visually appealing and aesthetic
    - vary composition and perspective for each image
    - avoid repeating the same scene structure

    Rendering style:
    - hand-painted digital illustration feel
    - soft brush strokes
    - pastel or muted color palette
    - subtle glow or soft light diffusion
    - avoid sharp, harsh contrast

    Additional style hint:
    - %s

    Avoid:
    - photorealism
    - realistic photography style
    - stock photo look
    - overly detailed textures (skin pores, noise, etc.)
    - hyper-real lighting
    - generic AI stock illustration

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
        styleHint,
        title,
        String.join(", ", keywords),
        originalPrompt
    );
  }
}