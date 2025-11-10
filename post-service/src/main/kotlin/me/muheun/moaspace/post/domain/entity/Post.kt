package me.muheun.moaspace.post.domain.entity

import java.time.Instant

data class Post(
    val id: Long? = null,
    val title: String,
    val content: String,
    val authorId: Long,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
