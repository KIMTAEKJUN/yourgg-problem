# YOURGG 소환사 매치 검색 서비스

## 🛠️ 사용 기술 스택

### 백엔드 프레임워크

- **Spring Boot 3.5.6** - Java 기반 웹 애플리케이션 프레임워크
- **Spring WebFlux** - 비동기 웹 클라이언트 (Riot API 호출용)
- **Spring Data JPA** - 데이터 접근 계층 (사용하지 않음)

### 프론트엔드

- **Thymeleaf** - 서버 사이드 템플릿 엔진
- **HTML5/CSS3** - 반응형 웹 페이지

### 개발 도구 및 라이브러리

- **Java 21** - 프로젝트 언어
- **Gradle** - 빌드 자동화 도구
- **Lombok** - 보일러플레이트 코드 자동 생성
- **Jackson** - JSON 파싱 라이브러리

### 외부 API

- **Riot Games API** - League of Legends 게임 데이터 조회
  - Account API (Riot ID → PUUID 변환)
  - Match API (매치 정보 조회)

## 🏗️ 아키텍처 및 개발 방법

### 프로젝트 구조

```
src/main/java/com/backend/yourgg/
├── config/           # 설정 클래스
│   ├── RiotApiConfig.java      # Riot API 설정
│   └── WebClientConfig.java    # HTTP 클라이언트 설정
├── controller/       # 웹 요청 처리
│   └── MatchController.java    # 매치 검색 및 상세 페이지 컨트롤러
├── dto/             # 데이터 전송 객체
│   ├── AccountDto.java         # 계정 정보 DTO
│   ├── MatchDetailDto.java     # 매치 상세 정보 DTO
│   ├── MatchInfoDto.java       # 매치 기본 정보 DTO
│   ├── ParticipantDto.java     # 참가자 정보 DTO
│   └── TeamDto.java           # 팀 정보 DTO
├── service/         # 비즈니스 로직
│   ├── AccountService.java     # 계정 서비스 인터페이스
│   ├── MatchService.java       # 매치 서비스 인터페이스
│   ├── impl/
│   │   ├── AccountServiceImpl.java  # 계정 서비스 구현체
│   │   └── MatchServiceImpl.java    # 매치 서비스 구현체
└── YourggProblemApplication.java    # 메인 애플리케이션 클래스

src/main/resources/
├── templates/       # Thymeleaf 템플릿
│   ├── index.html          # 메인 페이지 (Riot ID 입력 폼)
│   └── match-detail.html   # 매치 상세 페이지
└── application.properties  # 애플리케이션 설정
```

### 개발 접근 방법

#### 1. API 연동 중심 개발

- **Riot Games API**를 활용한 실시간 데이터 조회
- **비동기 처리**로 API 응답 대기 시간 최소화
- **에러 처리**로 안정적인 서비스 제공

#### 2. MVC 패턴 적용

- **Controller**: 사용자 요청 처리 및 응답 조율
- **Service**: 비즈니스 로직 및 API 호출
- **DTO**: 데이터 구조 정의 및 변환

#### 3. 템플릿 기반 프론트엔드

- **Thymeleaf**를 활용한 서버 사이드 렌더링
- **반응형 디자인**으로 다양한 디바이스 지원
- **사용자 경험 최적화**를 위한 직관적 UI

## 🔧 주요 기능 구현

### 1. Riot ID 기반 소환사 검색

```java
// Riot ID 입력 (예: Faker#KR1)
@PostMapping("/search")
public String searchSummoner(@RequestParam String riotId) {
    // 게임네임#태그라인 파싱
    // Account API로 PUUID 조회
    // 매치 목록 조회 및 필터링
}
```

### 2. 매치 정보 필터링

```java
// 소환사의 협곡 매치만 필터링
private boolean isSummonersRiftMatch(MatchInfoDto matchInfo) {
    List<Integer> summonersRiftQueues = List.of(
        420, // 솔로 랭크
        430, // 일반
        440  // 자유 랭크
    );
    return summonersRiftQueues.contains(matchInfo.getQueueId());
}
```

### 3. 큐 타입 한글 변환

```java
public String getQueueTypeName(Integer queueId) {
    return switch (queueId) {
        case 420 -> "솔로 랭크";
        case 430 -> "일반";
        case 440 -> "자유 랭크";
        default -> "기타";
    };
}
```

## 🚀 실행 방법

### 사전 요구사항

- Java 21 이상
- Gradle 설치

### 실행 단계

```bash
# 프로젝트 클론 및 이동
git clone <repository-url>
cd yourgg-problem

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 브라우저에서 접속
# http://localhost:8080
```

### 사용 방법

1. 메인 페이지 접속 (http://localhost:8080)
2. Riot ID 입력 (예: `Hide on bush#KR1`)
3. 최근 소환사의 협곡 매치 조회
4. 매치 상세 정보 확인

## 📋 API 연동 상세

### 1단계: Riot ID → PUUID 변환

```java
// Account API 사용
GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}
Authorization: Bearer {api-key}
```

### 2단계: 매치 목록 조회

```java
// Match API 사용
GET /lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count=10
Authorization: Bearer {api-key}
```

### 3단계: 매치 상세 정보 조회

```java
// Match API 사용
GET /lol/match/v5/matches/{matchId}
Authorization: Bearer {api-key}
```

## 🔒 보안 및 설정

### 환경 변수 설정

```properties
# application.properties
riot.api.key=RGAPI-{your-api-key}
riot.api.base-url=https://asia.api.riotgames.com
logging.level.com.backend.yourgg=DEBUG
```

### API 키 관리

- Riot Developer Portal에서 발급받은 API 키 사용
- 민감한 정보는 환경 변수로 관리
- 요청 시 User-Agent 및 API 키 헤더 포함
