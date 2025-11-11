package me.muheun.moaspace.search.repository

import me.muheun.moaspace.search.model.PostDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
class CustomDocumentRepositoryImplTest {

    @Autowired
    private lateinit var postDocumentRepository: PostDocumentRepository

    private val testIndexName = "posts_test_${System.currentTimeMillis()}"

    @BeforeEach
    fun setup() {
        postDocumentRepository.createIndex(
            IndexCoordinates.of(testIndexName),
            PostDocument::class.java
        )
    }

    @AfterEach
    fun cleanup() {
        postDocumentRepository.deleteIndex(IndexCoordinates.of(testIndexName))
    }

    @Test
    fun `createIndex - 인덱스 생성 성공`() {
        val newIndexName = "posts_new_${System.currentTimeMillis()}"
        val indexCoordinates = IndexCoordinates.of(newIndexName)

        postDocumentRepository.createIndex(indexCoordinates, PostDocument::class.java)

        val operations = postDocumentRepository.getOperations()
        val exists = operations.indexOps(indexCoordinates).exists()
        assertThat(exists).isTrue

        postDocumentRepository.deleteIndex(indexCoordinates)
    }

    @Test
    fun `createIndex - 이미 존재하는 인덱스는 생성하지 않음`() {
        val indexCoordinates = IndexCoordinates.of(testIndexName)

        postDocumentRepository.createIndex(indexCoordinates, PostDocument::class.java)

        val operations = postDocumentRepository.getOperations()
        val exists = operations.indexOps(indexCoordinates).exists()
        assertThat(exists).isTrue
    }

    @Test
    fun `deleteIndex - 인덱스 삭제 성공`() {
        val indexCoordinates = IndexCoordinates.of(testIndexName)

        val deleted = postDocumentRepository.deleteIndex(indexCoordinates)

        assertThat(deleted).isTrue
        val operations = postDocumentRepository.getOperations()
        val exists = operations.indexOps(indexCoordinates).exists()
        assertThat(exists).isFalse
    }

    @Test
    fun `save - 특정 인덱스에 문서 저장 성공`() {
        val indexCoordinates = IndexCoordinates.of(testIndexName)
        val document = PostDocument(
            id = 1L,
            title = "테스트 제목",
            content = "테스트 내용",
            authorId = 100L,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val saved = postDocumentRepository.save(document, indexCoordinates)

        assertThat(saved).isNotNull
        assertThat(saved.id).isEqualTo(1L)
        assertThat(saved.title).isEqualTo("테스트 제목")
    }

    @Test
    fun `saveAll - 여러 문서 저장 성공`() {
        val indexCoordinates = IndexCoordinates.of(testIndexName)
        val documents = listOf(
            PostDocument(
                id = 1L,
                title = "제목1",
                content = "내용1",
                authorId = 100L,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            PostDocument(
                id = 2L,
                title = "제목2",
                content = "내용2",
                authorId = 101L,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val saved = postDocumentRepository.saveAll(documents, indexCoordinates)

        assertThat(saved).hasSize(2)
        assertThat(saved.map { it.id }).containsExactlyInAnyOrder(1L, 2L)
    }

    @Test
    fun `setAlias - 인덱스에 별칭 설정 성공`() {
        val indexCoordinates = IndexCoordinates.of(testIndexName)
        val aliasName = "posts_alias_${System.currentTimeMillis()}"
        val aliasCoordinates = IndexCoordinates.of(aliasName)

        val result = postDocumentRepository.setAlias(indexCoordinates, aliasCoordinates)

        assertThat(result).isTrue
    }

    @Test
    fun `findIndexNamesByAlias - 별칭으로 인덱스 이름 조회 성공`() {
        val indexCoordinates = IndexCoordinates.of(testIndexName)
        val aliasName = "posts_alias_${System.currentTimeMillis()}"
        val aliasCoordinates = IndexCoordinates.of(aliasName)

        postDocumentRepository.setAlias(indexCoordinates, aliasCoordinates)

        val result = postDocumentRepository.findIndexNamesByAlias(aliasCoordinates)

        assertThat(result).isNotEmpty
        assertThat(result).contains(testIndexName)
    }

    @Test
    fun `findIndexNamesByAlias - 존재하지 않는 인덱스는 빈 Set 반환`() {
        val nonExistentIndex = IndexCoordinates.of("nonexistent_index_${System.currentTimeMillis()}")

        val indexNames = postDocumentRepository.findIndexNamesByAlias(nonExistentIndex)

        assertThat(indexNames).isEmpty()
    }

    @Test
    fun `getOperations - ElasticsearchOperations 객체 반환`() {
        val operations = postDocumentRepository.getOperations()

        assertThat(operations).isNotNull
    }
}
