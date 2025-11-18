package me.muheun.moaspace.search.consumer

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class CdcEventMetrics(
    private val meterRegistry: MeterRegistry
) {

    fun onReceived(topic: String) {
        meterRegistry.counter(RECEIVED_METRIC, TOPIC_TAG, topic).increment()
    }

    fun onInvalid(topic: String, reason: String) {
        meterRegistry.counter(INVALID_METRIC, TOPIC_TAG, topic, REASON_TAG, reason).increment()
    }

    fun onDispatched(topic: String) {
        meterRegistry.counter(DISPATCHED_METRIC, TOPIC_TAG, topic).increment()
    }

    companion object {
        private const val TOPIC_TAG = "topic"
        private const val REASON_TAG = "reason"
        private const val RECEIVED_METRIC = "cdc_events_received_total"
        private const val INVALID_METRIC = "cdc_events_invalid_total"
        private const val DISPATCHED_METRIC = "cdc_events_dispatched_total"
    }
}
