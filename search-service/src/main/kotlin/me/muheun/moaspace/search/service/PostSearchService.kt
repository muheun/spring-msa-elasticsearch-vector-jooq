package me.muheun.moaspace.search.service

import me.muheun.moaspace.search.model.PostDocument

interface PostSearchService {
    fun search(
        keyword: String,
        page: Int = 0,
        size: Int = 10,
        sort: String = "relevance"
    ): PostSearchResult

    fun getById(id: Long): PostDocument?

    fun countByKeyword(keyword: String): Long
}

data class PostSearchResult(
    val posts: List<PostDocument>,
    val totalHits: Long,
    val page: Int,
    val size: Int
)
