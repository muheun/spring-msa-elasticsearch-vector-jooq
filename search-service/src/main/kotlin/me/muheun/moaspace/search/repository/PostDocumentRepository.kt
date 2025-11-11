package me.muheun.moaspace.search.repository

import me.muheun.moaspace.search.model.PostDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PostDocumentRepository :
    ElasticsearchRepository<PostDocument, Long>,
    CustomDocumentRepository<PostDocument>
