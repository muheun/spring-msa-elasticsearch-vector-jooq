# 수동 E2E 테스트 가이드

## 개요
CDC 파이프라인 전체 흐름을 검증하는 E2E 테스트입니다.
**Kafka → CDC Event Listener → Elasticsearch → SearchService** 전체 파이프라인을 테스트합니다.

## 사전 조건
Docker-compose로 다음 인프라가 실행 중이어야 합니다:
- Elasticsearch (localhost:9200)
- Kafka (localhost:9092)
- Debezium Connector
- PostgreSQL (post-service DB)

## 테스트 절차

### 1. search-service 실행
```bash
cd search-service
./gradlew bootRun
```

### 2. post-service에서 게시글 생성 (CDC 이벤트 발생)
```bash
# post-service API로 게시글 생성
curl -X POST http://localhost:8081/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "E2E 테스트 제목",
    "content": "CDC 파이프라인 전체 테스트",
    "authorId": 9999
  }'
```

### 3. Elasticsearch 인덱싱 확인 (1-2초 대기 후)
```bash
# Elasticsearch에 문서가 저장되었는지 확인
curl http://localhost:9200/posts/_search?q=E2E
```

**예상 결과**:
```json
{
  "hits": {
    "total": { "value": 1 },
    "hits": [{
      "_source": {
        "id": ...,
        "title": "E2E 테스트 제목",
        "content": "CDC 파이프라인 전체 테스트",
        "authorId": 9999
      }
    }]
  }
}
```

### 4. SearchService API로 검색 (향후 Controller 추가 시)
```bash
# SearchService API로 검색 (Controller 구현 후)
curl http://localhost:8080/api/search?keyword=E2E
```

## 검증 포인트

✅ **Kafka 메시지 발행**: post-service에서 게시글 생성 시 Kafka 토픽에 CDC 이벤트 발행
✅ **CDC Event Listener 수신**: search-service의 CdcEventListener가 Kafka 메시지 수신 및 로그 출력
✅ **Elasticsearch 저장**: Elasticsearch에 PostDocument 저장 확인
✅ **SearchService 검색**: SearchService로 저장된 문서 검색 성공

## 문제 해결

### Kafka 연결 실패
- `docker-compose ps`로 Kafka 컨테이너 상태 확인
- `docker-compose logs kafka`로 Kafka 로그 확인

### Elasticsearch 인덱싱 실패
- `docker-compose logs elasticsearch`로 Elasticsearch 로그 확인
- search-service 로그에서 CdcEventListener/PostDocumentCdcEventHandler 에러 확인

### CDC 이벤트 미발생
- Debezium Connector 상태 확인: `curl http://localhost:8083/connectors/moa-space-posts-connector/status`
- PostgreSQL WAL 설정 확인
