package me.muheun.moaspace.post.domain.repository

import me.muheun.moaspace.post.domain.entity.Post
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.annotation.Rollback

@SpringBootTest
@Transactional
@Rollback
class PostRepositoryTest {

    @Autowired
    private lateinit var postRepository: PostRepository

    @Test
    fun `게시글 저장 성공`() {
        val post = Post(
            title = "테스트 제목",
            content = "테스트 내용",
            authorId = 1L
        )

        val savedPost = postRepository.save(post)

        assertThat(savedPost.id).isNotNull()
        assertThat(savedPost.title).isEqualTo("테스트 제목")
        assertThat(savedPost.content).isEqualTo("테스트 내용")
        assertThat(savedPost.authorId).isEqualTo(1L)
        assertThat(savedPost.createdAt).isNotNull()
        assertThat(savedPost.updatedAt).isNotNull()
    }

    @Test
    fun `게시글 ID로 조회 성공`() {
        val post = Post(
            title = "조회 테스트",
            content = "조회 내용",
            authorId = 2L
        )
        val savedPost = postRepository.save(post)

        val foundPost = postRepository.findById(savedPost.id!!)

        assertThat(foundPost).isPresent
        assertThat(foundPost.get().id).isEqualTo(savedPost.id)
        assertThat(foundPost.get().title).isEqualTo("조회 테스트")
    }

    @Test
    fun `존재하지 않는 ID로 조회 시 Optional empty`() {
        val foundPost = postRepository.findById(999999L)

        assertThat(foundPost).isEmpty
    }

    @Test
    fun `게시글 목록 페이징 조회`() {
        repeat(15) { i ->
            postRepository.save(
                Post(
                    title = "게시글 $i",
                    content = "내용 $i",
                    authorId = 1L
                )
            )
        }

        val page0 = postRepository.findAll(0, 10)
        val page1 = postRepository.findAll(1, 10)

        assertThat(page0.size).isLessThanOrEqualTo(10)
        assertThat(page0.size).isGreaterThan(0)
        assertThat(page1.size).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun `게시글 수정 성공`() {
        val post = Post(
            title = "원본 제목",
            content = "원본 내용",
            authorId = 1L
        )
        val savedPost = postRepository.save(post)

        val updatedPost = savedPost.copy(
            title = "수정된 제목",
            content = "수정된 내용"
        )
        val result = postRepository.update(savedPost.id!!, updatedPost)

        assertThat(result.title).isEqualTo("수정된 제목")
        assertThat(result.content).isEqualTo("수정된 내용")
        assertThat(result.updatedAt).isAfter(savedPost.updatedAt)
    }

    @Test
    fun `존재하지 않는 게시글 수정 시 예외 발생`() {
        val post = Post(
            title = "테스트",
            content = "테스트",
            authorId = 1L
        )

        org.junit.jupiter.api.assertThrows<NoSuchElementException> {
            postRepository.update(999999L, post)
        }
    }

    @Test
    fun `게시글 삭제 성공`() {
        val post = Post(
            title = "삭제할 게시글",
            content = "삭제 테스트",
            authorId = 1L
        )
        val savedPost = postRepository.save(post)

        val deleted = postRepository.deleteById(savedPost.id!!)

        assertThat(deleted).isTrue()
        assertThat(postRepository.findById(savedPost.id!!)).isEmpty
    }

    @Test
    fun `존재하지 않는 게시글 삭제 시 false 반환`() {
        val deleted = postRepository.deleteById(999999L)

        assertThat(deleted).isFalse()
    }
}
