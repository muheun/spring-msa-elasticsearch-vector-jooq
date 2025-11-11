package me.muheun.moaspace.search.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class PostDocumentTest {

    private val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()
    }

    @Test
    fun `PostDocument 생성 확인`() {
        val doc = PostDocument(
            id = 1L,
            title = "테스트 제목",
            content = "테스트 내용",
            authorId = 100L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        assertThat(doc).isNotNull
        assertThat(doc.id).isEqualTo(1L)
        assertThat(doc.title).isEqualTo("테스트 제목")
        assertThat(doc.content).isEqualTo("테스트 내용")
        assertThat(doc.authorId).isEqualTo(100L)
    }

    @Test
    fun `필드 검증 - 모든 필드 존재`() {
        val now = Instant.now()
        val doc = PostDocument(
            id = 2L,
            title = "제목",
            content = "내용",
            authorId = 200L,
            createdAt = now,
            updatedAt = now
        )

        assertThat(doc.id).isNotNull
        assertThat(doc.title).isNotEmpty
        assertThat(doc.content).isNotEmpty
        assertThat(doc.authorId).isGreaterThan(0)
        assertThat(doc.createdAt).isNotNull
        assertThat(doc.updatedAt).isNotNull
    }

    @Test
    fun `Serialization - PostDocument to JSON`() {
        val doc = PostDocument(
            id = 3L,
            title = "JSON 테스트",
            content = "직렬화 테스트",
            authorId = 300L,
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2025-01-01T00:00:00Z")
        )

        val json = objectMapper.writeValueAsString(doc)

        assertThat(json).contains("\"id\":3")
        assertThat(json).contains("JSON 테스트")
        assertThat(json).contains("직렬화 테스트")
        assertThat(json).contains("\"authorId\":300")
    }

    @Test
    fun `Deserialization - JSON to PostDocument`() {
        val json = """
            {
              "id": 4,
              "title": "역직렬화 테스트",
              "content": "JSON to Object",
              "authorId": 400,
              "createdAt": "2025-01-01T00:00:00Z",
              "updatedAt": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        val doc = objectMapper.readValue(json, PostDocument::class.java)

        assertThat(doc).isNotNull
        assertThat(doc.id).isEqualTo(4L)
        assertThat(doc.title).isEqualTo("역직렬화 테스트")
        assertThat(doc.content).isEqualTo("JSON to Object")
        assertThat(doc.authorId).isEqualTo(400L)
        assertThat(doc.createdAt).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"))
        assertThat(doc.updatedAt).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"))
    }
}
