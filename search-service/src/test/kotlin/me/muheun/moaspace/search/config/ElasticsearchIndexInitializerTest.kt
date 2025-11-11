package me.muheun.moaspace.search.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ElasticsearchIndexInitializerTest {

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    @Test
    fun `Elasticsearch 인덱스 초기화 확인`() {
        val indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of("posts"))
        assertThat(indexOps.exists()).isTrue
    }

    @Test
    fun `Elasticsearch 인덱스 매핑 확인`() {
        val indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of("posts"))
        val mapping = indexOps.getMapping()

        assertThat(mapping).isNotNull
    }
}
