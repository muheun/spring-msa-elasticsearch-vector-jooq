package me.muheun.moaspace.post.domain.exception

class PostNotFoundException(id: Long) : RuntimeException("게시글을 찾을 수 없습니다: id=$id")
