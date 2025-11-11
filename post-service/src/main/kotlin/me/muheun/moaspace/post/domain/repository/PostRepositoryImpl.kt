package me.muheun.moaspace.post.domain.repository

import me.muheun.moaspace.post.domain.entity.Post
import me.muheun.moaspace.post.jooq.generated.tables.Posts.POSTS_
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Optional

@Repository
class PostRepositoryImpl(
    private val dsl: DSLContext
) : PostRepository {

    override fun save(post: Post): Post {
        val record = dsl.insertInto(POSTS_)
            .set(POSTS_.TITLE, post.title)
            .set(POSTS_.CONTENT, post.content)
            .set(POSTS_.AUTHOR_ID, post.authorId)
            .set(POSTS_.CREATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
            .set(POSTS_.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("게시글 저장 실패")

        return Post(
            id = record.id,
            title = record.title,
            content = record.content,
            authorId = record.authorId,
            createdAt = record.createdAt.toInstant(),
            updatedAt = record.updatedAt.toInstant()
        )
    }

    override fun findById(id: Long): Optional<Post> {
        val record = dsl.selectFrom(POSTS_)
            .where(POSTS_.ID.eq(id))
            .fetchOne()

        return Optional.ofNullable(record?.let {
            Post(
                id = it.id,
                title = it.title,
                content = it.content,
                authorId = it.authorId,
                createdAt = it.createdAt.toInstant(),
                updatedAt = it.updatedAt.toInstant()
            )
        })
    }

    override fun findAll(page: Int, size: Int): List<Post> {
        return dsl.selectFrom(POSTS_)
            .orderBy(POSTS_.CREATED_AT.desc())
            .limit(size)
            .offset(page * size)
            .fetch()
            .map {
                Post(
                    id = it.id,
                    title = it.title,
                    content = it.content,
                    authorId = it.authorId,
                    createdAt = it.createdAt.toInstant(),
                    updatedAt = it.updatedAt.toInstant()
                )
            }
    }

    override fun update(id: Long, post: Post): Post {
        val updated = dsl.update(POSTS_)
            .set(POSTS_.TITLE, post.title)
            .set(POSTS_.CONTENT, post.content)
            .set(POSTS_.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
            .where(POSTS_.ID.eq(id))
            .execute()

        if (updated == 0) {
            throw NoSuchElementException("게시글을 찾을 수 없습니다: id=$id")
        }

        return findById(id).orElseThrow {
            IllegalStateException("업데이트된 게시글 조회 실패: id=$id")
        }
    }

    override fun deleteById(id: Long): Boolean {
        val deleted = dsl.deleteFrom(POSTS_)
            .where(POSTS_.ID.eq(id))
            .execute()

        return deleted > 0
    }
}
