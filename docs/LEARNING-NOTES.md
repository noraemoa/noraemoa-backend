# Learning Notes

## 패키지명 변경(com.noraemoa.songrec)과 CI 워크플로 분리

### 개념: Java 패키지 네이밍 (역도메인 규칙)
- 패키지 이름은 소유자 도메인을 뒤집어 시작(`com.회사.프로젝트`)해서 전 세계적으로 클래스 이름이 겹치지 않게 하는 관례다.
- 강의 예제에서 출발해 `com.in28minutes.webservices.songrec` 으로 남아 있던 패키지를 `com.noraemoa.songrec` 으로 바꿨다. `src/main/java`, `src/test/java` 전체의 `package`/`import` 와 `build.gradle` 의 `group` 을 `com.noraemoa` 로 맞췄다.
- 안 바꾸면 남의 도메인(in28minutes) 이름으로 산출물이 만들어진다. jar 를 Maven 저장소에 올리거나 다른 모듈에서 가져다 쓸 때 "이 코드의 주인이 누구냐"가 헷갈리고, 포트폴리오 코드를 보는 사람에게도 예제를 그대로 복사한 프로젝트처럼 보인다.
- 대안: 그대로 두기(비용 0이지만 정체성 문제가 남음), `io.github.<계정>` 처럼 GitHub 도메인 쓰기(도메인 소유를 증명하기 쉬워 공개 배포에 유리함). 서비스 이름 noraemoa 가 이미 조직/저장소 이름이라 `com.noraemoa` 를 골랐다.
- 면접 30초: "처음에 강의 예제 프로젝트에서 시작해서 패키지가 강의 사이트 도메인으로 되어 있었습니다. 자바는 도메인을 뒤집어서 패키지를 짓는 관례가 있어서, 서비스 이름에 맞게 com.noraemoa.songrec 으로 정리했습니다. Gradle 의 group 도 같이 바꿔서 빌드 산출물 좌표까지 맞췄습니다. 작은 작업이지만 코드의 소유권과 정체성을 분명히 하려고 했습니다."

### 개념: `git mv` 와 rename 추적
- `git mv` 는 파일 이동과 스테이징을 한 번에 하는 명령이다. git 은 이동 자체를 저장하지 않고, 내용 유사도로 "이건 이름만 바뀐 파일"이라고 추론한다.
- `src/main/java/com/in28minutes/webservices/songrec` → `src/main/java/com/noraemoa/songrec` 디렉터리 단위로 `git mv` 했다. 그다음 내용은 package/import 줄만 치환해서 212개 파일이 모두 `R`(rename)로 잡혔다.
- 탐색기에서 옮기거나 지우고 새로 만들면, 한 커밋 안에서 내용까지 크게 바뀌는 순간 git 이 rename 이 아니라 "삭제 + 새 파일"로 볼 수 있다. 그러면 `git log --follow`, `git blame` 으로 `TrackSemanticSearchService` 같은 파일의 이전 이력을 따라갈 수 없게 된다.
- 대안: IntelliJ 의 Refactor > Rename package(편하지만 IDE 가 git 스테이징까지 깔끔하게 해 준다는 보장이 없고 결과를 재현하기 어려움). 명령으로 하면 같은 결과를 그대로 다시 만들 수 있어서 `git mv` + `sed` 를 택했다.
- 면접 30초: "패키지를 옮길 때 git mv 를 써서 git 이 파일을 rename 으로 인식하게 했습니다. git 은 이동을 따로 기록하지 않고 내용 유사도로 판단해서, 이동과 큰 수정을 한 커밋에 섞으면 이력이 끊길 수 있습니다. 그래서 이동하고 import 만 바꾸는 커밋으로 분리했고, 덕분에 blame 이나 log --follow 로 예전 이력을 계속 볼 수 있습니다."

### 개념: JPQL 문자열 안의 FQCN(전체 클래스 이름)
- JPQL 에서 enum 상수를 리터럴로 쓰려면 `com.패키지.Enum.VALUE` 처럼 전체 경로를 문자열로 적어야 한다.
- `PlaylistRepository`(공개 플레이리스트 조회 쿼리 2곳)와 `PlaylistLikeRepository`(2곳)의 `@Query` 안에 `...domain.playlist.PlaylistVisibility.PUBLIC` 이 있어서 함께 바꿨다.
- 이 부분은 그냥 문자열이라 IDE 리팩터링이나 컴파일러가 잡아 주지 않는다. 놓치면 컴파일은 성공하고, 앱 기동 때 Spring Data 가 쿼리를 검증하다가 "클래스를 찾을 수 없음"으로 실패한다. 이번에는 `SongrecApplicationTests`(전체 컨텍스트 테스트)가 이 검증까지 해 준다.
- 대안: `:visibility` 파라미터로 바꾸고 메서드 인자로 `PlaylistVisibility.PUBLIC` 넘기기(문자열 의존이 사라져 더 안전함). 이번 작업은 이름 변경만 하기로 해서 쿼리 구조는 바꾸지 않았고, 다음 개선 후보로 남겨 둔다.
- 면접 30초: "패키지명을 바꿀 때 import 말고도 JPQL 쿼리 문자열 안에 enum 의 전체 경로가 들어간 곳이 있었습니다. 문자열이라 컴파일러가 못 잡고 실행할 때야 터지기 때문에, 저장소 전체를 텍스트로 검색해서 남은 게 없는지 확인했습니다. 근본적으로는 파라미터 바인딩으로 바꾸면 이런 문자열 의존을 없앨 수 있다고 생각합니다."

### 개념: CI/CD 분리와 `workflow_dispatch`
- CI 는 "코드가 깨지지 않았는지" 빌드와 테스트로 확인하는 단계이고, CD 는 그 결과물을 서버에 배포하는 단계다. `workflow_dispatch` 는 GitHub Actions 워크플로를 Actions 탭에서 버튼으로 실행하게 해 주는 트리거다.
- `.github/workflows/deploy.yml` 의 `on:` 을 `push` 에서 `workflow_dispatch` 로 바꿨다. 새로 만든 `.github/workflows/ci.yml` 은 push 와 PR 때 JDK 17 로 `backend/songrec` 에서 `./gradlew test` 를 돌리고, 실패하면 테스트 리포트를 아티팩트로 올린다.
- 그대로 두면 EC2 가 꺼진 지금은 main 에 push 할 때마다 SCP 단계에서 실패해 빨간 X 가 뜬다. 진짜 테스트가 깨졌는지, 배포 서버가 없어서인지 구분할 수 없고, 실패에 익숙해지면 진짜 실패도 무시하게 된다. 또 전에는 테스트 없이 `bootJar` 만 만들어 배포했기 때문에 깨진 코드도 배포될 수 있었다.
- 대안: deploy.yml 에 `if: false` 를 넣거나 파일 삭제(삭제하면 EC2 를 다시 켰을 때 다시 만들어야 함), 배포 job 이 CI 성공 뒤에만 돌게 `needs:` 로 묶기(서버가 다시 살아나면 좋은 다음 단계). 지금은 서버가 없으니 수동 실행이 가장 단순하다.
- 면접 30초: "원래 main 에 push 하면 바로 EC2 로 배포했는데, 서버를 내린 뒤로 매번 Actions 가 실패로 떠서 진짜 문제와 구분이 안 됐습니다. 그래서 배포는 workflow_dispatch 로 수동 실행하게 바꾸고, push 와 PR 에는 빌드와 테스트만 하는 CI 워크플로를 따로 만들었습니다. 검증과 배포를 나누니까 테스트가 깨지면 바로 알 수 있게 됐습니다."

### 개념: `@WebMvcTest` 슬라이스 테스트와 `excludeFilters`
- `@WebMvcTest` 는 컨트롤러, `@ControllerAdvice`, Filter, `WebMvcConfigurer` 같은 웹 계층 bean만 띄우는 슬라이스 테스트다. 서비스, 리포지토리, JPA 는 뜨지 않는다.
- `config/OsivConfig.java` 는 `WebMvcConfigurer` 라서 슬라이스에 포함되는데, 생성자로 `EntityManagerFactory` 를 받는다. JPA 가 없는 슬라이스라 `AuthControllerTest`, `PlaylistControllerTest` 의 컨텍스트 로드가 `NoSuchBeanDefinitionException` 으로 실패했다. 두 테스트의 `@WebMvcTest` 에 `excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = OsivConfig.class)` 를 넣어 제외했다. `PlaylistControllerTest` 는 컨트롤러에 새로 생긴 `TrackService` 의존성도 `@MockBean` 으로 추가했다.
- 그대로 두면 로컬에서도 컨트롤러 테스트가 전부 깨진 채로 남고, CI 를 붙여도 항상 빨간불이라 CI 가 쓸모없어진다.
- 대안: 테스트에 `@MockBean EntityManagerFactory`(가짜 EMF 로 OSIV 인터셉터가 실제로 돌아서 요청 중 NPE 위험), OsivConfig 를 `ObjectProvider` 로 조건부 등록(운영 코드 수정이 필요함). 컨트롤러 슬라이스 테스트는 OSIV 와 관계가 없다는 의도를 가장 잘 드러내고 운영 코드를 안 건드리는 제외 방식을 골랐다.
- 면접 30초: "컨트롤러 테스트는 @WebMvcTest 로 웹 계층만 띄우는데, OSIV 설정 클래스가 WebMvcConfigurer 라서 같이 로드됐고, JPA 의 EntityManagerFactory 가 없어서 실패했습니다. 컨트롤러 테스트에는 OSIV 가 필요 없어서 excludeFilters 로 그 설정만 뺐습니다. 운영 코드는 건드리지 않고 테스트 범위만 정확히 맞춘 해결이라고 생각합니다."

### 개념: 테스트 전용 프로필 + H2 인메모리 DB
- `@ActiveProfiles("test")` 로 `src/test/resources/application-test.properties` 를 쓰게 해서, 외부 DB 대신 메모리 안의 H2 로 스프링 컨텍스트를 띄우는 방식이다.
- `SongrecApplicationTests`(@SpringBootTest)는 기본 `local` 프로필 때문에 localhost:3306 MySQL 에 연결하려다 실패했다. 기본값 없는 `${JWT_SECRET}`, `${OPENAI_API_KEY}` 같은 환경변수도 CI 에는 없다. 그래서 `build.gradle` 에 `testRuntimeOnly 'com.h2database:h2'` 를 추가하고, test 프로필에 H2(MySQL 호환 모드) 접속 정보와 더미 시크릿을 넣었다.
- 그대로 두면 MySQL 을 켜 둔 내 PC 에서만 통과하고, CI 나 다른 팀원 PC 에서는 항상 실패한다. 이번에도 로컬 MySQL 이 꺼져 있어서 `ConnectException` 이 났다.
- 대안: CI 에 MySQL service 컨테이너(운영 DB 와 같지만 CI 설정이 무거워지고 로컬에서는 여전히 MySQL 이 필요함), Testcontainers(가장 정확하지만 어디서든 Docker 가 필요함), 태그로 CI 에서 제외(검증이 빠짐). 지금 이 테스트는 "컨텍스트가 뜨는지"만 확인하고 네이티브 쿼리도 없어서 H2 로 충분하다고 판단했다.
- 면접 30초: "전체 컨텍스트 테스트가 로컬 MySQL 에 의존해서 CI 에서는 무조건 실패하는 상황이었습니다. 테스트 프로필을 따로 만들어 H2 인메모리 DB 와 더미 시크릿을 쓰게 했더니, 외부 환경 없이 어디서든 같은 결과가 나왔습니다. 다만 H2 와 MySQL 은 문법 차이가 있어서, 나중에 네이티브 쿼리가 늘어나면 Testcontainers 로 옮기는 걸 고려하고 있습니다."

### 개념: 기동 시 외부 호출하는 `CommandLineRunner` 를 `@MockBean` 으로 대체
- `@MockBean` 은 스프링 컨텍스트 안의 특정 bean을 Mockito 목으로 바꿔 끼운다. 목의 메서드는 아무것도 하지 않는다.
- `config/QdrantInitializer` 는 `@Component` 인 `CommandLineRunner` 라서 앱이 뜰 때 `QdrantCollectionService.ensureQueryEmbeddingCollection()` 으로 Qdrant(localhost:6333)에 HTTP 요청을 보낸다. 실패 처리가 없어서 Qdrant 가 없으면 컨텍스트가 실패한다. `SongrecApplicationTests` 에서 이 bean만 `@MockBean` 으로 바꿨다.
- 그대로 두면 H2 로 DB 문제를 해결해도 CI 에 Qdrant 가 없어서 같은 테스트가 또 실패한다.
- 대안: `QdrantClient` 전체를 목으로 바꾸기(범위가 더 넓어 다른 bean 동작까지 가림), `QdrantInitializer` 에 `@Profile("!test")` 붙이기(운영 코드에 테스트 사정이 들어감), CI 에 Qdrant 컨테이너 띄우기(무거움). 가장 좁은 범위만 바꾸는 방식을 골랐다.
- 면접 30초: "컨텍스트 테스트를 H2 로 바꿨는데도, 앱이 뜰 때 Qdrant 에 컬렉션을 만드는 초기화 코드가 있어서 외부 서버가 없으면 실패했습니다. 그 초기화 bean 하나만 @MockBean 으로 바꿔서, 컨텍스트 구성은 검증하면서 외부 호출은 막았습니다. 목으로 바꾸는 범위를 최소로 해야 테스트가 실제 구성을 최대한 그대로 검증할 수 있다고 생각했습니다."

## 추천 요청 생성 트랜잭션 분리와 썸네일 비동기화

### 개념: 트랜잭션 범위 = 커넥션 점유 범위 (그리고 OSIV)
- 스프링 `@Transactional` 은 메서드에 들어갈 때 풀에서 DB 커넥션을 하나 빌리고, 커밋/롤백할 때 돌려준다. 그래서 트랜잭션이 길면 그 시간만큼 커넥션을 붙잡는다.
- 원래 `RequestService.createRequest()` 는 메서드 전체가 `@Transactional` 이었고, 그 안에서 OpenAI 프롬프트 정제, 질의 분석, 임베딩, Qdrant 검색, 이미지 생성까지 했다. DB 작업은 수십 ms 인데 커넥션은 외부 API 를 기다리는 수 초~수십 초 동안 계속 잡혀 있었다. 또 OSIV(Open Session In View)가 켜져 있으면 트랜잭션이 끝나도 응답이 나갈 때까지 커넥션을 놓지 않기 때문에, `config/OsivConfig` 에서 `POST /requests` 만 OSIV 대상에서 뺐다.
- 그대로 두면 풀 크기(예: 10)보다 많은 사용자가 동시에 추천을 요청할 때 나머지 요청은 커넥션을 기다리다 타임아웃(기본 30초)이 나고, 추천과 상관없는 로그인이나 목록 조회 API 까지 같이 멈춘다.
- 대안: `hikari.maximum-pool-size` 를 늘리기(지금 30). 대기는 줄지만 DB 서버의 커넥션 한도와 메모리를 쓰고, 동시 요청이 30을 넘으면 같은 문제가 다시 생긴다. 원인(긴 트랜잭션)을 없애는 쪽을 골랐다.
- 면접 30초: "추천 API 가 메서드 전체를 트랜잭션으로 묶은 채 OpenAI 를 여러 번 호출하고 있어서, DB 는 거의 안 쓰는데 커넥션을 수 초씩 잡고 있었습니다. 동시 요청이 몰리면 커넥션 풀이 고갈돼서 다른 API 까지 느려지는 구조였습니다. 풀 크기를 늘리는 건 증상만 가리는 거라서, 외부 호출을 트랜잭션 밖으로 빼서 커넥션을 DB 작업 동안만 쓰도록 바꿨습니다."

### 개념: 개선 전에 측정부터 하기 (DataSource 프록시로 커넥션 점유 시간 재기)
- 코드를 바꾸기 전에 같은 방법으로 기준값을 남겨야 "얼마나 좋아졌는지"를 숫자로 말할 수 있다.
- `config/perf/ConnectionTrackingDataSourceConfig` 의 `BeanPostProcessor` 가 스프링의 `DataSource` 빈을 `DelegatingDataSource` 로 감싼다. `getConnection()` 에 걸린 시간은 대기 시간, 빌린 커넥션의 `close()`(풀 반납)까지 걸린 시간은 점유 시간으로 `ConnectionUsageTracker`(ThreadLocal)에 쌓는다. `config/perf/RequestCreatePerfFilter` 가 `POST /requests` 1건마다 `PERF_REQUEST_CREATE totalMs=.. connHoldTotalMs=..` 한 줄을 남긴다. `perf.connection-tracking.enabled=true` 인 local/test 프로필에서만 켜진다. 원본(큰 트랜잭션) 코드는 별도 git worktree 에서 같은 측정 코드로 따로 쟀다.
- 측정 없이 고치면 "빨라진 것 같다"로만 말할 수 있고, 개선이 정말 커넥션 점유 때문이었는지 아니면 이미지 생성 시간이 빠진 것뿐인지 구분할 수 없다.
- 대안: 트랜잭션 매니저를 상속해 시작/종료 시점을 재기(트랜잭션 밖 자동 커밋 조회는 못 잡음), Hikari 메트릭 + Actuator/Prometheus(정확하지만 요청 1건 단위로 보기 어렵고 설정이 무거움), p6spy 같은 라이브러리(의존성 추가). 의존성 없이 요청 단위로 "커넥션을 실제로 붙잡은 시간"을 바로 볼 수 있어서 DataSource 프록시를 골랐다.
- 면접 30초: "개선 전에 기준값부터 쟀습니다. DataSource 를 프록시로 감싸서 커넥션을 빌린 시점부터 반납할 때까지의 시간을 요청 단위로 합산하고, 응답 시간과 같이 로그로 남겼습니다. 같은 프롬프트로 원본, 중간 버전, 최종 버전을 각각 세 번씩 호출해서 비교했고, 그래서 개선 효과를 숫자로 설명할 수 있었습니다."
- 측정 결과 (2026-10-20, 로컬 MySQL + Qdrant + 실제 OpenAI, 프롬프트 "비 오는 날 혼자 차 마시면서 듣기 좋은 노래", 순차 4회 중 1회차는 워밍업이라 제외, 단위 ms)

  | 버전 | 회차 | totalMs | connHoldTotalMs | connHoldMaxMs | connWaitTotalMs |
  |---|---|---|---|---|---|
  | A 원본(큰 트랜잭션 1개) | 2 | 23495 | 23472 | 23472 | 0 |
  | A | 3 | 21621 | 21598 | 21598 | 2 |
  | A | 4 | 20901 | 20861 | 20861 | 0 |
  | **A 평균** | | **22006** | **21977** | **21977** | **0.7** |
  | B 현재 HEAD(짧은 트랜잭션 4개) | 2 | 19575 | 802 | 331 | 18 |
  | B | 3 | 18926 | 1099 | 377 | 44 |
  | B | 4 | 21913 | 1349 | 577 | 16 |
  | **B 평균** | | **20138** | **1083** | **428** | **26** |

  구간별 시간: `PERF_SUMMARY` 는 검색 구간이고, 나머지는 로그 타임스탬프 차이로 계산했다. 평균, 단위 ms.

  | 버전 | 정제 등 검색 전 | 검색(`PERF_SUMMARY totalMs`) | 검색 후(썸네일 생성+저장, DB 저장) |
  |---|---|---|---|
  | A | ~1928 | 4396 (analyzeMs 평균 2128) | ~15682 |
  | B | ~2430 | 3987 (analyzeMs 평균 2372) | ~13721 |

- 해석
  - 원본(A)은 `connCount=1` 이고 점유 시간이 응답 시간의 99.9%다. 커넥션 1개를 요청 처음부터 끝까지 붙잡는다는 뜻이다. 그 22초 중 가장 긴 구간은 **썸네일 이미지 생성(약 15.7초, 전체의 약 71%)** 이고, 다음은 트랙 검색(약 4.4초, 그중 OpenAI 질의 분석 analyzeMs 가 약 2.1초로 가장 큼), 프롬프트 정제(약 1.9초) 순이다. DB 작업 자체는 이 중 수백 ms 정도다.
  - 현재 HEAD(B)는 커넥션을 10번 짧게 빌린다. 점유 시간 합계가 21977 → 1083ms 로 **약 95% 줄었다**. 그래서 동시 요청이 몰려도 풀이 고갈될 가능성이 크게 낮다.
  - 하지만 응답 시간은 22.0 → 20.1초로 약 8%만 줄었다. 사용자가 여전히 이미지 생성(약 13.7초)을 기다리기 때문이다. 다음 단계에서 썸네일을 비동기로 바꾸는 근거가 된다.
  - `connWaitTotalMs` 는 A, B 모두 0~44ms 다. 요청을 순차로 1건씩 보냈기 때문에 대기가 생길 상황 자체가 없었다. 대기 시간 차이는 동시 요청 테스트(4단계)에서 따로 보여 준다. B의 대기 값이 조금 큰 것은 커넥션을 10번 빌리면서 생기는 수 ms 의 획득 비용이 쌓인 것이다.
  - 표본이 3회뿐이고 OpenAI 응답 시간이 회차마다 수 초씩 흔들려서, 응답 시간 비교는 대략적인 경향으로만 봐야 한다. 커넥션 점유 시간은 차이가 20배라 흔들림과 상관없이 결론이 같다.

### 측정: 동시 요청 시 커넥션 풀 고갈 재현 (`ConnectionPoolContentionTest`)
- 조건: H2, `hikari.maximum-pool-size=2`, `connection-timeout=3000`, OpenAI 프롬프트 정제 모킹 응답 2초, 동시 요청 5개(워밍업 1회 후). 같은 테스트 파일을 원본 코드(baseline worktree, 커밋하지 않음)와 수정 후 코드에서 각각 3회 돌렸다.

  | 버전 | 회차 | 성공 | 실패(커넥션 타임아웃) | 최대 커넥션 대기(ms) | 대기 합계(ms) | 5건 처리 시간(ms) |
  |---|---|---|---|---|---|---|
  | 원본(큰 트랜잭션) | 1 | 4 | 1 | 3019 | 7652 | 4750 |
  | 원본 | 2 | 4 | 1 | 3017 | 7288 | 4253 |
  | 원본 | 3 | 4 | 1 | 3052 | 7258 | 4265 |
  | 수정 후 | 1 | 5 | 0 | 253 | 429 | 3963 |
  | 수정 후 | 2 | 5 | 0 | 54 | 184 | 2346 |
  | 수정 후 | 3 | 5 | 0 | 109 | 358 | 2544 |

  (수정 후 값은 2026-10-21 최종 코드(대기 기준 1000ms 미만)로 다시 잰 것이다. 전날 대기 기준을 바꾸기 전에 잰 값(최대 대기 68~272ms, 실패 0건)과 경향이 같다.)

- 해석
  - 원본은 3회 모두 **5건 중 1건이 커넥션 타임아웃**(`CannotCreateTransactionException` ← `SQLTransientConnectionException`)으로 실패했다. 성공한 요청 중 2건도 **약 2.1초씩 커넥션을 기다렸다**. 앞 요청 2개가 OpenAI 응답(2초)을 기다리는 동안 커넥션 2개를 모두 쥐고 있었기 때문이다. 요청별 로그를 보면 2건은 대기 0ms, 2건은 대기 약 2100ms, 1건은 3000ms를 기다리다 실패했다. "풀 2개 → 2개씩 차례로 처리 → 3초를 넘긴 마지막 요청은 실패" 하는 구조가 숫자로 그대로 보인다.
  - 수정 후에는 OpenAI 를 기다리는 동안 커넥션을 쥐지 않으므로 5건이 모두 성공했고, 최대 대기는 54~253ms 였다. 1회차 253ms 와 처리 시간 3963ms 는 JVM 을 새로 띄운 직후라 콜드 스타트 비용이 섞인 값이다. 2·3회차 기준으로 5건 처리 시간이 약 4.3초 → 2.4초로, 거의 OpenAI 지연(2초) 수준까지 줄었다.
  - 결론: 풀 크기를 늘리지 않아도, 트랜잭션 범위를 DB 작업으로 좁히는 것만으로 같은 풀에서 동시 요청을 문제없이 처리한다.

### 다음에 이어서 할 일 (2026-10-20 중단 시점)
- 완료(wip 커밋에 포함): 측정 코드(`config/perf`), 검색과 Qdrant 업서트 분리 + 업서트를 커밋 후로 이동, DB 쓰기를 트랜잭션 1개(`RequestWriteService.saveRequest`)로 통합, 검색 실패 시 503(`ExternalApiException`)으로 실패시키고 아무것도 저장하지 않기, 썸네일 비동기화(`AsyncConfig`, `RequestThumbnailEventListener`), 테스트(`RequestCreateFlowTest` 4개, `ConnectionPoolContentionTest`), `.gitignore`에 `*.log`·`uploads/` 추가, `run.log`·`uploads/` 추적 해제.
- 남은 일
  1. ~~`./gradlew test` 전체를 다시 돌려 모두 통과하는지 확인하기.~~ 완료(2026-10-21): 35개 테스트 모두 통과, 실패 0건, 건너뜀 0건.
  2. ~~2단계, 3단계 개념 정리~~ 완료(2026-10-21): 아래 "개념" 6개로 추가. 원래 메모: self-invocation 과 프록시, 외부 호출 순서(검색 먼저, Qdrant 업서트는 커밋 후), 외부 API 실패 시 부분 저장 방지, `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`, 전용 `ThreadPoolTaskExecutor`(기본 executor 를 쓰지 않는 이유, 거절 정책).
  3. ~~5단계 C 측정~~ 완료(2026-10-21): 아래 "측정: 수정 후(C) 실측과 A / B / C 비교" 참고. 원래 메모: 최종 버전(C)을 A/B 와 같은 방법(로컬 bootRun, `perf-test@noraemoa.local`, 같은 프롬프트로 4회 호출 중 2~4회 기록)으로 측정해 A / B / C 비교표 만들기. 썸네일 완료 시간은 `THUMBNAIL_DONE` 로그로 따로 기록한다.
  4. 로그 정리: 유지할 것(`PERF_REQUEST_CREATE`, `PERF_SUMMARY`, `QDRANT_QUERY_UPSERT`, `THUMBNAIL_DONE/FAILED`, 썸네일 거절 warn)과 운영에서 끌 것(DataSource 프록시, `perf.connection-tracking.enabled`)을 정리하기.
  5. 프론트엔드 변경 사항 요약하기(응답의 `thumbnailUrl` 이 처음에는 null → 플레이스홀더를 보여 주고 `GET /requests/{id}` 를 다시 조회).
  6. `hikari.maximum-pool-size=30` 을 줄일지 측정 결과를 보고 판단하기.
  7. 마무리: baseline worktree(`D:\git-work\noraemoa-backend-baseline`) 삭제(`git worktree remove --force`), wip 커밋을 최종 커밋 메시지("refactor: 추천 요청 생성 시 외부 API 호출을 트랜잭션 밖으로 분리하고 썸네일 생성을 비동기로 전환")로 정리하기.

### 개념: `@Transactional` 프록시와 self-invocation(내부 호출) 문제
- 스프링은 `@Transactional` 이 붙은 빈을 프록시 객체로 감싸고, 외부에서 그 빈의 메서드를 부를 때 프록시가 끼어들어 트랜잭션을 시작/커밋한다. 같은 클래스 안에서 `this.save()` 처럼 부르면 프록시를 거치지 않으므로 `@Transactional` 이 무시된다.
- `RequestService.createRequest()` 는 트랜잭션 없이 외부 API 를 호출하고, DB 쓰기만 짧은 트랜잭션으로 묶어야 한다. 쓰기 메서드를 `RequestService` 안의 `@Transactional private/public` 메서드로 만들면 내부 호출이 되어 트랜잭션이 열리지 않는다. 그래서 쓰기를 별도 빈 `RequestWriteService.saveRequest()` 로 분리하고, `RequestService` 가 주입받은 빈(=프록시)을 통해 호출하게 했다. 썸네일 반영도 `RequestWriteService.updateThumbnail()` 로 같은 이유로 분리했다.
- 적용하지 않으면 트랜잭션이 없는 상태에서 Keyword, Request, RequestTrack 저장이 각각 자동 커밋된다. 예를 들어 RequestTrack 저장 중 예외가 나면 Request 와 Keyword 는 이미 커밋돼 남는다. 게다가 코드에는 `@Transactional` 이 붙어 있어서 겉보기에는 문제를 알아채기 어렵다.
- 대안: `TransactionTemplate` 으로 코드 안에서 트랜잭션 범위를 직접 지정하기(정확하지만 비즈니스 코드에 트랜잭션 코드가 섞인다), 자기 자신을 `@Lazy` 로 주입받거나 `AopContext.currentProxy()` 쓰기(동작은 하지만 구조가 꼬여 보이고 실수하기 쉽다), AspectJ 위빙 모드(설정이 무겁다). "외부 호출 담당"과 "DB 쓰기 담당"으로 역할을 나누는 의미도 있어서 별도 빈 분리를 골랐다.
- 면접 30초: "스프링 트랜잭션은 프록시 기반이라 같은 클래스 안에서 메서드를 부르면 @Transactional 이 적용되지 않습니다. 추천 요청 생성에서 외부 API 호출은 트랜잭션 밖에 두고 DB 저장만 트랜잭션으로 묶어야 했는데, 같은 클래스에 두면 트랜잭션이 안 걸립니다. 그래서 DB 쓰기만 담당하는 RequestWriteService 를 따로 만들어 프록시를 거쳐 호출되게 했습니다. 역할도 분리돼서 어디까지가 트랜잭션인지 코드만 보고 알 수 있게 됐습니다."

### 개념: 외부 호출 순서 정하기 (검색 먼저, Qdrant 질의 벡터 저장은 커밋 후)
- 외부 시스템(Qdrant)에 쓰는 작업이 DB 에서 만든 ID 를 필요로 할 때, DB 커밋과 외부 쓰기의 순서를 정해 두 저장소가 어긋나지 않게 하는 설계다.
- Qdrant 질의 포인트는 `requestId` 를 ID 로 쓴다. 다음 요청이 "과거 비슷한 질의 → 그 요청 곡들의 별점"을 찾는 데 쓰이기 때문이다. 원래는 Request 를 먼저 커밋해 ID 를 얻고, 검색 도중에 업서트했다. `TrackSemanticSearchService.search()` 에서 업서트를 빼서 질의 벡터와 검색 텍스트만 돌려주게 하고, 새 메서드 `upsertQueryPoint()` 를 `RequestService.createRequest()` 가 `saveRequest()` 커밋 이후에 호출하도록 바꿨다. 업서트가 실패하면 `log.warn` 만 남긴다. 이 포인트가 없어도 이번 추천 결과는 정상이고, 이후 추천의 피드백 신호만 조금 약해진다.
- 적용하지 않으면 검색 중간에 실패했을 때 Qdrant 에는 질의 포인트가 남고 DB 에는 트랙 없는 Request 만 남는다. 또 자기 자신의 포인트를 먼저 넣은 뒤 "비슷한 과거 질의"를 찾기 때문에 검색 결과에 자기 자신이 섞였다(피드백이 없어 점수에는 영향이 없었지만 불필요한 결과다).
- 대안: Request 를 먼저 커밋하고 실패하면 지우는 보상 로직(트랜잭션이 2개로 나뉘고, 보상이 실패하면 데이터가 반쯤 남는다), UUID 같은 ID 를 미리 발급하기(스키마 변경이 필요하다), Outbox 테이블로 Qdrant 업서트를 확실히 보장하기(가장 안전하지만 이번 범위에 비해 무겁다). 업서트가 실패해도 다음 추천의 품질만 조금 떨어지는 데이터라서 "커밋 후 저장 + 실패 시 로그"로 충분하다고 판단했다.
- 면접 30초: "Qdrant 에 질의 벡터를 저장할 때 DB 의 requestId 가 필요해서, 원래는 요청을 먼저 커밋하고 검색 도중에 저장했습니다. 그러면 검색이 실패했을 때 DB 와 Qdrant 에 반쪽짜리 데이터가 남았습니다. 그래서 검색은 저장 없이 먼저 끝내고, DB 저장이 커밋된 뒤에 Qdrant 에 저장하도록 순서를 바꿨습니다. Qdrant 저장은 실패해도 추천 결과에는 영향이 없어서 로그만 남기도록 했습니다."

### 개념: 외부 API 실패 시 부분 저장 막기 (Fail fast + 단일 트랜잭션)
- DB 에 쓰기 전에 실패할 수 있는 외부 작업을 모두 끝내고, DB 쓰기는 한 트랜잭션으로 묶어서 "전부 저장되거나 아무것도 저장되지 않게" 만드는 방식이다.
- `RequestService.createRequest()` 는 외부 API 실패를 성격에 따라 다르게 처리한다. 프롬프트 정제(제목/키워드)는 없어도 추천이 가능하므로 실패하면 `fallbackTitle()`(프롬프트 앞 30자)로 계속 진행한다. 트랙 검색은 추천의 핵심이므로 실패하면 `ExternalApiException` 을 던지고 `GlobalExceptionHandler` 가 503 으로 응답한다. 이 시점까지 DB 쓰기가 한 번도 없어서 남는 데이터가 없다. DB 쓰기는 `RequestWriteService.saveRequest()` 트랜잭션 하나라서 저장 도중 실패해도 전부 롤백된다.
- 적용하지 않으면(원래 HEAD) 검색 실패 예외를 삼키고 트랙 0개짜리 Request 를 저장했다. 사용자는 빈 플레이리스트를 받고, 피드에도 빈 요청이 남는다. 다시 시도하면 같은 요청이 하나 더 쌓인다.
- 대안: 지금처럼 빈 결과로 저장하기(사용자가 빈 결과를 성공으로 오해한다), 실패한 요청을 상태값(`FAILED`)으로 저장하고 나중에 재시도하기(재시도 큐가 필요하다). 503 은 클라이언트가 "잠시 후 다시 시도"로 처리하기 쉬운 응답이라 골랐다.
- 면접 30초: "외부 API 가 실패했을 때 데이터가 반쯤 저장되지 않도록, 실패할 수 있는 외부 호출을 DB 쓰기 전에 모두 끝내도록 순서를 바꿨습니다. 제목 생성처럼 없어도 되는 기능은 기본값으로 대체하고, 곡 검색처럼 핵심 기능이 실패하면 아무것도 저장하지 않고 503 을 돌려줍니다. DB 저장은 트랜잭션 하나로 묶어서 중간에 실패해도 전부 롤백됩니다. 테스트에서 검색이 실패하면 Request 와 Keyword 가 0건인 것까지 확인했습니다."

### 개념: `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` 로 후속 작업 분리
- 트랜잭션이 커밋에 성공한 뒤에만 이벤트를 받아서, 그 작업을 별도 스레드에서 실행하는 방식이다. 롤백되면 이벤트가 전달되지 않는다.
- `RequestWriteService.saveRequest()` 가 트랜잭션 안에서 `RequestCreatedEvent`(requestId, 프롬프트, 제목, 키워드)를 발행한다. `RequestThumbnailEventListener.generateThumbnail()` 이 `AFTER_COMMIT` + `@Async("thumbnailExecutor")` 로 받아 이미지 생성 → 파일 저장 → `updateThumbnail()`(새 트랜잭션) 순서로 처리한다. 응답은 `thumbnailUrl = null` 로 바로 나가고, 실패하면 `THUMBNAIL_FAILED` 를 `log.error` 로 남긴 뒤 추천 결과는 그대로 둔다. 이벤트에는 엔티티 대신 값만 넣었다. 다른 스레드에서 영속성 컨텍스트 없이 LAZY 필드를 읽다 생기는 예외를 피하기 위해서다.
- 적용하지 않으면 사용자가 이미지 생성(실측 약 13~15초)을 기다리며 응답이 20초를 넘는다. 트랜잭션 커밋 전에 이벤트를 처리하면 롤백된 요청에 대해 이미지를 만들거나, 아직 커밋되지 않아 다른 스레드에서 Request 가 안 보이는 문제가 생긴다.
- 대안: 일반 `@EventListener` + `@Async`(커밋 전에 실행될 수 있어 위 문제가 생긴다), `@Async` 없이 `AFTER_COMMIT` 만 쓰기(같은 스레드에서 돌아 응답이 다시 느려진다. 또 커밋이 끝난 트랜잭션에 묶여 있어서 `REQUIRES_NEW` 없이는 썸네일 반영 쓰기가 저장되지 않는 함정이 있다), 메시지 큐(Kafka, SQS)(서버가 죽어도 작업이 남는 장점이 있지만 인프라가 늘어난다). 썸네일은 실패해도 사용자가 직접 올릴 수 있는 부가 기능이라 메모리 기반 비동기로 충분하다고 봤다. 대신 서버가 재시작되면 대기 중인 썸네일 작업이 사라진다는 한계가 있다.
- 면접 30초: "썸네일 이미지 생성이 응답 시간의 70% 정도를 차지해서 비동기로 바꿨습니다. 요청 저장 트랜잭션이 커밋된 뒤에만 실행되도록 @TransactionalEventListener 의 AFTER_COMMIT 을 쓰고, @Async 로 별도 스레드에서 돌렸습니다. 그래서 저장이 롤백되면 이미지도 만들지 않고, 이미지 생성이 실패해도 추천 결과에는 영향이 없습니다. 다만 메모리 큐라서 서버가 재시작되면 작업이 사라지는데, 썸네일은 부가 기능이라 그 정도는 감수했습니다."

### 개념: `@Async` 전용 `ThreadPoolTaskExecutor` (기본 executor 를 쓰지 않는 이유)
- `@Async` 작업을 실행할 스레드 풀을 용도별로 직접 만들어, 스레드 수·큐 크기·넘칠 때의 처리를 명시하는 설정이다.
- `config/AsyncConfig` 에 `@EnableAsync` 와 `thumbnailExecutor` 를 두었다(core 2, max 4, 큐 50, 스레드 이름 `thumbnail-`). 큐가 꽉 차면 작업을 버리고 `log.warn` 을 남긴다. 종료할 때는 진행 중인 작업을 최대 30초 기다린다. 반환값이 없는 `@Async` 메서드의 예외는 `AsyncUncaughtExceptionHandler` 로 `log.error` 를 남긴다.
- 적용하지 않으면(스프링 부트 기본 `applicationTaskExecutor`) 큐 크기가 무제한(`Integer.MAX_VALUE`)이다. OpenAI 이미지 API 가 느려지면 작업이 메모리에 끝없이 쌓이고, 큐가 차지 않으니 스레드도 늘지 않아 밀린 작업이 계속 늘어난다. 다른 `@Async` 작업과 스레드를 같이 써서 느린 이미지 생성이 다른 작업까지 막는다. 스프링만 쓸 때의 기본값인 `SimpleAsyncTaskExecutor` 는 작업마다 스레드를 새로 만들어 요청이 몰리면 스레드가 폭증한다.
- 대안: 거절 정책으로 `CallerRunsPolicy`(작업을 버리지 않지만 요청 스레드가 이미지 생성을 직접 하게 되어 응답이 다시 20초가 된다), `AbortPolicy`(예외가 요청 쪽으로 전파된다), 가상 스레드(스레드 수 걱정은 줄지만 OpenAI 호출 수 제한은 여전히 필요하다). 썸네일은 없어도 되는 부가 기능이라 "버리고 기록"을 골랐다. 스레드 수를 작게 잡은 것은 OpenAI 이미지 API 의 요청 한도(rate limit)도 고려한 것이다.
- 면접 30초: "@Async 를 기본 설정으로 쓰면 큐 크기가 무제한이라, OpenAI 가 느려질 때 작업이 메모리에 계속 쌓일 수 있습니다. 그래서 썸네일 전용 스레드 풀을 만들어 스레드 2~4개, 큐 50개로 제한했습니다. 큐가 넘치면 요청 스레드로 떠넘기지 않고 경고 로그만 남기고 버리게 했는데, 썸네일은 없어도 추천 결과에는 문제가 없는 부가 기능이기 때문입니다."

### 개념: "트랜잭션 밖에서 실행된다"를 테스트로 증명하기
- 외부 API 목(mock) 안에서 `TransactionSynchronizationManager.isActualTransactionActive()` 를 기록해, 호출 시점에 트랜잭션이 열려 있었는지를 직접 확인하는 테스트 기법이다.
- `RequestCreateFlowTest.externalCallsRunOutsideTransaction()` 은 프롬프트 정제, 질의 분석, 임베딩, Qdrant 검색·업서트, 썸네일 생성 목 안에서 트랜잭션 활성 여부를 모아 모두 `false` 인지 확인한다. Qdrant 업서트 목에서는 트랜잭션 없는 새 조회로 Request 가 보이는지(=이미 커밋됐는지)도 확인하고, 썸네일은 `thumbnail-` 스레드에서 실행되는지 확인한다. `ConnectionPoolContentionTest` 는 풀 2개 + OpenAI 지연 2초 + 동시 요청 5개로 풀 고갈을 재현한다. 같은 파일을 원본 코드(baseline worktree)에서도 돌려서 "수정 전에는 타임아웃이 나고 수정 후에는 안 난다"를 숫자로 보여 준다.
- 적용하지 않으면 누군가 나중에 `createRequest()` 에 `@Transactional` 을 다시 붙여도 기능 테스트는 모두 통과해 회귀를 모른다. 성능 문제는 트래픽이 몰려야 드러나기 때문이다.
- 대안: 로그를 눈으로 확인하기(자동 검증이 안 된다), 수정 전 구조를 테스트 안에서 `TransactionTemplate` 으로 흉내 내기(처음 계획이었지만 흉내일 뿐이라, 실제 원본 코드를 같은 테스트로 돌리는 쪽이 더 설득력 있다고 판단했다), Testcontainers MySQL(운영과 같은 DB 지만 의존성이 늘어난다. 풀 고갈은 DB 종류와 상관없어서 기존 H2 로 충분했다). 동시성 테스트는 첫 호출의 콜드 스타트 비용이 대기 시간처럼 보이는 문제가 있어서 워밍업 1회를 넣었다. 대기 기준은 "OpenAI 지연의 절반 미만(1000ms)"으로 잡아 실행 환경 차이로 테스트가 흔들리지 않게 했다.
- 면접 30초: "외부 API 호출이 트랜잭션 밖에서 일어나는지를 테스트로 고정해 두고 싶었습니다. 그래서 OpenAI 와 Qdrant 를 목으로 바꾸고, 목이 호출되는 순간 트랜잭션이 열려 있는지를 기록해 모두 false 인지 확인했습니다. 추가로 커넥션 풀 2개에 동시 요청 5개를 보내는 테스트를 만들어, 수정 전 코드에서는 매번 1건이 타임아웃 나고 수정 후에는 대기가 거의 없다는 것을 숫자로 비교했습니다."

### 측정: 수정 후(C) 실측과 A / B / C 비교
- 방법은 A, B 와 같다(2026-10-21, 로컬 MySQL + Qdrant + 실제 OpenAI, `perf-test@noraemoa.local`, 같은 프롬프트로 순차 4회 호출, 1회차는 워밍업이라 제외, 단위 ms). C 는 이번 작업의 최종 코드다(트랜잭션 1개 + 커밋 후 Qdrant 업서트 + 썸네일 비동기).

  | 버전 | 회차 | totalMs | connHoldTotalMs | connHoldMaxMs | connWaitTotalMs |
  |---|---|---|---|---|---|
  | C 최종 | 2 | 6910 | 417 | 260 | 5 |
  | C | 3 | 10186 | 476 | 312 | 4 |
  | C | 4 | 6019 | 376 | 280 | 4 |
  | **C 평균** | | **7705** | **423** | **284** | **4.3** |

  A / B / C 평균 비교:

  | 버전 | 구조 | 응답 totalMs | 커넥션 점유 합계 | 가장 긴 점유 1건 | 커넥션 획득 횟수 |
  |---|---|---|---|---|---|
  | A 원본 | 큰 트랜잭션 1개, 썸네일 동기 | 22006 | 21977 | 21977 | 1 |
  | B 중간(이전 HEAD) | 짧은 트랜잭션 4개, 썸네일 동기 | 20138 | 1083 | 428 | 10 |
  | C 최종 | 트랜잭션 1개, 썸네일 비동기 | **7705** | **423** | **284** | 7 |
  | A → C 변화 | | **-65%** | **-98%** | -99% | |

  C 의 비동기 썸네일 생성 시간(`THUMBNAIL_DONE ms`, 응답과 별개로 백그라운드에서 걸린 시간):

  | 회차 | requestId | THUMBNAIL_DONE ms |
  |---|---|---|
  | 2 | 3818 | 12592 |
  | 3 | 3819 | 14055 |
  | 4 | 3820 | 13805 |
  | **평균** | | **13484** |

  C 구간별 시간(평균, 로그 타임스탬프로 계산): 정제 등 검색 전 약 3193, 검색(`PERF_SUMMARY`) 4200, 검색 후(DB 저장 + Qdrant 업서트) 약 312.

- 해석
  - 응답 시간: 22.0초 → 7.7초로 약 65% 줄었다. 줄어든 시간은 거의 썸네일 생성 시간(약 13.5초)이다. 이제 사용자는 "정제 + 검색 + 저장"만 기다리고, 이미지는 응답 뒤 약 13.5초 만에 백그라운드에서 완성돼 DB 에 반영된다. 응답 직후 `GET /requests/{id}` 로 4건 모두 `thumbnailUrl` 이 채워진 것을 확인했다.
  - 3회차(10186ms)는 프롬프트 정제(OpenAI)에만 약 5.0초가 걸려서 길었다. 다른 회차는 약 2.2~2.4초였다. 코드가 아니라 OpenAI 응답 편차이고, 이 회차를 빼면 응답 평균은 약 6.5초다.
  - 커넥션 점유: 21977 → 423ms(약 98% 감소). B 보다도 1083 → 423ms 로 더 줄었다. 트랜잭션 4개를 1개로 합쳤고, 썸네일 반영 쓰기가 요청 스레드에서 빠졌고, 저장 후 재조회 트랜잭션이 없어져서 획득 횟수가 10 → 7 로 줄었기 때문이다.
  - 남은 응답 시간의 대부분은 OpenAI 호출 2번(정제 약 2~3초, 질의 분석 약 2.6초)이다. 더 줄이려면 정제와 검색을 병렬로 실행하거나 정제를 비동기로 바꾸는 방법이 있다. 다만 제목이 응답에 필요한지는 기획과 함께 판단해야 해서 이번 범위에서는 하지 않았다.
  - 한계: 표본이 버전마다 3회이고 실제 OpenAI 를 써서 응답 시간은 수 초씩 흔들린다. 커넥션 점유는 수십 배 차이라 결론이 흔들리지 않는다.
- 면접 30초: "개선 전후를 같은 방법으로 세 번씩 재서 비교했습니다. 커넥션 점유 시간은 요청당 22초에서 0.4초로 98% 줄었고, 응답 시간은 썸네일 생성을 비동기로 돌려서 22초에서 7.7초로 65% 줄었습니다. 커넥션 풀 2개에 동시 요청 5개를 보내는 테스트에서도 수정 전에는 매번 한 건이 타임아웃 났는데, 수정 후에는 대기가 거의 없었습니다."

## 추천 요청 관련 API 의 소유권 검증 및 관리자 권한 검증 누락 수정

### 점검 결과 (2026-10-21)
| 구분 | API | 수정 전 | 조치 |
|---|---|---|---|
| 관리자 확인 없음 | `DELETE /requests/admin/{id}` | 로그인 사용자 누구나 남의 요청 삭제 가능 | `@PreAuthorize("hasRole('ADMIN')")` → 일반 사용자 403 |
| 소유자 확인 없음 | `POST /requests/{id}/tracks` | 남의 요청에 Spotify 곡 추가 가능 | 소유권 확인 → 404 |
| | `POST /requests/{id}/tracks/{trackId}` | 남의 요청에 곡 추가 가능 | 소유권 확인 → 404 |
| | `POST /requests/{id}/keywords/{keywordId}` | 남의 요청에 키워드 추가 가능 | 소유권 확인 → 404 |
| | `DELETE /requests/{id}/keywords/{keywordId}` | 남의 요청의 키워드 삭제 가능 | 소유권 확인 → 404 |
| 공개 범위 확인 없음(점검 중 발견) | `GET /playlists/{id}`, `GET /playlists/{id}/tracks` | 남의 **비공개** 플레이리스트와 곡 목록 조회 가능 | 이미 있던 `findAccessiblePlaylist`(공개 또는 본인) 사용 → 404 |
| 의도한 동작(그대로) | `PUT /requests/{id}/tracks/{trackId}/rating` | 남의 요청 곡에도 별점 가능 | 피드에서 다른 사람의 추천에 별점을 주는 기능이라 유지. 별점은 사용자별로 저장되어 남의 별점을 덮어쓰지 않는다 |
| 이번 범위 밖(그대로) | `POST /tracks`, `POST /keywords` | 로그인 사용자 누구나 공용 곡/키워드 추가 가능 | 초기 데이터 설정용 API 라 지금은 손대지 않음 |
| 정상 | `PATCH`/`DELETE /requests/{id}`, 썸네일 업로드, `DELETE /requests/{id}/tracks/{trackId}`, 플레이리스트 수정·삭제·곡 추가/삭제·공개 설정, `/users/**`, DailyTrack, 좋아요(공개 플레이리스트만), 취향 프로필 | 소유자 확인 또는 `@PreAuthorize`, 토큰의 본인 userId 만 사용 | - |
| 공개(설계상) | `GET /requests/{id}`, `/{id}/tracks`, `/{id}/keywords`, `/requests/feed`, 공개/인기 플레이리스트, 어제의 곡 | 피드 기능 | - |

- 검증: `RequestAuthorizationTest` 12개. 운영 코드 수정만 잠시 되돌린 상태(stash)에서 같은 테스트를 돌리면 **"거절돼야 하는" 7개가 모두 실패**했다(남의 요청에 곡·키워드 추가 201, 키워드 삭제·관리자 삭제 204, 남의 비공개 플레이리스트 200). 수정 후에는 12개 모두 통과하고, 전체 테스트 47개도 통과한다.

### 개념: 객체 수준 권한 검증 (IDOR 막기)
- 로그인했는지(인증)와 별개로, "이 사용자가 **이 데이터**를 다룰 수 있는지"를 요청마다 확인하는 것이다. 이걸 빠뜨려서 URL 의 ID 만 바꾸면 남의 데이터에 접근되는 취약점을 IDOR(Insecure Direct Object Reference)라고 한다. OWASP API Security Top 10 의 1번(BOLA)이 같은 문제다.
- 곡 추가(`RequestTrackService.addTrackByRequest`, `addSpotifyTrackToRequest`), 키워드 추가/삭제(`RequestKeywordService.addKeywordByRequest`, `deleteKeywordByRequestId`)가 `requestId` 만 받고 주인을 확인하지 않았다. 컨트롤러는 `@AuthenticationPrincipal` 로 로그인 사용자를 받아 놓고 서비스에 넘기지 않았다. 이제 모두 `userId` 를 받아 `RequestOwnershipService.getOwnedRequest(userId, requestId)` 로 확인한다. 플레이리스트 조회도 같은 문제여서 `PlaylistService.getAccessiblePlaylist(userId, playlistId)` 가 "공개이거나 본인 것"만 돌려주도록 바꿨다. 검증을 넣기 전에 내부 호출 경로도 확인했다. 추천 요청 생성(`RequestWriteService.saveRequest`)이 같은 `addTrackByRequest` 로 추천 곡을 저장하고 있어서, 여기서도 요청을 만든 사용자의 `userId` 를 넘기게 했다.
- 적용하지 않으면, 요청 ID 는 1, 2, 3 처럼 순서대로 늘어나는 숫자라서 로그인한 아무 사용자가 ID 를 하나씩 바꿔 가며 남의 추천 플레이리스트에 이상한 곡을 넣거나 키워드를 지울 수 있다. 비공개로 설정한 플레이리스트도 ID 만 알면 그대로 보였다.
- 대안: 컨트롤러에서 확인하기(API 마다 같은 코드를 반복하고, 서비스를 다른 곳에서 부르면 검증이 빠진다), `@PreAuthorize("@requestSecurity.isOwner(#requestId)")` 같은 SpEL 메서드 보안(선언적이라 깔끔하지만 확인 쿼리와 실제 조회가 따로 돌고, 기존 404 정책과 맞추기 어렵다), ID 를 UUID 로 바꾸기(추측은 어려워지지만 권한 검증을 대신하지는 못한다). 기존 `getActiveRequest`, `getOwnedPlaylist` 와 같은 "서비스에서 소유자 조건으로 조회" 방식을 골라 코드 스타일을 통일했다.
- 면접 30초: "로그인만 확인하고 그 데이터의 주인인지는 확인하지 않아서, 요청 ID 만 바꾸면 남의 추천 플레이리스트에 곡이나 키워드를 넣고 뺄 수 있는 문제가 있었습니다. 소유권 확인을 서비스 계층의 공통 메서드 하나로 모으고, 수정 API 가 모두 그 메서드를 거치게 했습니다. 점검하다가 비공개 플레이리스트도 남이 조회할 수 있다는 걸 발견해서 같이 고쳤습니다. 수정 전 코드에서는 실패하고 수정 후에는 통과하는 테스트로 확인했습니다."

### 개념: 권한이 없을 때 403 과 404 중 무엇을 줄까
- 403 Forbidden 은 "그 데이터는 있지만 너는 권한이 없다", 404 Not Found 는 "그런 데이터는 없다"는 뜻이다. 권한이 없을 때 일부러 404 를 주면 데이터가 있는지 자체를 숨길 수 있다.
- 남의 요청이나 비공개 플레이리스트에 접근하면 `RequestOwnershipService.getOwnedRequest`, `PlaylistService.getAccessiblePlaylist` 가 `NotFoundException`(404)을 던진다. 기존 `getActiveRequest`, `getOwnedPlaylist` 가 쓰던 정책을 그대로 유지했다. 반대로 관리자 API 는 "데이터의 주인"이 아니라 "역할"의 문제라서 403 을 쓴다. 관리자 API 가 있다는 사실은 숨길 이유가 없고, 프론트에서도 권한 문제임을 알 수 있어야 하기 때문이다.
- 403 을 주면 공격자가 ID 를 바꿔 가며 "403 이 나오는 ID = 실제로 있는 남의 요청"이라는 정보를 얻는다. 비공개 플레이리스트라면 "이 ID 의 비공개 플레이리스트가 있다"는 사실 자체가 드러난다.
- 대안으로 403 을 쓰면 원인이 분명해서 디버깅과 문의 대응이 쉽고, 프론트에서 "권한 없음" 안내를 따로 보여 줄 수 있다. 대신 존재 여부가 드러난다. GitHub 도 권한이 없는 비공개 저장소에는 404 를 주는 것처럼, 존재를 숨기는 쪽이 일반적이라 404 를 유지했다. 다만 404 의 진짜 이유가 로그에 남지 않으므로, 필요해지면 서버 로그를 따로 남기는 방법을 검토할 수 있다.
- 면접 30초: "권한이 없는 남의 데이터에 접근하면 403 대신 404 를 주도록 했습니다. 403 을 주면 '이 ID 의 데이터가 있긴 하다'는 정보를 주게 돼서, ID 를 바꿔 가며 남의 데이터가 있는지 확인할 수 있기 때문입니다. 반면 관리자 전용 API 는 데이터가 아니라 역할의 문제라 403 을 그대로 썼습니다. 403 이 디버깅에는 더 편하다는 단점은 알고 있습니다."

### 개념: `@PreAuthorize` 메서드 보안과 403 응답이 만들어지는 경로
- `@EnableMethodSecurity` 를 켜면 `@PreAuthorize("hasRole('ADMIN')")` 가 붙은 메서드를 프록시가 감싸고, 호출 전에 현재 사용자의 권한(`ROLE_ADMIN`)을 확인한다. `hasRole('ADMIN')` 은 자동으로 `ROLE_` 를 붙여 비교한다.
- `RequestService.deleteRequestAdmin()` 에 붙였다. `UserService.getUsers()` 와 같은 방식이다. `UserRole` 은 `USER`, `ADMIN` 두 값이고, `JwtAuthFilter` 가 토큰의 role 을 `"ROLE_" + role` 권한으로 넣어 주기 때문에 별도 설정 없이 동작한다. 403 응답이 실제로 어디서 만들어지는지 확인해 보니, `@PreAuthorize` 가 거부하면 `AuthorizationDeniedException` 이 컨트롤러 호출 안에서 발생하고, 시큐리티 필터까지 가기 전에 `GlobalExceptionHandler.handleAccessDenied`(`@RestControllerAdvice`)가 먼저 잡아 `{"error":"FORBIDDEN"}` 으로 응답한다. `RestAccessDeniedHandler`(`{"message":"권한이 없습니다."}`)는 `authorizeHttpRequests` 같은 URL 단위 규칙에서 거부될 때만 쓰인다. 테스트에서 응답 본문의 `$.error == "FORBIDDEN"` 까지 확인했다.
- 적용하지 않으면 URL 에 `/admin` 이 들어 있어도 실제로는 아무 검사가 없어서, 로그인한 일반 사용자가 남의 요청을 지울 수 있었다(수정 전 테스트에서 204). 이름만 보고 "관리자만 쓰겠지"라고 생각하기 쉬운 부분이다.
- 대안: `SecurityConfig` 에서 `.requestMatchers("/requests/admin/**").hasRole("ADMIN")` 로 URL 단위로 막기(한눈에 보이지만, 서비스를 다른 경로에서 부르면 막히지 않는다. 이때는 `RestAccessDeniedHandler` 가 응답한다), 서비스 안에서 `if (!isAdmin) throw` 로 직접 확인하기(반복되고 테스트하기 어렵다). 프로젝트가 이미 서비스 메서드에 `@PreAuthorize` 를 쓰고 있어서 같은 방식으로 맞췄다. 두 403 응답 본문의 형식이 서로 다르다는 점은 나중에 하나로 맞추면 좋겠다.
- 면접 30초: "관리자 삭제 API 가 URL 에만 admin 이 붙어 있고 실제 권한 검사가 없어서, 일반 사용자도 남의 요청을 지울 수 있었습니다. 프로젝트에서 이미 쓰던 방식대로 서비스 메서드에 @PreAuthorize hasRole ADMIN 을 붙였습니다. 테스트하면서 이 403 은 시큐리티의 AccessDeniedHandler 가 아니라 전역 예외 처리기가 만든다는 걸 알게 됐고, 응답 본문까지 확인하는 테스트로 고정했습니다."

### 개념: 공통 검증 로직을 별도 빈으로 분리해 순환 참조 피하기
- 여러 서비스가 함께 쓰는 로직을, 서로를 의존하지 않는 작은 빈으로 빼서 의존 방향이 한쪽으로만 흐르게 하는 설계다. 스프링 부트 2.6부터는 빈 사이에 순환 참조가 있으면 기본 설정에서 앱이 뜨지 않는다.
- 소유권 확인은 원래 `RequestService.getActiveRequest()` 에 있었다. 그런데 `RequestTrackService` 가 이걸 쓰려고 `RequestService` 를 주입받으면 `RequestService → RequestWriteService → RequestTrackService → RequestService` 로 순환이 생긴다. 그래서 리포지토리만 의존하는 `RequestOwnershipService` 를 만들고, `RequestService.getActiveRequest()` 도 이 빈을 호출하도록 바꿨다. 기존 호출부는 그대로 두고, 검증 로직은 한곳에만 있다.
- 적용하지 않으면 앱이 뜰 때 순환 참조 오류(`BeanCurrentlyInCreationException`)로 실패한다. 이를 피하려고 서비스마다 같은 `findByIdAndUserIdAndDeletedFalse(...).orElseThrow(...)` 를 복사하면, 나중에 정책(예: 404 → 403, 관리자 예외)을 바꿀 때 한 곳을 빠뜨리기 쉽다.
- 대안: `spring.main.allow-circular-references=true`(문제를 숨길 뿐이다), `@Lazy` 주입(동작은 하지만 의존 구조가 꼬인 채 남는다), 리포지토리에 default 메서드로 넣기(예외 정책이 리포지토리로 내려가 계층이 섞인다). 역할이 분명한 작은 빈이 가장 단순하다고 판단했다.
- 면접 30초: "소유권 확인을 공통 메서드로 모으려는데, 기존 메서드가 있는 서비스를 그대로 가져다 쓰면 서비스끼리 순환 참조가 생겨서 앱이 뜨지 않는 구조였습니다. 그래서 리포지토리만 의존하는 작은 검증 전용 서비스를 만들고, 기존 메서드도 이걸 호출하게 했습니다. 의존 방향이 한쪽으로만 흐르고, 정책을 바꿀 때도 한 곳만 고치면 됩니다."

### 개념: 권한 테스트는 실제 DB 와 보안 필터로, 그리고 "수정 전에는 실패하는지" 확인하기
- 권한 테스트는 상태 코드만이 아니라 "거절된 요청이 데이터를 바꾸지 않았는지"까지 확인해야 의미가 있다. 그래서 서비스를 목으로 바꾸지 않고 실제 서비스와 DB 로 실행한다.
- `RequestAuthorizationTest` 는 `@SpringBootTest` + `@AutoConfigureMockMvc` + H2 로 보안 필터와 `@PreAuthorize` 까지 실제로 거친다. 인증 사용자는 spring-security-test 의 `authentication(...)` 으로 `JwtAuthFilter` 와 같은 모양(`JwtPrincipal` + `ROLE_*`)을 넣었다. 다른 사용자의 호출은 404 와 함께 DB 에 RequestTrack, RequestKeyword, Track 이 생기지 않았는지(삭제는 그대로 남았는지) 확인하고, 본인 호출은 정상 동작을 확인한다. 운영 코드만 stash 로 되돌리고 같은 테스트를 돌려, "거절돼야 하는" 7개가 실제로 실패하는 것도 확인했다.
- 기존 컨트롤러 테스트처럼 `@WebMvcTest` + 서비스 목으로 하면 목이 돌려주는 값만 확인하게 되어, 서비스 안의 권한 검증이 빠져도 테스트가 통과한다. 이번 버그가 바로 서비스 안에 검증이 없던 문제라 이 방식으로는 잡을 수 없다.
- 대안: 서비스 단위 테스트에서 `NotFoundException` 만 확인하기(빠르지만 컨트롤러가 `principal.userId()` 를 제대로 넘기는지는 확인하지 못한다), 실제 JWT 를 발급해 헤더로 보내기(가장 실제와 같지만 테스트가 토큰 발급 코드에 묶인다). 필터 체인과 DB 는 모두 거치면서 토큰 발급과는 분리되는 `authentication(...)` 방식을 골랐다.
- 면접 30초: "권한 테스트는 403 이나 404 가 나오는지뿐 아니라 데이터가 실제로 안 바뀌었는지도 봐야 한다고 생각해서, 서비스를 목으로 바꾸지 않고 H2 와 실제 보안 필터로 테스트했습니다. 그리고 수정 코드를 잠시 되돌린 상태에서 같은 테스트를 돌려 실제로 실패하는지도 확인했습니다. 수정 전에는 남의 요청에 곡을 넣는 요청이 201 로 성공했는데, 수정 후에는 404 가 나고 데이터도 그대로였습니다."

## 비밀정보 유출 대응 (저장소 공개 전 정리)

### 점검 결과 (2026-10-22)
| 비밀정보 | 어디에 있었나 | 원격(GitHub)까지 올라갔나 | 대응 |
|---|---|---|---|
| EC2 접속 키 `noraemoa-key.pem` | 테스트 코드 커밋에 함께 커밋됨 | 예 (main, test/add-tests 등) | 키 무효화(AWS 계정 만료로 이미 무효), 추적 해제, 이력에서 파일 삭제 |
| 로컬 DB 비밀번호 | `application-local.properties` (커밋 3개) | 예 | `${DB_PASSWORD}` 로 바꾸고 값은 `.env` 로 이동, 이력에서 `***REMOVED***` 로 치환 |
| 예전 `JWT_SECRET` | 예전에 커밋됐던 `.env` | 예 | `.env` 를 이력에서 삭제, 새 JWT 키로 교체 |
| OpenAI, Spotify, AWS 접근 키 | 로컬 `.env` 에만 있음 | 아니요 (`sk-`, `AKIA` 패턴 검색 0건) | 커밋된 적 없음을 확인 |

- 검증: 정리 후 `git log --all -p` 에서 RSA 개인키 헤더 문자열, 예전 DB 비밀번호, 예전 JWT 값이 모두 0건, 이력에 pem/.env 경로 0건. `./gradlew test` 47개 모두 통과. (노트에 키 헤더 문자열을 그대로 적었더니 검사에 1건으로 잡혀서, 문서에도 그 문자열은 적지 않았다.)
- 정리 전 저장소는 폴더 복사본과 `git clone --mirror` 로 로컬에만 백업했다(옛 이력과 비밀값이 그대로 있으므로 절대 올리지 않는다).

### 개념: 비밀정보 유출 대응 순서 (무효화 → 코드에서 분리 → 이력 정리 → 재발 방지)
- 저장소에 비밀값이 올라갔을 때 "이미 누군가 가져갔을 수 있다"고 가정하고, 그 값을 쓸모없게 만드는 것부터 하는 절차다. 이력 삭제는 그다음 일이다.
- 이번에는 ① 키가 실제로 유효한지 확인(EC2 키는 AWS 계정 만료로 이미 무효, JWT 키는 새로 발급, 예전 DB 비밀번호는 다른 곳에서 쓰지 않는지 확인) ② 설정 파일에서 값을 빼서 `.env` 로 옮김(`application-local.properties` 의 `spring.datasource.password=${DB_PASSWORD}`) ③ `git rm --cached` 로 `noraemoa-key.pem` 추적 해제 ④ `git filter-repo` 로 모든 브랜치 이력에서 pem 과 `.env` 를 지우고 DB 비밀번호 문자열을 치환 ⑤ `.gitignore`, `.env.example` 로 다시 커밋되지 않게 막는 순서로 진행했다. 점검은 파일 이름(`git log --all --name-only`)과 내용(`git log --all -p` 에서 키 패턴 검색) 두 방향으로 했다.
- 이력만 지우고 키를 그대로 쓰면 소용이 없다. GitHub 에 한 번 올라간 커밋은 force push 후에도 fork, PR, 다른 사람의 clone, 캐시에 남아 해시 URL 로 한동안 열릴 수 있고, 공개 저장소는 비밀값을 자동으로 수집하는 봇이 몇 분 안에 긁어 간다. 반대로 키를 바꾸지 않고 "최신 커밋에서만 지우면" 이력에는 그대로 남아 `git log -p` 한 번이면 보인다.
- 대안: 저장소를 지우고 새로 만들기(가장 확실하지만 커밋 기록, 이슈, PR 이 모두 사라진다), 비밀값이 있는 커밋을 `git revert` 하기(이력에 그대로 남으므로 대응이 아니다), 키만 바꾸고 이력은 두기(보안상으로는 충분하지만, 이력서에 링크하는 저장소에 무효 키라도 남아 있으면 관리가 허술해 보인다). 이번에는 키 무효화를 기본으로 하고, 공개 전에 이력도 정리했다.
- 면접 30초: "저장소를 공개하기 전에 점검해 보니 EC2 접속 키와 DB 비밀번호, 예전 JWT 키가 커밋 이력에 남아 있었습니다. 이력을 지워도 이미 복제됐을 수 있어서, 먼저 키가 무효인지 확인하고 JWT 키는 새로 바꿨습니다. 그다음 설정값을 환경변수로 분리하고 git filter-repo 로 모든 브랜치 이력에서 파일을 지우고 비밀번호 문자열을 치환했습니다. 마지막에 git log 전체에서 키 패턴이 0건인지 확인했습니다."

### 개념: `git filter-repo` 로 이력 다시 쓰기
- 저장소의 모든 커밋을 처음부터 다시 만들면서 특정 파일을 빼거나 문자열을 바꾸는 도구다. git 공식 문서가 `git filter-branch` 대신 쓰라고 권한다.
- `py -m git_filter_repo --invert-paths --path backend/songrec/noraemoa-key.pem --path backend/songrec/.env --replace-text <치환규칙파일> --force` 로 실행했다. 치환 규칙 파일(`예전값==>***REMOVED***`)은 저장소 밖 임시 폴더에 두고 작업 후 지웠다. 원격에만 있던 `test/add-tests` 브랜치는 로컬 브랜치를 먼저 만들어 같이 정리했다. filter-repo 는 실수로 옛 원격에 섞어 push 하지 않도록 `origin` remote 를 지우므로, 다시 추가한 뒤 force push 해야 한다. 이력에서 `.env` 가 사라지면서 ".env 추적 해제" 커밋은 빈 커밋이 되어 자동으로 없어졌다.
- 이력을 다시 쓰면 그 지점 이후 모든 커밋 해시가 바뀐다. 그래서 force push 가 필요하고, 같은 저장소를 쓰는 사람은 다시 clone 해야 한다. 옛 clone 에서 그냥 push 하면 지운 이력이 다시 올라온다.
- 대안: `git filter-branch`(느리고 실수하기 쉬워 git 문서에서도 권하지 않는다), BFG Repo-Cleaner(빠르고 쉽지만 Java 실행 파일이 따로 필요하고, 기본으로 최신 커밋은 건드리지 않는다), `git rebase -i` 로 직접 고치기(커밋 수가 많고 머지 커밋이 있으면 비현실적이다). pip 으로 바로 설치되고 경로 삭제와 문자열 치환을 한 번에 할 수 있는 filter-repo 를 골랐다.
- 면접 30초: "이력에서 비밀값을 지울 때 git filter-repo 를 썼습니다. 파일 경로 삭제와 문자열 치환을 한 번에 모든 브랜치에 적용할 수 있기 때문입니다. 실행 전에 저장소 전체를 미러로 백업했고, 원격에만 있던 브랜치도 로컬로 가져와 빠짐없이 정리했습니다. 커밋 해시가 모두 바뀌기 때문에 force push 가 필요하고, 협업자는 다시 clone 해야 한다는 점도 알고 있습니다."

### 개념: 비밀값은 코드가 아니라 환경변수로 (`.env` + `.env.example`)
- 비밀번호나 API 키 같은 값은 소스 코드와 설정 파일에 적지 않고, 실행 환경이 넣어 주게 하는 방식이다(12-Factor App 의 Config 원칙).
- `application-local.properties` 의 DB 비밀번호를 `${DB_PASSWORD}` 로 바꿨다. `application.properties` 의 `spring.config.import=optional:file:.env[.properties]` 가 `.env` 를 읽어 주므로 별도 라이브러리가 필요 없다. 어떤 값이 필요한지는 값을 비운 `.env.example`, `application-local.properties.example` 로 알려 준다. `.env`, `*.pem`, `*.log`, `uploads/` 는 `.gitignore` 에 있다.
- 적용하지 않으면 저장소를 볼 수 있는 모든 사람이 비밀값을 보고, 비밀번호를 바꿀 때마다 코드를 커밋해야 한다. `.example` 파일이 없으면 새로 clone 한 사람이 어떤 값을 넣어야 앱이 뜨는지 알 수 없다.
- 대안: AWS Secrets Manager, Parameter Store, Vault 같은 비밀 저장소(운영 환경에는 이쪽이 맞지만 로컬 개발에는 과하다), Jasypt 로 설정 파일 암호화(복호화 키를 또 어딘가에 둬야 해서 문제가 옮겨갈 뿐이다), GitHub Actions secrets(CI/배포 단계에서는 이미 `EC2_SSH_KEY` 등에 쓰고 있다). 로컬은 `.env`, 배포는 secrets 로 나눴다.
- 면접 30초: "로컬 설정 파일에 DB 비밀번호가 그대로 적혀 커밋돼 있어서, 환경변수 참조로 바꾸고 실제 값은 git 에 올라가지 않는 .env 로 옮겼습니다. 대신 어떤 값이 필요한지는 값을 비운 .env.example 로 남겨, 새로 받은 사람도 무엇을 채워야 하는지 알 수 있게 했습니다. 운영에서는 Secrets Manager 같은 비밀 저장소가 더 적합하다고 알고 있습니다."
