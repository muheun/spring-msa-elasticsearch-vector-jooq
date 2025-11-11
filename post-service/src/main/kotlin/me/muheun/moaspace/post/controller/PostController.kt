package me.muheun.moaspace.post.controller

import me.muheun.moaspace.post.domain.dto.PostCreateRequest
import me.muheun.moaspace.post.domain.dto.PostResponse
import me.muheun.moaspace.post.domain.dto.PostUpdateRequest
import me.muheun.moaspace.post.domain.service.PostService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/posts")
@Validated
class PostController(
    private val postService: PostService
) {

    @PostMapping
    fun createPost(@RequestBody @Valid request: PostCreateRequest): ResponseEntity<PostResponse> {
        val response = postService.createPost(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<PostResponse> {
        val response = postService.getPostById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<PostResponse>> {
        val responses = postService.getAllPosts(page, size)
        return ResponseEntity.ok(responses)
    }

    @PutMapping("/{id}")
    fun updatePost(
        @PathVariable id: Long,
        @RequestBody @Valid request: PostUpdateRequest
    ): ResponseEntity<PostResponse> {
        val response = postService.updatePost(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: Long): ResponseEntity<Void> {
        postService.deletePost(id)
        return ResponseEntity.noContent().build()
    }
}
