package me.muheun.moaspace.search.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import me.muheun.moaspace.search.model.PostDocument
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PostConsumer(
    private val elasticsearchOperations: ElasticsearchOperations,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(PostConsumer::class.java)

    @KafkaListener(topics = ["posts.posts.posts"], groupId = "search-service-group")
    fun consume(messageJson: String) {
        val message = objectMapper.readValue(messageJson, Map::class.java) as Map<String, Any?>
        try {
            val deleted = message["__deleted"] as? String == "true"

            if (deleted) {
                val id = message["id"] as? Number ?: return
                elasticsearchOperations.delete(id.toString(), IndexCoordinates.of("posts"))
                logger.info("Elasticsearch 문서 삭제 완료: id={}", id)
            } else {
                val postDocument = PostDocument(
                    id = (message["id"] as? Number)?.toLong() ?: return,
                    title = message["title"] as? String ?: return,
                    content = message["content"] as? String ?: return,
                    authorId = (message["author_id"] as? Number)?.toLong() ?: return,
                    createdAt = parseInstant(message["created_at"]),
                    updatedAt = parseInstant(message["updated_at"])
                )

                elasticsearchOperations.save(postDocument, IndexCoordinates.of("posts"))
                logger.info("Elasticsearch 문서 저장 완료: id={}, title={}", postDocument.id, postDocument.title)
            }
        } catch (e: Exception) {
            logger.error("Kafka 메시지 처리 실패: message={}, error={}", message, e.message, e)
        }
    }

    private fun parseInstant(value: Any?): java.time.Instant {
        return when (value) {
            is String -> java.time.Instant.parse(value)
            is Long -> java.time.Instant.ofEpochMilli(value)
            is Number -> java.time.Instant.ofEpochMilli(value.toLong())
            else -> java.time.Instant.now()
        }
    }
}
