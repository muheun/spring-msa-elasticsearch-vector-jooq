package me.muheun.moaspace.search.service

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
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
class PostSearchServiceTest {

    @Autowired
    private lateinit var postSearchService: PostSearchService

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    private val indexCoordinates = IndexCoordinates.of("posts")

    @AfterEach
    fun cleanup() {
        try {
            val query = Query.findAll()
            elasticsearchOperations.delete(query, PostDocument::class.java, indexCoordinates)
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @Test
    fun `키워드 검색 - 제목 매칭`() {
        val doc = PostDocument(
            id = 1L,
            title = "Elasticsearch 검색 엔진",
            content = "일반 내용입니다",
            authorId = 100L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        Thread.sleep(1000)

        val result = postSearchService.search("Elasticsearch")

        assertThat(result.posts).hasSize(1)
        assertThat(result.posts[0].title).contains("Elasticsearch")
        assertThat(result.totalHits).isEqualTo(1)
    }

    @Test
    fun `키워드 검색 - 내용 매칭`() {
        val doc = PostDocument(
            id = 2L,
            title = "일반 제목",
            content = "Kotlin으로 개발하는 마이크로서비스",
            authorId = 200L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        Thread.sleep(1000)

        val result = postSearchService.search("Kotlin")

        assertThat(result.posts).hasSize(1)
        assertThat(result.posts[0].content).contains("Kotlin")
        assertThat(result.totalHits).isEqualTo(1)
    }

    @Test
    fun `키워드 검색 - 제목+내용 모두 매칭`() {
        val doc1 = PostDocument(
            id = 3L,
            title = "Spring Boot 튜토리얼",
            content = "기초 내용",
            authorId = 300L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val doc2 = PostDocument(
            id = 4L,
            title = "기초 가이드",
            content = "Spring Boot 심화 학습",
            authorId = 300L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        elasticsearchOperations.save(doc1, indexCoordinates)
        elasticsearchOperations.save(doc2, indexCoordinates)
        Thread.sleep(1000)

        val result = postSearchService.search("Spring")

        assertThat(result.posts).hasSize(2)
        assertThat(result.totalHits).isEqualTo(2)
    }

    @Test
    fun `키워드 검색 - 매칭 결과 없음`() {
        val doc = PostDocument(
            id = 5L,
            title = "React 프론트엔드",
            content = "UI 개발",
            authorId = 500L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        Thread.sleep(1000)

        val result = postSearchService.search("Vue")

        assertThat(result.posts).isEmpty()
        assertThat(result.totalHits).isEqualTo(0)
    }

    @Test
    fun `ID로 단일 문서 조회 - 성공`() {
        val doc = PostDocument(
            id = 6L,
            title = "테스트 제목",
            content = "테스트 내용",
            authorId = 600L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        elasticsearchOperations.save(doc, indexCoordinates)
        Thread.sleep(1000)

        val result = postSearchService.getById(6L)

        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo(6L)
        assertThat(result?.title).isEqualTo("테스트 제목")
    }

    @Test
    fun `ID로 단일 문서 조회 - 존재하지 않음`() {
        val result = postSearchService.getById(999L)

        assertThat(result).isNull()
    }

    @Test
    fun `키워드로 문서 개수 조회`() {
        val docs = listOf(
            PostDocument(7L, "Docker 컨테이너", "내용1", 700L, Instant.now(), Instant.now()),
            PostDocument(8L, "Docker Compose", "내용2", 700L, Instant.now(), Instant.now()),
            PostDocument(9L, "Kubernetes", "내용3", 700L, Instant.now(), Instant.now())
        )
        docs.forEach { elasticsearchOperations.save(it, indexCoordinates) }
        Thread.sleep(1000)

        val count = postSearchService.countByKeyword("Docker")

        assertThat(count).isEqualTo(2)
    }

    @Test
    fun `페이징 처리 - page와 size`() {
        val docs = (1..15).map {
            PostDocument(
                id = it.toLong() + 100,
                title = "페이징 테스트 $it",
                content = "내용 $it",
                authorId = 800L,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        docs.forEach { elasticsearchOperations.save(it, indexCoordinates) }
        Thread.sleep(1000)

        val page0 = postSearchService.search("페이징", page = 0, size = 10)
        val page1 = postSearchService.search("페이징", page = 1, size = 10)

        assertThat(page0.posts).hasSize(10)
        assertThat(page0.page).isEqualTo(0)
        assertThat(page0.size).isEqualTo(10)
        assertThat(page0.totalHits).isEqualTo(15)

        assertThat(page1.posts).hasSize(5)
        assertThat(page1.page).isEqualTo(1)
        assertThat(page1.totalHits).isEqualTo(15)
    }

    @Test
    fun `정렬 처리 - 최신순`() {
        val now = Instant.now()
        val doc1 = PostDocument(10L, "정렬 테스트 1", "내용", 900L, now.minusSeconds(3600), now.minusSeconds(3600))
        val doc2 = PostDocument(11L, "정렬 테스트 2", "내용", 900L, now.minusSeconds(1800), now.minusSeconds(1800))
        val doc3 = PostDocument(12L, "정렬 테스트 3", "내용", 900L, now, now)

        elasticsearchOperations.save(doc1, indexCoordinates)
        elasticsearchOperations.save(doc2, indexCoordinates)
        elasticsearchOperations.save(doc3, indexCoordinates)
        Thread.sleep(1000)

        val result = postSearchService.search("정렬", sort = "latest")

        assertThat(result.posts).hasSize(3)
        assertThat(result.posts[0].id).isEqualTo(12L)
        assertThat(result.posts[1].id).isEqualTo(11L)
        assertThat(result.posts[2].id).isEqualTo(10L)
    }

    @Test
    fun `빈 키워드 검색 시 전체 결과 반환`() {
        val docs = (1..5).map {
            PostDocument(
                id = it.toLong() + 200,
                title = "문서 $it",
                content = "내용 $it",
                authorId = 1000L,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        docs.forEach { elasticsearchOperations.save(it, indexCoordinates) }
        Thread.sleep(1000)

        val result = postSearchService.search("")

        assertThat(result.posts).hasSizeGreaterThanOrEqualTo(5)
        assertThat(result.totalHits).isGreaterThanOrEqualTo(5)
    }
}
