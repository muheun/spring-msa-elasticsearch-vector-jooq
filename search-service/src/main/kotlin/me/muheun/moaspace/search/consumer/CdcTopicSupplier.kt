package me.muheun.moaspace.search.consumer

import me.muheun.moaspace.search.config.CdcProperties
import org.springframework.stereotype.Component

@Component("cdcTopicSupplier")
class CdcTopicSupplier(
    private val cdcProperties: CdcProperties
) {
    val topics: Array<String>
        get() = cdcProperties.topicHandlers.keys.toTypedArray()
}
