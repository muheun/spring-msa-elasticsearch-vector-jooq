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
class PostDocumentCdcEventHandlerTest {

    @Autowired
    private lateinit var handler: PostDocumentCdcEventHandler

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    private val indexCoordinates = IndexCoordinates.of("posts")

    @AfterEach
    fun cleanup() {
        val query = Query.findAll()
        elasticsearchOperations.delete(query, PostDocument::class.java, indexCoordinates)
        refreshIndex()
    }

    @Test
    fun `create 이벤트는 문서를 저장한다`() {
        val event = cdcEvent(
            mapOf(
                "id" to 1,
                "title" to "테스트 제목",
                "content" to "테스트 내용",
                "author_id" to 100,
                "created_at" to "2025-01-01T00:00:00Z",
                "updated_at" to "2025-01-01T00:00:00Z"
            )
        )

        handler.handle(event)
        refreshIndex()
        val saved = elasticsearchOperations.get("1", PostDocument::class.java, indexCoordinates)
        assertThat(saved).isNotNull
        assertThat(saved?.title).isEqualTo("테스트 제목")
        assertThat(saved?.content).isEqualTo("테스트 내용")
    }

    @Test
    fun `update 이벤트는 기존 문서를 덮어쓴다`() {
        val original = PostDocument(
            id = 2,
            title = "원본",
            content = "내용",
            authorId = 10,
            createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        )
        elasticsearchOperations.save(original, indexCoordinates)
        refreshIndex()

        val event = cdcEvent(
            mapOf(
                "id" to 2,
                "title" to "수정",
                "content" to "수정된 내용",
                "author_id" to 10,
                "created_at" to "2025-01-01T00:00:00Z",
                "updated_at" to "2025-02-01T00:00:00Z"
            )
        )

        handler.handle(event)
        refreshIndex()

        val updated = elasticsearchOperations.get("2", PostDocument::class.java, indexCoordinates)
        assertThat(updated?.title).isEqualTo("수정")
        assertThat(updated?.content).isEqualTo("수정된 내용")
        assertThat(updated?.updatedAt).isEqualTo(java.time.Instant.parse("2025-02-01T00:00:00Z"))
    }

    @Test
    fun `delete 이벤트는 문서를 제거한다`() {
        val doc = PostDocument(
            id = 3,
            title = "삭제 대상",
            content = "삭제될 문서",
            authorId = 11,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        refreshIndex()

        val event = cdcEvent(mapOf("id" to 3), deleted = true)

        handler.handle(event)
        refreshIndex()

        val deleted = elasticsearchOperations.get("3", PostDocument::class.java, indexCoordinates)
        assertThat(deleted).isNull()
    }

    @Test
    fun `필수 필드가 없으면 이벤트를 무시한다`() {
        val event = cdcEvent(
            mapOf(
                "title" to "제목만",
                "content" to "내용"
            )
        )

        handler.handle(event)
        refreshIndex()

        val result = elasticsearchOperations.search(Query.findAll(), PostDocument::class.java, indexCoordinates)
        assertThat(result.totalHits).isZero()
    }

    @Test
    fun `Instant 값은 String 과 Long 을 모두 지원한다`() {
        val event = cdcEvent(
            mapOf(
                "id" to 4,
                "title" to "타입 테스트",
                "content" to "본문",
                "author_id" to 123,
                "created_at" to 1705315845000,
                "updated_at" to "2025-01-15T11:45:30Z"
            )
        )

        handler.handle(event)
        refreshIndex()
        val document = elasticsearchOperations.get("4", PostDocument::class.java, indexCoordinates)
        assertThat(document?.createdAt).isEqualTo(java.time.Instant.ofEpochMilli(1705315845000))
        assertThat(document?.updatedAt).isEqualTo(java.time.Instant.parse("2025-01-15T11:45:30Z"))
    }

    private fun refreshIndex() {
        elasticsearchOperations.indexOps(indexCoordinates).refresh()
    }

    private fun cdcEvent(payload: Map<String, Any?>, deleted: Boolean = false): CdcEvent {
        return CdcEvent(
            topic = "posts.posts.posts",
            payload = payload,
            deleted = deleted
        )
    }
}
