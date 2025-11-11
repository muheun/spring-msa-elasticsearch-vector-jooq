package me.muheun.moaspace.search.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Mapping
import org.springframework.data.elasticsearch.annotations.Setting
import java.time.Instant

@Document(indexName = "posts", createIndex = false)
@Setting(settingPath = "elasticsearch/settings/posts-settings.json")
@Mapping(mappingPath = "elasticsearch/mappings/posts-mapping.json")
data class PostDocument(
    @Id
    val id: Long,

    @Field(type = FieldType.Text, analyzer = "nori")
    val title: String,

    @Field(type = FieldType.Text, analyzer = "nori")
    val content: String,

    @Field(type = FieldType.Long)
    val authorId: Long,

    @Field(type = FieldType.Date)
    val createdAt: Instant,

    @Field(type = FieldType.Date)
    val updatedAt: Instant
)
