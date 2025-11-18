package me.muheun.moaspace.search.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cdc")
data class CdcProperties(
    val topicHandlers: Map<String, String> = emptyMap()
) {
    init {
        require(topicHandlers.isNotEmpty()) { "cdc.topic-handlers must contain at least one mapping" }
    }
}
