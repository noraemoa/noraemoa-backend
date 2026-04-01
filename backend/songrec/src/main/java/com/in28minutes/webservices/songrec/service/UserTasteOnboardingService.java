package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.user.PreferenceTagCategory;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserPreferenceTag;
import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.UserProfilePayload;
import com.in28minutes.webservices.songrec.repository.UserPreferenceTagRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import com.in28minutes.webservices.songrec.service.openai.UserTasteProfileService;
import com.in28minutes.webservices.songrec.service.qdrant.UserProfileVectorIndexService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserTasteOnboardingService {
  private final UserRepository userRepository;
  private final UserPreferenceTagRepository userPreferenceTagRepository;
  private final UserTasteProfileService userTasteProfileService;
  private final UserProfileVectorIndexService userProfileVectorIndexService;

  @Transactional
  public void saveUserTasteProfile(Long userId, UserTasteProfileCreateRequestDto dto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    UserTasteProfileResult result = userTasteProfileService.generateProfile(dto);

    userPreferenceTagRepository.deleteAllByUser_Id(userId);

    List<UserPreferenceTag> tags = new ArrayList<>();
    addTags(tags, user, PreferenceTagCategory.MOOD, result.getPreferred_mood_tags(), false);
    addTags(tags, user, PreferenceTagCategory.SCENE, result.getPreferred_scene_tags(), false);
    addTags(tags, user, PreferenceTagCategory.TEXTURE, result.getPreferred_texture_tags(), false);
    addTags(tags, user, PreferenceTagCategory.GENRE, result.getPreferred_genre_tags(), false);
    addTags(tags, user, PreferenceTagCategory.MOOD, result.getDisliked_tags(), true);

    userPreferenceTagRepository.saveAll(tags);

    UserProfilePayload payload = UserProfilePayload.builder()
        .userId(user.getId())
        .preferred_mood_tags(result.getPreferred_mood_tags())
        .preferred_scene_tags(result.getPreferred_scene_tags())
        .preferred_texture_tags(result.getPreferred_texture_tags())
        .preferred_genre_tags(result.getPreferred_genre_tags())
        .disliked_tags(result.getDisliked_tags())
        .profile_summary(result.getProfile_summary())
        .build();

    Long vectorRef = userProfileVectorIndexService.upsertUserProfile(payload);

    user.setProfileSummary(result.getProfile_summary());
    user.setProfileVectorRef(vectorRef);
  }

  private void addTags(
      List<UserPreferenceTag> target,
      User user,
      PreferenceTagCategory category,
      List<String> values,
      boolean disliked
  ) {
    if (values == null) {
      return;
    }

    for (String value : values) {
      target.add(UserPreferenceTag.builder()
          .user(user)
          .category(category)
          .tagValue(value)
          .weight(1.0)
          .disliked(disliked)
          .build());
    }
  }
}
