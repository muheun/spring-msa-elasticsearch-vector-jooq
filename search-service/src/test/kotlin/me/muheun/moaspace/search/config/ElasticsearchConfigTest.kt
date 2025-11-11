package me.muheun.moaspace.search.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ElasticsearchConfigTest {

    @Autowired
    private lateinit var elasticsearchConfig: ElasticsearchConfig

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    @Test
    fun `ElasticsearchConfig Bean 생성 확인`() {
        assertThat(elasticsearchConfig).isNotNull
    }

    @Test
    fun `ElasticsearchOperations Bean 생성 확인`() {
        assertThat(elasticsearchOperations).isNotNull
    }

    @Test
    fun `Elasticsearch 연결 확인`() {
        val clientConfiguration = elasticsearchConfig.clientConfiguration()
        assertThat(clientConfiguration).isNotNull
    }
}
