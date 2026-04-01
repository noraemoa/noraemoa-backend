package com.in28minutes.webservices.songrec.service.qdrant;

import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.TrackSemanticSearchItemDto;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
import com.in28minutes.webservices.songrec.integration.qdrant.client.QdrantClient;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.QdrantRetrieveResponse;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.RerankedCandidate;
import com.in28minutes.webservices.songrec.repository.TrackLikeRepository;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import com.in28minutes.webservices.songrec.repository.projection.LikedTrackRow;
import com.in28minutes.webservices.songrec.service.openai.EmbeddingService;
import com.in28minutes.webservices.songrec.service.openai.TrackSearchQueryAnalysisService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackSemanticSearchService {
  private final TrackSearchQueryAnalysisService trackSearchQueryAnalysisService;
  private final EmbeddingService embeddingService;
  private final QdrantClient qdrantClient;
  private final TrackRepository trackRepository;
  private final TrackLikeRepository trackLikeRepository;
  private final UserRepository userRepository;

  public List<TrackSemanticSearchItemDto> search(Long userId,String query,int limit){

    // query 분석
    TrackSearchQueryAnalysisResult analysis =
        trackSearchQueryAnalysisService.analyze(query);

    // 임베딩
    String searchText = buildSearchText(analysis);
    List<Float> vector = embeddingService.embedText(searchText);

    // qdrant 후보
    QdrantSearchResponse response = qdrantClient.searchSong(vector,10);
    response.getResult().getPoints().forEach(r->log.info("후보곡:{}", r.getPayload().getTitle()));

    // 재정렬
    return rerank(response.getResult().getPoints(), userId,limit);
  }

  private List<TrackSemanticSearchItemDto> rerank(List<Point> response,Long userId, int limit){
    List<TrackSemanticSearchItemDto> results = new ArrayList<>();
    if(response ==null||response.isEmpty()){
      return results;
    }

    User user = userRepository.findById(userId).orElse(null);

    List<RerankedCandidate> selectedCandidates = new ArrayList<>();
    List<Point> filteredPoints = new ArrayList<>();

    List<Float> likedAverageVector =null;
    try{
      likedAverageVector=likedAverageVector(userId);
    }catch (Exception e){
    }

    List<Float> profileVector =null;
    try {
      if (user != null && user.getProfileVectorRef() != null ) {
        QdrantRetrieveResponse profileResponse =
            qdrantClient.retrieveUserProfilePoints(List.of(user.getProfileVectorRef()));

        if (profileResponse != null
            && profileResponse.getPoints() != null
            && !profileResponse.getPoints().isEmpty()
            && profileResponse.getPoints().get(0).getVector() != null) {
          profileVector = profileResponse.getPoints().get(0).getVector();
        }
      }
    } catch (Exception e) {
      profileVector = null;
    }


    List<Long> trackIds=response.stream().map(Point::getId).toList();
    Map<Long,Track> trackMap = trackRepository.findAllByIdIn(trackIds).stream()
        .collect(Collectors.toMap(Track::getId, Function.identity()));

    for(Point candidate:response){
      if(candidate.getVector()==null){
        continue;
      }
      boolean tooSimilar=false;


      for (Point filteredPoint : filteredPoints) {
        if(filteredPoint.getVector()==null) continue;

        double sim = cosineSimilarity(candidate.getVector(), filteredPoint.getVector());
        if (sim > 0.9) {
          tooSimilar = true;
          break;
        }
      }
      if(tooSimilar){
        continue;
      }

      double qdrantScore=candidate.getScore()==null?0.0:candidate.getScore();

      Long trackId=trackMap.get(candidate.getId()).getId();
      Track track = trackMap.get(trackId);
      if(track==null) continue;

      Long likedCount = trackLikeRepository.countByTrackId(trackId);
      double popularityScore=normalizePopularity(likedCount);

      double likedScore=0.0;
      if (likedAverageVector != null && candidate.getVector() != null) {
        likedScore = cosineSimilarity(candidate.getVector(), likedAverageVector);
      }

      double profileScore=0.0;
      if(profileVector!=null){
        profileScore = cosineSimilarity(candidate.getVector(), profileVector);
      }


      double finalScore= 0.55*qdrantScore + 0.20*profileScore+ 0.15*likedScore +0.10*popularityScore;
      filteredPoints.add(candidate);
      selectedCandidates.add(new RerankedCandidate(candidate,track,finalScore));
    }

    selectedCandidates.sort((a,b)->Double.compare(b.getFinalScore(),a.getFinalScore()));
    selectedCandidates.stream()
        .limit(limit)
        .forEach(c->results.add(TrackSemanticSearchItemDto.from(c.getTrack(),c.getFinalScore())));

    return results;
  }

  private double normalizePopularity(Long likedCount) {
    if (likedCount == null || likedCount <= 0) {
      return 0.0;
    }

    // 임시 기준: 좋아요 100개 정도면 거의 1.0에 가깝게
    double normalized = Math.log(1 + likedCount) / Math.log(101);
    return Math.min(1.0, normalized);
  }

  private List<Float> likedAverageVector(Long userId){
    List<Long> likedTracks = trackLikeRepository.findLikedTracks(userId).stream()
        .map(LikedTrackRow::getTrackId).toList();

    if(likedTracks.isEmpty())
      return null;

    List<List<Float>> trackVectors = qdrantClient.retrievePoints(likedTracks).getPoints().stream()
        .map(Point::getVector).filter(Objects::nonNull).toList();

    if(trackVectors.isEmpty())
      return null;

    return averageVectors(trackVectors);
  }

  public List<Float> averageVectors(List<List<Float>> vectors) {
    if (vectors == null || vectors.isEmpty()) {
      throw new IllegalArgumentException("vectors is empty");
    }

    int dimension = vectors.get(0).size();
    double[] sums = new double[dimension];

    for (List<Float> vector : vectors) {
      if (vector == null || vector.size() != dimension) {
        throw new IllegalArgumentException("vector size mismatch");
      }

      for (int i = 0; i < dimension; i++) {
        sums[i] += vector.get(i);
      }
    }

    List<Float> average = new ArrayList<>(dimension);
    int count = vectors.size();

    for (int i = 0; i < dimension; i++) {
      average.add((float) (sums[i] / count));
    }

    return average;
  }

  public double cosineSimilarity(List<Float> a, List<Float> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vector size mismatch");
    }

    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;

    for (int i = 0; i < a.size(); i++) {
      double va = a.get(i);
      double vb = b.get(i);

      dotProduct += va * vb;
      normA += va * va;
      normB += vb * vb;
    }

    if (normA == 0 || normB == 0) {
      return 0.0;
    }

    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }


  private Long extractTrackId(QdrantSearchResponse.Point point) {
    if (point.getPayload() != null && point.getPayload().getTrackId() != null) {
      return point.getPayload().getTrackId();
    }

    Object id = point.getId();
    if (id instanceof Number number) {
      return number.longValue();
    }

    try {
      return Long.parseLong(String.valueOf(id));
    } catch (Exception e) {
      return null;
    }
  }

  private String buildSearchText(TrackSearchQueryAnalysisResult result) {
    return String.format(
        "mood: %s, scene: %s, texture: %s, genre: %s. description: %s.",
        safeJoin(result.getMood_tags()),
        safeJoin(result.getScene_tags()),
        safeJoin(result.getTexture_tags()),
        safeJoin(result.getGenre_tags()),
        nullToEmpty(result.getShort_description())
    ).trim().replaceAll("\\s+", " ");
  }

  private String safeJoin(List<String> values) {
    return values == null || values.isEmpty() ? "" : String.join(", ", values);
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
