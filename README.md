# Spring MSA with Elasticsearch Vector Search

**Phase 1 완료**: Docker 환경, MSA 기본 구조, CRUD API 구현

## 프로젝트 개요

Spring Boot 기반 Microservices Architecture 학습 프로젝트입니다. Gateway를 통한 단일 진입점, Database per Service 패턴, Flyway 스키마 관리, jOOQ 타입 안전 쿼리, TDD 100% 커버리지, Elasticsearch 하이브리드 검색(키워드 + 벡터) 등 실전 MSA 패턴을 적용합니다.

**현재 구현 완료**:
- ✅ Docker Compose 기반 PostgreSQL 18 환경
- ✅ Spring Cloud Gateway (단일 진입점, 라우팅)
- ✅ Post Service CRUD API (게시글 생성/조회/수정/삭제)
- ✅ Flyway DB 마이그레이션
- ✅ jOOQ 타입 안전 SQL 쿼리
- ✅ 80% 테스트 커버리지 (Repository, Service, Controller)

## 기술 스택 및 선택 이유

### 인프라
- **PostgreSQL 18**: 최신 안정 버전으로 JSON, Full-Text Search, Vector 검색 등 고급 기능 활용 가능
- **Docker Compose**: 로컬 개발 환경 일관성 보장, 팀원 간 환경 차이 최소화

### 백엔드
- **Kotlin**: Java와의 호환성 + Null Safety + 간결한 문법으로 생산성 향상
- **Spring Boot 3.x**: 최신 LTS 버전, Native Image 지원 및 성능 최적화
- **Spring Cloud Gateway**: Netflix Zuul 대비 성능 우수, Reactive 기반 비동기 처리
- **JDK 21 LTS**: Virtual Threads, Pattern Matching 등 최신 기능 활용

### 데이터베이스 도구
- **Flyway**: 스키마 버전 관리를 Git으로 추적, 팀원 간 DB 동기화 자동화
- **jOOQ**: MyBatis 대비 컴파일 타임 타입 체크로 SQL 오류 조기 발견, IDE 자동완성 지원

### 빌드 도구
- **Gradle 8.x**: Groovy 대비 Kotlin DSL의 타입 안전성, 빌드 캐싱으로 속도 개선

## MSA 아키텍처

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│   Gateway (8080)    │ ← 단일 진입점 (Spring Cloud Gateway)
└──────┬──────────────┘
       │
       ├─→ POST /api/posts/** ─→ ┌──────────────────┐
       │                          │ Post Service     │
       │                          │   (8081)         │
       │                          └────────┬─────────┘
       │                                   │
       │                                   ▼
       │                          ┌──────────────────┐
       │                          │  PostgreSQL 18   │
       │                          │    (15432)       │
       │                          │  Schema: posts   │
       │                          └──────────────────┘
```

**설계 원칙**:
- **Gateway 우선**: 모든 요청은 Gateway를 거쳐 서비스로 라우팅 (향후 서비스 추가 시 클라이언트 URL 변경 불필요)
- **Database per Service**: Post Service는 `posts` 스키마만 접근 (서비스 간 데이터 독립성 보장)
- **Schema Ownership**: Flyway가 스키마 생성/변경 관리 (Docker init 스크립트는 확장 설치만)

## 빠른 시작

### Prerequisites

| 도구 | 버전 | 확인 명령 |
|------|------|----------|
| Docker Desktop | 최신 | `docker --version` |
| Docker Compose | 최신 | `docker-compose --version` |
| JDK | 21 LTS | `java -version` |
| Gradle | 8.x (Wrapper 사용 가능) | `gradle --version` |

**시스템 요구사항**:
- 메모리: Docker Desktop에 최소 4GB 할당
- 디스크: 최소 2GB 여유 공간
- 포트: 8080, 8081, 15432 사용 가능 확인 (`lsof -i :8080`)

### 1단계: PostgreSQL 실행 (30초)

```bash
cd moa-space
docker-compose up -d postgres

# 컨테이너 상태 확인
docker-compose ps
# Expected: moa-space-postgres (healthy)
```

### 2단계: Gateway 실행 (10초)

```bash
cd gateway
./gradlew bootRun

# 다른 터미널에서 health check
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### 3단계: Post Service 실행 (15초)

```bash
cd post-service
./gradlew bootRun

# Flyway 마이그레이션 자동 실행 확인 (로그)
# "Migrating schema `posts` to version 1"
# "Successfully applied 3 migrations"

# Health check
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP","components":{"db":{"status":"UP"}}}
```

### 4단계: jOOQ 코드 생성

```bash
cd post-service
./gradlew generateJooq

# 생성된 파일 확인
ls -la build/generated-src/jooq/main/com/moaspace/post/jooq/generated/tables/
# Expected: Posts.java, Comments.java
```

## 검증

### CRUD API 테스트

```bash
# Gateway를 통한 게시글 생성
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"테스트 게시글","content":"내용입니다","authorId":1}'
# Expected: HTTP 201, {"id":1,"title":"테스트 게시글",...}

# 게시글 조회
curl http://localhost:8080/api/posts/1
# Expected: HTTP 200, 게시글 상세 정보

# 게시글 목록
curl "http://localhost:8080/api/posts?page=0&size=10"
# Expected: HTTP 200, 게시글 배열

# 게시글 수정
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"수정된 제목","content":"수정된 내용"}'
# Expected: HTTP 200, 수정된 게시글

# 게시글 삭제
curl -X DELETE http://localhost:8080/api/posts/1
# Expected: HTTP 204

# 테스트 실행
cd post-service
./gradlew test jacocoTestReport
# Expected: BUILD SUCCESSFUL, 80% coverage
```

### PostgreSQL 확인

```bash
# PostgreSQL 접속
docker exec -it moa-space-postgres psql -U moauser -d moaspace

# psql에서
moaspace=# \dn                      -- 스키마 목록
moaspace=# SET search_path TO posts;
moaspace=# \dt                      -- 테이블 목록 (posts, comments 확인)
moaspace=# SELECT * FROM posts;     -- 게시글 데이터 확인
moaspace=# SELECT * FROM flyway_schema_history;  -- 마이그레이션 이력
moaspace=# \q
```

## 디렉토리 구조

```
moa-space/
├── docker-compose.yml       # PostgreSQL 18 설정
├── .env                     # DB credentials (gitignored)
├── init/                    # PostgreSQL 초기화 스크립트
│   └── 01-install-extensions.sh
├── data/                    # PostgreSQL 데이터 (gitignored)
│
├── gateway/                 # Spring Cloud Gateway (port 8080)
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── kotlin/com/moaspace/gateway/
│   │   │   ├── GatewayApplication.kt
│   │   │   └── config/GatewayConfig.kt
│   │   └── resources/application.yml
│   └── src/test/
│
└── post-service/            # Post Service (port 8081)
    ├── build.gradle.kts
    ├── src/main/
    │   ├── kotlin/com/moaspace/post/
    │   │   ├── PostServiceApplication.kt
    │   │   ├── config/DatabaseConfig.kt
    │   │   ├── controller/
    │   │   │   ├── PostController.kt
    │   │   │   └── advice/GlobalExceptionHandler.kt
    │   │   └── domain/
    │   │       ├── dto/             # Request/Response DTOs
    │   │       ├── entity/Post.kt
    │   │       ├── repository/      # jOOQ Repository
    │   │       ├── service/PostService.kt
    │   │       └── exception/PostNotFoundException.kt
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/        # Flyway 마이그레이션
    │           ├── V1__create_posts_schema.sql
    │           ├── V2__create_posts_table.sql
    │           └── V3__create_comments_table.sql
    └── src/test/                    # 80% coverage tests
```

## Troubleshooting

### PostgreSQL 컨테이너가 healthy 상태가 안 됨

```bash
# 로그 확인
docker-compose logs postgres

# 흔한 원인 1: data/ 디렉토리 권한 문제
sudo chown -R 999:999 data/

# 흔한 원인 2: 포트 충돌
lsof -i :15432
# .env에서 PORT=15433으로 변경 후 재시작
```

### Flyway 마이그레이션 실패

```bash
# 원인 1: PostgreSQL 아직 준비 안 됨
docker-compose ps  # postgres healthy 확인 후 Post Service 재시작

# 원인 2: flyway_schema_history 테이블 손상
docker exec -it moa-space-postgres psql -U moauser -d moaspace
moaspace=# DROP TABLE IF EXISTS posts.flyway_schema_history CASCADE;
# Post Service 재시작
```

### jOOQ 코드 생성 실패

```bash
# 원인 1: Flyway 마이그레이션 미실행
# → Post Service 먼저 실행하여 Flyway 완료 확인

# 원인 2: posts 스키마 없음
docker exec -it moa-space-postgres psql -U moauser -d moaspace -c "\dn"
# posts 스키마 존재 확인
```

## 현재 제약사항

- **로컬 개발 환경**: 프로덕션 배포 설정 제외 (Kubernetes, CI/CD 등)
- **단일 서비스**: Post Service만 구현 (User Service, Comment Service는 Phase 4)
- **기본 CRUD**: 페이징, 정렬, 필터링 등 고급 기능은 Phase 2에서 확장

## 다음 단계

- **Phase 2**: Elasticsearch + CDC (Change Data Capture) - 키워드 검색
- **Phase 3**: Vector 검색 + 하이브리드 검색 (키워드 + 벡터)
- **Phase 4**: 인증/인가 (JWT) + Circuit Breaker + Cache

## 유용한 명령어

```bash
# Docker
docker-compose up -d          # 전체 서비스 시작
docker-compose down           # 전체 서비스 중지
docker-compose down -v        # 데이터까지 완전 삭제
docker-compose logs -f postgres  # PostgreSQL 로그 실시간 확인

# Gradle
./gradlew bootRun             # 애플리케이션 실행
./gradlew build               # 빌드 (테스트 포함)
./gradlew test                # 테스트 실행
./gradlew jacocoTestReport    # 커버리지 리포트 생성
./gradlew generateJooq        # jOOQ 코드 생성 (Post Service)

# PostgreSQL
docker exec -it moa-space-postgres pg_isready -U moauser -d moaspace  # 연결 테스트
docker exec -it moa-space-postgres psql -U moauser -d moaspace        # psql 접속
```

## 라이선스

MIT License
