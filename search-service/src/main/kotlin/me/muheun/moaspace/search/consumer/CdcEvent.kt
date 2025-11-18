package me.muheun.moaspace.search.consumer

data class CdcEvent(
    val topic: String,
    val payload: Map<String, Any?>,
    val deleted: Boolean
)
