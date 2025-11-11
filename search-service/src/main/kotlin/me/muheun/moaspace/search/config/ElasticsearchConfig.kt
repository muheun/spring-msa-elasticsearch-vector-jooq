package me.muheun.moaspace.search.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.IndexOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

@Configuration
@EnableElasticsearchRepositories(basePackages = ["me.muheun.moaspace.search.repository"])
class ElasticsearchConfig : ElasticsearchConfiguration() {

    @Value("\${spring.elasticsearch.uris}")
    private lateinit var elasticsearchUrl: String

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo(elasticsearchUrl.removePrefix("http://"))
            .build()
    }
}

@Component
class ElasticsearchIndexInitializer(
    private val elasticsearchOperations: ElasticsearchOperations
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(ElasticsearchIndexInitializer::class.java)

    override fun run(args: ApplicationArguments?) {
        val indexOps: IndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of("posts"))

        if (indexOps.exists()) {
            logger.info("Elasticsearch 인덱스 'posts'가 이미 존재합니다")
            return
        }

        try {
            val settingsResource = ClassPathResource("elasticsearch/settings/posts-settings.json")
            val mappingsResource = ClassPathResource("elasticsearch/mappings/posts-mapping.json")

            val settingsJson = settingsResource.inputStream.bufferedReader().use { it.readText() }
            val mappingsJson = mappingsResource.inputStream.bufferedReader().use { it.readText() }

            val objectMapper = ObjectMapper()
            val settings = objectMapper.readTree(settingsJson).get("settings")
            val mappings = objectMapper.readTree(mappingsJson).get("mappings")

            val indexSettings = org.springframework.data.elasticsearch.core.document.Document.create()
            indexSettings.putAll(objectMapper.convertValue(settings, Map::class.java) as Map<String, Any>)

            indexOps.create(indexSettings)
            logger.info("Elasticsearch 인덱스 'posts' 생성 완료 (settings 적용)")

            val mappingDocument = org.springframework.data.elasticsearch.core.document.Document.create()
            mappingDocument.putAll(objectMapper.convertValue(mappings, Map::class.java) as Map<String, Any>)
            indexOps.putMapping(mappingDocument)
            logger.info("Elasticsearch 인덱스 'posts' 매핑 적용 완료")

        } catch (e: Exception) {
            logger.error("Elasticsearch 인덱스 초기화 실패: ${e.message}", e)
            throw RuntimeException("Failed to initialize Elasticsearch index", e)
        }
    }
}
