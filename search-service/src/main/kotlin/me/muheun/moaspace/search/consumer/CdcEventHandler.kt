package me.muheun.moaspace.search.consumer

interface CdcEventHandler {
    val key: String
    fun handle(event: CdcEvent)
}
