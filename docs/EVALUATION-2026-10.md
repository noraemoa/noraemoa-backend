# 추천 재정렬 가중치 비교 평가 (2026-10-22)

Qdrant 벡터 검색 후보 50곡을 재정렬(rerank)할 때, 가중치 설정에 따라 상위 10곡의 태그 적합도(NDCG@10)와 사용자 취향 유사도(likedSim@10)가 어떻게 달라지는지 비교했다.

## 결과

| 설정 | 가중치 (qdrant / profile / liked / popularity / feedback) | 취향 비중 (profile+liked) | precision@10 | NDCG@10 | likedSim@10 |
|---|---|---|---|---|---|
| `raw` (재정렬 없음) | Qdrant 유사도 순서 그대로 | - | 0.097 | **0.244** | 0.657 |
| `v1` | 0.55 / 0.20 / 0.15 / 0.10 / 0 | 0.35 | 0.067 | 0.201 | **0.713** |
| `v2_noFeedback` | 0.60 / 0.10 / 0.10 / 0.05 / 0 | 0.20 | 0.077 | 0.237 | 0.699 |
| `v2` (현재 운영) | 0.60 / 0.10 / 0.10 / 0.05 / 0.15 | 0.20 | 0.077 | 0.237 | 0.699 |

30개 쿼리 평균이다. 가중치는 `TrackSemanticSearchService.RerankWeights` 의 `V1`, `CURRENT_NO_FEEDBACK`, `CURRENT` 이다.

### 해석
- 재정렬을 하면 태그 적합도(NDCG@10)는 조금 내려가고, 사용자 취향 유사도(likedSim@10)는 올라간다. 둘은 서로 맞바꾸는 관계다.
- 취향 비중이 0.35 인 `v1` 은 취향 유사도가 가장 높지만(0.713) NDCG@10 이 가장 많이 떨어진다(0.244 → 0.201).
- 취향 비중을 0.20 으로 낮춘 `v2` 는 NDCG@10 을 재정렬 없음에 가깝게 유지하면서(0.237) 취향 유사도를 0.657 → 0.699 로 올린다.
- `v2` 와 `v2_noFeedback` 의 결과가 같은 것은, 로컬 DB 에 별점이 달린 추천곡(`request_track.rating_count > 0`)이 2,492개 중 1개뿐이라 별점 피드백 점수가 사실상 0 이었기 때문이다. 이번 평가로는 별점 피드백(0.15)의 효과를 판단할 수 없다.

## 측정 조건

- **사용자**: 1명(ID 15). 좋아요한 곡 16곡이며 16곡 모두 Qdrant `songs` 컬렉션에 벡터가 있다. likedSim@10 은 이 사용자 한 명의 취향 기준이다.
- **쿼리**: `RecommendationEvaluator.getQueries()` 에 고정된 30개(아래 목록). 30개 모두 후보가 있어 평가됐다(`evaluatedQueries=30 / 30`).
- **후보**: 쿼리마다 Qdrant `songs`(621곡)에서 벡터 유사도 상위 50곡을 가져와 네 설정에 똑같이 쓴다.
- **정답 태그(OpenAI 생성)**: 쿼리 쪽 태그는 평가 실행 중 OpenAI 질의 분석(`TrackSearchQueryAnalysisService.analyze`)이 만든 mood/scene/texture 태그다. 곡 쪽 태그는 카탈로그 수집 때 OpenAI 가 생성해 Qdrant payload 에 저장한 태그다. 사람이 직접 만든 정답 데이터가 아니다.
- **지표 정의**
  - 태그 일치율 = (쿼리 태그 중 곡 태그에 있는 개수) / (쿼리 태그 수)
  - precision@10: 상위 10곡 중 태그 일치율이 0.5 이상인 곡의 비율
  - NDCG@10: 태그 일치율을 등급(0.75 이상 3, 0.5 이상 2, 0.25 이상 1, 그 외 0)으로 바꿔 계산한다. 이상적인 순서(IDCG)는 **선택된 상위 10곡을 등급순으로 다시 정렬한 것**이라, 후보 50곡 전체가 아니라 상위 10곡 안의 순서만 평가한다.
  - likedSim@10: 상위 10곡 벡터와, 사용자가 좋아요한 곡 벡터 평균 사이의 코사인 유사도 평균
- **재현성**: 쿼리 태그를 실행할 때마다 OpenAI 가 새로 만들기 때문에, 같은 코드라도 실행마다 수치가 조금씩 달라질 수 있다. 좋아요, 별점, 카탈로그 같은 DB 상태가 바뀌어도 달라진다.
- **환경**: 로컬 MySQL(Docker `mysql:8-oracle`), Qdrant(Docker `qdrant/qdrant`), `local` 프로필, 커밋 `f5742a3`. 실행 시간 약 2분 43초.

## 실행 방법

`EvaluationRunner` 는 `@Component` 가 주석 처리되어 있어 앱을 띄워도 평가가 실행되지 않는다. 이번에는 저장소 코드를 바꾸지 않고, 저장소 밖에 둔 아래 실행 클래스를 앱의 런타임 classpath 로 실행했다. 웹 서버 없이 컨텍스트만 띄운 뒤 `runComparison()` 을 호출한다.

```java
public class EvalLauncher {
  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = new SpringApplicationBuilder(SongrecApplication.class)
        .web(WebApplicationType.NONE).run(args);
    try {
      ctx.getBean(RecommendationEvaluator.class).runComparison();
    } finally {
      ctx.close();
    }
  }
}
```

`backend/songrec` 에서 실행해야 `.env` 를 읽는다. MySQL, Qdrant 가 켜져 있어야 하고 OpenAI API 키가 필요하다(쿼리 30개만큼 질의 분석 API 를 호출한다).

## 로그 원문

```
2026-10-22T21:11:04.856+09:00  INFO 13728 --- [songrec] [           main] c.n.s.e.e.RecommendationEvaluator        : EVAL_COMPARE evaluatedQueries=30 / 30
2026-10-22T21:11:04.856+09:00  INFO 13728 --- [songrec] [           main] c.n.s.e.e.RecommendationEvaluator        : EVAL_COMPARE raw precision@10=0.09666666666666668 ndcg@10=0.2443388974115489 likedSim@10=0.6565282278601917
2026-10-22T21:11:04.857+09:00  INFO 13728 --- [songrec] [           main] c.n.s.e.e.RecommendationEvaluator        : EVAL_COMPARE v1 precision@10=0.06666666666666667 ndcg@10=0.20138438285001642 likedSim@10=0.7131704959963112
2026-10-22T21:11:04.857+09:00  INFO 13728 --- [songrec] [           main] c.n.s.e.e.RecommendationEvaluator        : EVAL_COMPARE v2_noFeedback precision@10=0.07666666666666667 ndcg@10=0.23659765565111623 likedSim@10=0.6991573578611242
2026-10-22T21:11:04.857+09:00  INFO 13728 --- [songrec] [           main] c.n.s.e.e.RecommendationEvaluator        : EVAL_COMPARE v2 precision@10=0.07666666666666667 ndcg@10=0.23659765565111623 likedSim@10=0.6991573578611242
```

## 평가 쿼리 (30개)

1. 비 오는 밤에 혼자 듣기 좋은 몽환적인 노래
2. 새벽 감성에 어울리는 잔잔한 노래
3. 운동할 때 듣기 좋은 강한 비트 음악
4. 드라이브할 때 듣기 좋은 신나는 노래
5. 이별 후에 듣기 좋은 슬픈 발라드
6. 공부할 때 집중 잘 되는 음악
7. 여름에 듣기 좋은 청량한 노래
8. 겨울 밤 감성적인 노래
9. 기분이 좋아지는 밝은 노래
10. 우울할 때 위로가 되는 노래
11. 여행 갈 때 듣기 좋은 설레는 노래
12. 카페에서 듣기 좋은 잔잔한 음악
13. 힙합 느낌 강한 에너지 넘치는 곡
14. 여성 보컬의 부드러운 감성 노래
15. 남성 보컬의 깊은 감성 발라드
16. 몽환적이고 신비로운 분위기의 음악
17. 강렬한 EDM 스타일 음악
18. 로파이 느낌의 편안한 음악
19. 기타 중심의 어쿠스틱 음악
20. 밤에 혼자 걸을 때 듣기 좋은 음악
21. 비 오는 날 창밖 보며 듣기 좋은 음악
22. 첫사랑 떠올리는 감성적인 노래
23. 파티 분위기 띄우는 신나는 음악
24. 차분하게 생각 정리할 때 듣는 음악
25. 스트레스 풀 때 듣기 좋은 강한 음악
26. 잔잔하지만 감정이 깊은 음악
27. 영화 OST 같은 분위기의 음악
28. 감성적인 피아노 중심 음악
29. 도시 야경 보며 듣기 좋은 음악
30. 아침에 상쾌하게 시작할 때 듣는 음악
