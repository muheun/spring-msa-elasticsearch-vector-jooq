package me.muheun.moaspace.search.consumer

import me.muheun.moaspace.search.model.PostDocument
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PostDocumentCdcEventHandler(
    private val elasticsearchOperations: ElasticsearchOperations
) : CdcEventHandler {

    override val key: String = "post-document"

    private val logger = LoggerFactory.getLogger(PostDocumentCdcEventHandler::class.java)
    private val indexCoordinates = IndexCoordinates.of("posts")

    override fun handle(event: CdcEvent) {
        if (event.deleted) {
            deleteDocument(event)
            return
        }

        val payload = event.payload
        val id = (payload["id"] as? Number)?.toLong()
        val title = payload["title"] as? String
        val content = payload["content"] as? String
        val authorId = (payload["author_id"] as? Number)?.toLong()

        if (id == null || title.isNullOrBlank() || content.isNullOrBlank() || authorId == null) {
            logger.warn("PostDocument 필수 필드 누락으로 이벤트 무시: {}", payload)
            return
        }

        val createdAt = parseInstant(payload["created_at"], "created_at") ?: return
        val updatedAt = parseInstant(payload["updated_at"], "updated_at") ?: return

        val document = PostDocument(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        elasticsearchOperations.save(document, indexCoordinates)
        logger.info("Elasticsearch 문서 저장 완료: id={}, title={}", document.id, document.title)
    }

    private fun deleteDocument(event: CdcEvent) {
        val id = (event.payload["id"] as? Number)?.toLong()
        if (id == null) {
            logger.warn("삭제 이벤트에 id가 없어 무시됩니다: {}", event.payload)
            return
        }

        elasticsearchOperations.delete(id.toString(), indexCoordinates)
        logger.info("Elasticsearch 문서 삭제 완료: id={}", id)
    }

    private fun parseInstant(value: Any?, fieldName: String): Instant? {
        val instant = when (value) {
            is String -> runCatching { Instant.parse(value) }.getOrNull()
            is Number -> Instant.ofEpochMilli(value.toLong())
            else -> null
        }

        if (instant == null) {
            logger.warn("{} 필드를 Instant로 파싱하지 못해 이벤트를 무시합니다: {}", fieldName, value)
        }

        return instant
    }
}
