package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.domain.user.PreferenceTagCategory;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.domain.user.UserPreferenceTag;
import com.noraemoa.songrec.dto.request.TrackSemanticSearchItemDto;
import com.noraemoa.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.noraemoa.songrec.dto.response.user.AggregatedTasteProfile;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.integration.openai.dto.UserTasteProfileResult;
import com.noraemoa.songrec.integration.qdrant.client.QdrantClient;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantRetrieveResponse;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.noraemoa.songrec.integration.qdrant.dto.RerankPrepareResultDto;
import com.noraemoa.songrec.integration.qdrant.dto.RerankedCandidate;
import com.noraemoa.songrec.integration.qdrant.dto.UserProfilePayload;
import com.noraemoa.songrec.repository.UserPreferenceTagRepository;
import com.noraemoa.songrec.repository.UserRepository;
import com.noraemoa.songrec.service.openai.UserTasteProfileService;
import com.noraemoa.songrec.service.profile.BalanceGameTagAggregator;
import com.noraemoa.songrec.service.qdrant.TrackSemanticSearchService;
import com.noraemoa.songrec.service.qdrant.UserProfileVectorIndexService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTasteOnboardingService {

  private final UserRepository userRepository;
  private final UserPreferenceTagRepository userPreferenceTagRepository;
  private final UserTasteProfileService userTasteProfileService;
  private final UserProfileVectorIndexService userProfileVectorIndexService;
  private final BalanceGameTagAggregator balanceGameTagAggregator;
  private final QdrantClient qdrantClient;
  private final TrackSemanticSearchService trackSemanticSearchService;


  @Transactional
  public UserTasteProfileResult saveUserTasteProfile(Long userId,
      UserTasteProfileCreateRequestDto dto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    AggregatedTasteProfile aggregated = balanceGameTagAggregator.aggregate(dto);
    UserTasteProfileResult result = userTasteProfileService.generateProfile(dto, aggregated);

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
    return result;
  }

  public List<TrackSemanticSearchItemDto> searchWelcomeSongs(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
    List<Float> vector=null;
    try {
      if ( user.getProfileVectorRef() != null ) {
        QdrantRetrieveResponse profileResponse =
            qdrantClient.retrieveUserProfilePoints(List.of(user.getProfileVectorRef()));

        if (profileResponse != null
            && profileResponse.getResult() != null
            && !profileResponse.getResult().isEmpty()
            && profileResponse.getResult().get(0).getVector() != null) {
          vector = profileResponse.getResult().get(0).getVector();
        }
      }
    } catch (Exception e) {
      log.info(e.getMessage());
      vector = null;
    }
    QdrantSearchResponse response = qdrantClient.searchSong(vector,10);
    RerankPrepareResultDto selectCandidateResult = trackSemanticSearchService.selectRerankedCandidates(
        vector,response.getResult().getPoints(), userId);
    List<RerankedCandidate> selectedCandidates = selectCandidateResult.getSelectedCandidates();

    return trackSemanticSearchService.rerank(selectedCandidates, 3);
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
