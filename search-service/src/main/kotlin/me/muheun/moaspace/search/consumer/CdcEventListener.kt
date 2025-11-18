package me.muheun.moaspace.search.consumer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class CdcEventListener(
    private val objectMapper: ObjectMapper,
    private val handlerRegistry: CdcEventHandlerRegistry,
    private val cdcEventValidator: CdcEventValidator,
    private val cdcEventMetrics: CdcEventMetrics
) {

    private val logger = LoggerFactory.getLogger(CdcEventListener::class.java)
    private val payloadType = object : TypeReference<Map<String, Any?>>() {}

    @KafkaListener(topics = ["#{@cdcTopicSupplier.topics}"])
    fun consume(messageJson: String, @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String) {
        val payload = runCatching {
            objectMapper.readValue(messageJson, payloadType)
        }.getOrElse {
            logger.error("Kafka 메시지 역직렬화 실패: {}", it.message, it)
            return
        }

        if (payload.isEmpty()) {
            logger.warn("Kafka 메시지에 payload가 비어 있어 무시됩니다. topic={}", topic)
            cdcEventMetrics.onInvalid(topic, "empty_payload")
            return
        }

        val deleted = payload["__deleted"] as? String == "true"
        val event = CdcEvent(
            topic = topic,
            payload = payload,
            deleted = deleted
        )

        cdcEventMetrics.onReceived(topic)

        val validation = cdcEventValidator.validate(event)
        if (!validation.isValid) {
            val reason = validation.reason ?: "invalid"
            cdcEventMetrics.onInvalid(topic, reason)
            logger.warn("CDC 이벤트 검증 실패: topic={}, reason={}, payload={}", topic, reason, payload)
            return
        }

        handlerRegistry.handle(topic, event)
        cdcEventMetrics.onDispatched(topic)
    }
}
