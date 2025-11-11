package me.muheun.moaspace.post.domain.service

import me.muheun.moaspace.post.domain.dto.PostCreateRequest
import me.muheun.moaspace.post.domain.dto.PostResponse
import me.muheun.moaspace.post.domain.dto.PostUpdateRequest
import me.muheun.moaspace.post.domain.entity.Post
import me.muheun.moaspace.post.domain.exception.PostNotFoundException
import me.muheun.moaspace.post.domain.repository.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository
) {
    private val logger = LoggerFactory.getLogger(PostService::class.java)

    fun createPost(request: PostCreateRequest): PostResponse {
        logger.info("게시글 생성 시작: title={}", request.title)

        val post = Post(
            title = request.title,
            content = request.content,
            authorId = request.authorId
        )

        val savedPost = postRepository.save(post)
        logger.info("게시글 생성 완료: id={}", savedPost.id)

        return toResponse(savedPost)
    }

    @Transactional(readOnly = true)
    fun getPostById(id: Long): PostResponse {
        logger.debug("게시글 조회: id={}", id)

        val post = postRepository.findById(id)
            .orElseThrow { PostNotFoundException(id) }

        return toResponse(post)
    }

    @Transactional(readOnly = true)
    fun getAllPosts(page: Int, size: Int): List<PostResponse> {
        logger.debug("게시글 목록 조회: page={}, size={}", page, size)

        val posts = postRepository.findAll(page, size)

        return posts.map { toResponse(it) }
    }

    fun updatePost(id: Long, request: PostUpdateRequest): PostResponse {
        logger.info("게시글 수정 시작: id={}", id)

        val existingPost = postRepository.findById(id)
            .orElseThrow { PostNotFoundException(id) }

        val updatedPost = existingPost.copy(
            title = request.title,
            content = request.content
        )

        val savedPost = postRepository.update(id, updatedPost)
        logger.info("게시글 수정 완료: id={}", id)

        return toResponse(savedPost)
    }

    fun deletePost(id: Long) {
        logger.info("게시글 삭제 시작: id={}", id)

        val exists = postRepository.findById(id).isPresent
        if (!exists) {
            throw PostNotFoundException(id)
        }

        postRepository.deleteById(id)
        logger.info("게시글 삭제 완료: id={}", id)
    }

    private fun toResponse(post: Post): PostResponse {
        return PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            authorId = post.authorId,
            createdAt = post.createdAt!!,
            updatedAt = post.updatedAt!!
        )
    }
}
