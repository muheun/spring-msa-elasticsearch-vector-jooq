package me.muheun.moaspace.search.consumer

import me.muheun.moaspace.search.model.PostDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PostConsumerTest {

    @Autowired
    private lateinit var postConsumer: PostConsumer

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    private val indexCoordinates = IndexCoordinates.of("posts")

    @AfterEach
    fun cleanup() {
        val query = Query.findAll()
        elasticsearchOperations.delete(query, PostDocument::class.java, indexCoordinates)
    }

    @Test
    fun `CREATE 이벤트 처리 시 Elasticsearch 문서 저장`() {
        // Given
        val messageJson = """
            {
              "id": 1,
              "title": "테스트 제목",
              "content": "테스트 내용",
              "author_id": 100,
              "created_at": "2025-01-01T00:00:00Z",
              "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        // When
        postConsumer.consume(messageJson)

        // Then
        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("1", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNotNull
        assertThat(saved?.title).isEqualTo("테스트 제목")
        assertThat(saved?.content).isEqualTo("테스트 내용")
        assertThat(saved?.authorId).isEqualTo(100L)
    }

    @Test
    fun `UPDATE 이벤트 처리 시 Elasticsearch 문서 업데이트`() {
        // Given: 기존 문서 저장
        val originalDoc = PostDocument(
            id = 2L,
            title = "원본 제목",
            content = "원본 내용",
            authorId = 200L,
            createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        )
        elasticsearchOperations.save(originalDoc, indexCoordinates)
        Thread.sleep(1000)

        // When: UPDATE 이벤트 발생
        val updateMessageJson = """
            {
              "id": 2,
              "title": "수정된 제목",
              "content": "수정된 내용",
              "author_id": 200,
              "created_at": "2025-01-01T00:00:00Z",
              "updated_at": "2025-01-02T00:00:00Z"
            }
        """.trimIndent()

        postConsumer.consume(updateMessageJson)

        // Then
        Thread.sleep(1000)
        val updated = elasticsearchOperations.get("2", PostDocument::class.java, indexCoordinates)
        assertThat(updated).isNotNull
        assertThat(updated?.title).isEqualTo("수정된 제목")
        assertThat(updated?.content).isEqualTo("수정된 내용")
    }

    @Test
    fun `DELETE 이벤트 처리 시 Elasticsearch 문서 삭제`() {
        // Given: 기존 문서 저장
        val doc = PostDocument(
            id = 3L,
            title = "삭제 대상",
            content = "삭제될 문서",
            authorId = 300L,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        Thread.sleep(1000)

        // When: DELETE 이벤트 발생
        val deleteMessageJson = """
            {
              "id": 3,
              "__deleted": "true"
            }
        """.trimIndent()

        postConsumer.consume(deleteMessageJson)

        // Then
        Thread.sleep(1000)
        val deleted = elasticsearchOperations.get("3", PostDocument::class.java, indexCoordinates)
        assertThat(deleted).isNull()
    }

    @Test
    fun `필수 필드 누락 시 문서 저장하지 않음 - id 누락`() {
        // Given: id 필드 누락
        val messageJson = """
            {
              "title": "제목만 있음",
              "content": "내용",
              "author_id": 400
            }
        """.trimIndent()

        // When
        postConsumer.consume(messageJson)

        // Then
        Thread.sleep(1000)
        val query = Query.findAll()
        val docs = elasticsearchOperations.search(query, PostDocument::class.java, indexCoordinates)
        assertThat(docs.totalHits).isEqualTo(0)
    }

    @Test
    fun `필수 필드 누락 시 문서 저장하지 않음 - title 누락`() {
        // Given: title 필드 누락
        val messageJson = """
            {
              "id": 5,
              "content": "내용만 있음",
              "author_id": 500
            }
        """.trimIndent()

        // When
        postConsumer.consume(messageJson)

        // Then
        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("5", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNull()
    }

    @Test
    fun `Instant 파싱 - String 형식 (ISO 8601)`() {
        // Given
        val messageJson = """
            {
              "id": 6,
              "title": "제목",
              "content": "내용",
              "author_id": 600,
              "created_at": "2025-01-15T10:30:45Z",
              "updated_at": "2025-01-15T11:45:30Z"
            }
        """.trimIndent()

        // When
        postConsumer.consume(messageJson)

        // Then
        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("6", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNotNull
        assertThat(saved?.createdAt).isEqualTo(java.time.Instant.parse("2025-01-15T10:30:45Z"))
        assertThat(saved?.updatedAt).isEqualTo(java.time.Instant.parse("2025-01-15T11:45:30Z"))
    }

    @Test
    fun `Instant 파싱 - Long 형식 (epoch millis)`() {
        // Given
        val messageJson = """
            {
              "id": 7,
              "title": "제목",
              "content": "내용",
              "author_id": 700,
              "created_at": 1705315845000,
              "updated_at": 1705320330000
            }
        """.trimIndent()

        // When
        postConsumer.consume(messageJson)

        // Then
        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("7", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNotNull
        assertThat(saved?.createdAt).isEqualTo(java.time.Instant.ofEpochMilli(1705315845000))
        assertThat(saved?.updatedAt).isEqualTo(java.time.Instant.ofEpochMilli(1705320330000))
    }

    @Test
    fun `Instant 파싱 - null 값도 문서 저장 성공`() {
        // Given
        val messageJson = """
            {
              "id": 8,
              "title": "제목",
              "content": "내용",
              "author_id": 800,
              "created_at": null,
              "updated_at": null
            }
        """.trimIndent()

        // When
        postConsumer.consume(messageJson)

        // Then
        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("8", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNotNull
        assertThat(saved?.title).isEqualTo("제목")
        assertThat(saved?.content).isEqualTo("내용")
        assertThat(saved?.authorId).isEqualTo(800L)
        assertThat(saved?.createdAt).isNotNull()
        assertThat(saved?.updatedAt).isNotNull()
    }

    @Test
    fun `예외 발생 시 에러 로그 출력 - 잘못된 날짜 형식`() {
        val messageJson = """
            {
              "id": 9,
              "title": "예외 테스트",
              "content": "잘못된 날짜 형식",
              "author_id": 900,
              "created_at": "invalid-date-format",
              "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        postConsumer.consume(messageJson)

        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("9", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNull()
    }

    @Test
    fun `필수 필드 누락 시 문서 저장하지 않음 - content 누락`() {
        val messageJson = """
            {
              "id": 10,
              "title": "제목만 있음",
              "author_id": 1000,
              "created_at": "2025-01-01T00:00:00Z",
              "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        postConsumer.consume(messageJson)

        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("10", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNull()
    }

    @Test
    fun `필수 필드 누락 시 문서 저장하지 않음 - author_id 누락`() {
        val messageJson = """
            {
              "id": 11,
              "title": "제목",
              "content": "내용",
              "created_at": "2025-01-01T00:00:00Z",
              "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        postConsumer.consume(messageJson)

        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("11", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNull()
    }

    @Test
    fun `DELETE 이벤트에서 id 누락 시 삭제하지 않음`() {
        val doc = PostDocument(
            id = 12L,
            title = "삭제 대상",
            content = "삭제될 문서",
            authorId = 1200L,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        Thread.sleep(1000)

        val deleteMessageJson = """
            {
              "__deleted": "true"
            }
        """.trimIndent()

        postConsumer.consume(deleteMessageJson)

        Thread.sleep(1000)
        val stillExists = elasticsearchOperations.get("12", PostDocument::class.java, indexCoordinates)
        assertThat(stillExists).isNotNull()
    }

    @Test
    fun `Instant 파싱 - Integer 형식 (Number 타입이지만 Long이 아닌 경우)`() {
        val messageJson = """
            {
              "id": 13,
              "title": "제목",
              "content": "내용",
              "author_id": 1300,
              "created_at": 1705315845,
              "updated_at": 1705320330
            }
        """.trimIndent()

        postConsumer.consume(messageJson)

        Thread.sleep(1000)
        val saved = elasticsearchOperations.get("13", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNotNull()
        assertThat(saved?.createdAt).isNotNull()
        assertThat(saved?.updatedAt).isNotNull()
    }
}
