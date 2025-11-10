package me.muheun.moaspace.post.domain.dto

import java.time.Instant

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)
