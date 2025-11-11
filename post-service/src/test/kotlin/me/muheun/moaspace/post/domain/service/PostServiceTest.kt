package me.muheun.moaspace.post.domain.service

import me.muheun.moaspace.post.domain.dto.PostCreateRequest
import me.muheun.moaspace.post.domain.dto.PostUpdateRequest
import me.muheun.moaspace.post.domain.exception.PostNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Rollback
class PostServiceTest {

    @Autowired
    private lateinit var postService: PostService

    @Test
    fun `게시글 생성 성공`() {
        val request = PostCreateRequest(
            title = "서비스 테스트 제목",
            content = "서비스 테스트 내용",
            authorId = 1L
        )

        val response = postService.createPost(request)

        assertThat(response.id).isNotNull()
        assertThat(response.title).isEqualTo("서비스 테스트 제목")
        assertThat(response.content).isEqualTo("서비스 테스트 내용")
        assertThat(response.authorId).isEqualTo(1L)
        assertThat(response.createdAt).isNotNull()
        assertThat(response.updatedAt).isNotNull()
    }

    @Test
    fun `게시글 ID로 조회 성공`() {
        val createRequest = PostCreateRequest(
            title = "조회용 게시글",
            content = "조회용 내용",
            authorId = 2L
        )
        val created = postService.createPost(createRequest)

        val retrieved = postService.getPostById(created.id)

        assertThat(retrieved.id).isEqualTo(created.id)
        assertThat(retrieved.title).isEqualTo("조회용 게시글")
    }

    @Test
    fun `존재하지 않는 게시글 조회 시 PostNotFoundException`() {
        assertThrows<PostNotFoundException> {
            postService.getPostById(999999L)
        }
    }

    @Test
    fun `게시글 목록 페이징 조회`() {
        repeat(15) { i ->
            postService.createPost(
                PostCreateRequest(
                    title = "목록 테스트 $i",
                    content = "내용 $i",
                    authorId = 1L
                )
            )
        }

        val page0 = postService.getAllPosts(0, 10)
        val page1 = postService.getAllPosts(1, 10)

        assertThat(page0.size).isLessThanOrEqualTo(10)
        assertThat(page0.size).isGreaterThan(0)
        assertThat(page1.size).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun `게시글 수정 성공`() {
        val createRequest = PostCreateRequest(
            title = "수정 전 제목",
            content = "수정 전 내용",
            authorId = 1L
        )
        val created = postService.createPost(createRequest)

        val updateRequest = PostUpdateRequest(
            title = "수정 후 제목",
            content = "수정 후 내용"
        )
        val updated = postService.updatePost(created.id, updateRequest)

        assertThat(updated.id).isEqualTo(created.id)
        assertThat(updated.title).isEqualTo("수정 후 제목")
        assertThat(updated.content).isEqualTo("수정 후 내용")
        assertThat(updated.updatedAt).isAfter(created.updatedAt)
    }

    @Test
    fun `존재하지 않는 게시글 수정 시 PostNotFoundException`() {
        val updateRequest = PostUpdateRequest(
            title = "수정 제목",
            content = "수정 내용"
        )

        assertThrows<PostNotFoundException> {
            postService.updatePost(999999L, updateRequest)
        }
    }

    @Test
    fun `게시글 삭제 성공`() {
        val createRequest = PostCreateRequest(
            title = "삭제할 게시글",
            content = "삭제 테스트",
            authorId = 1L
        )
        val created = postService.createPost(createRequest)

        postService.deletePost(created.id)

        assertThrows<PostNotFoundException> {
            postService.getPostById(created.id)
        }
    }

    @Test
    fun `존재하지 않는 게시글 삭제 시 PostNotFoundException`() {
        assertThrows<PostNotFoundException> {
            postService.deletePost(999999L)
        }
    }
}
