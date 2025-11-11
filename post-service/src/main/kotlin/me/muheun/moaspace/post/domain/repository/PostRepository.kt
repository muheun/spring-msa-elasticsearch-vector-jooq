package me.muheun.moaspace.post.domain.repository

import me.muheun.moaspace.post.domain.entity.Post
import java.util.Optional

interface PostRepository {
    fun save(post: Post): Post
    fun findById(id: Long): Optional<Post>
    fun findAll(page: Int, size: Int): List<Post>
    fun update(id: Long, post: Post): Post
    fun deleteById(id: Long): Boolean
}
