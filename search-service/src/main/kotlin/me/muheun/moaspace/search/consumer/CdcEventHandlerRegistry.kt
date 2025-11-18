package me.muheun.moaspace.search.consumer

import me.muheun.moaspace.search.config.CdcProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CdcEventHandlerRegistry(
    handlers: List<CdcEventHandler>,
    private val cdcProperties: CdcProperties
) {

    private val logger = LoggerFactory.getLogger(CdcEventHandlerRegistry::class.java)

    private val handlerByKey = handlers.associateBy { it.key }

    fun handle(topic: String, event: CdcEvent) {
        val handlerKey = cdcProperties.topicHandlers[topic]
        if (handlerKey == null) {
            logger.warn("지원되지 않는 Kafka 토픽: {}", topic)
            return
        }

        val handler = handlerByKey[handlerKey]
        if (handler == null) {
            logger.error("토픽 {} 에 매핑된 핸들러({})를 찾을 수 없습니다", topic, handlerKey)
            return
        }

        handler.handle(event)
    }
}
