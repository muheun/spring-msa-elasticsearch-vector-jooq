package me.muheun.moaspace.search.service

import me.muheun.moaspace.search.model.PostDocument
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.stereotype.Service

@Service
class ElasticsearchPostSearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) : PostSearchService {

    private val indexCoordinates = IndexCoordinates.of("posts")

    override fun search(
        keyword: String,
        page: Int,
        size: Int,
        sort: String
    ): PostSearchResult {
        val query = buildSearchQuery(keyword, page, size, sort)
        val searchHits: SearchHits<PostDocument> = elasticsearchOperations.search(
            query,
            PostDocument::class.java,
            indexCoordinates
        )

        val posts = searchHits.searchHits.map { it.content }
        return PostSearchResult(
            posts = posts,
            totalHits = searchHits.totalHits,
            page = page,
            size = size
        )
    }

    override fun getById(id: Long): PostDocument? {
        return elasticsearchOperations.get(id.toString(), PostDocument::class.java, indexCoordinates)
    }

    override fun countByKeyword(keyword: String): Long {
        val query = buildCountQuery(keyword)
        return elasticsearchOperations.count(query, PostDocument::class.java, indexCoordinates)
    }

    private fun buildSearchQuery(keyword: String, page: Int, size: Int, sort: String): Query {
        val criteria = if (keyword.isBlank()) {
            Criteria.where("id").exists()
        } else {
            Criteria.where("title").contains(keyword)
                .or("content").contains(keyword)
        }

        val sortOrder = when (sort) {
            "latest" -> Sort.by(Sort.Direction.DESC, "createdAt")
            else -> Sort.by(Sort.Direction.DESC, "_score")
        }

        return CriteriaQuery(criteria)
            .setPageable(PageRequest.of(page, size, sortOrder))
    }

    private fun buildCountQuery(keyword: String): Query {
        val criteria = Criteria.where("title").contains(keyword)
            .or("content").contains(keyword)

        return CriteriaQuery(criteria)
    }
}
