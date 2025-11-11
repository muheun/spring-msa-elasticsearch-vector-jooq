package me.muheun.moaspace.search.controller

import me.muheun.moaspace.search.model.PostDocument
import me.muheun.moaspace.search.repository.PostDocumentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/indices")
class IndexManagementController(
    private val postDocumentRepository: PostDocumentRepository
) {

    private val logger = LoggerFactory.getLogger(IndexManagementController::class.java)

    @PostMapping("/posts")
    fun createPostsIndex(): ResponseEntity<Map<String, String>> {
        return try {
            postDocumentRepository.createIndex(
                IndexCoordinates.of("posts"),
                PostDocument::class.java
            )
            logger.info("Elasticsearch 인덱스 'posts' 생성 완료")
            ResponseEntity.ok(mapOf("message" to "Index 'posts' created successfully"))
        } catch (e: Exception) {
            logger.error("Elasticsearch 인덱스 'posts' 생성 실패: ${e.message}", e)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @DeleteMapping("/posts")
    fun deletePostsIndex(): ResponseEntity<Map<String, String>> {
        return try {
            val deleted = postDocumentRepository.deleteIndex(IndexCoordinates.of("posts"))
            if (deleted) {
                logger.info("Elasticsearch 인덱스 'posts' 삭제 완료")
                ResponseEntity.ok(mapOf("message" to "Index 'posts' deleted successfully"))
            } else {
                logger.warn("Elasticsearch 인덱스 'posts' 존재하지 않음")
                ResponseEntity.badRequest().body(mapOf("error" to "Index 'posts' does not exist"))
            }
        } catch (e: Exception) {
            logger.error("Elasticsearch 인덱스 'posts' 삭제 실패: ${e.message}", e)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }
}
