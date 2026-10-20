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
