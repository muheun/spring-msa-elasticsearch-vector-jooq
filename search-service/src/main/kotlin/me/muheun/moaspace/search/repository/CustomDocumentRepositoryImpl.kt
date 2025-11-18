package me.muheun.moaspace.search.repository

import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.IndexOperations
import org.springframework.data.elasticsearch.core.index.AliasAction
import org.springframework.data.elasticsearch.core.index.AliasActionParameters
import org.springframework.data.elasticsearch.core.index.AliasActions
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Repository

@Repository
class CustomDocumentRepositoryImpl<T>(
    private val elasticsearchOperations: ElasticsearchOperations
) : CustomDocumentRepository<T> {

    private val logger = LoggerFactory.getLogger(CustomDocumentRepositoryImpl::class.java)

    override fun <S : T> save(entity: S, indexName: IndexCoordinates): S {
        return elasticsearchOperations.save(entity!!, indexName)
    }

    override fun <S : T> saveAll(entities: Iterable<S>, indexName: IndexCoordinates): Iterable<S> {
        return elasticsearchOperations.save(entities, indexName)
    }

    // Elasticsearch POST /{index}/_alias/{alias}
    override fun setAlias(indexNameWrapper: IndexCoordinates, aliasNameWrapper: IndexCoordinates): Boolean {
        val indexOperations: IndexOperations = elasticsearchOperations.indexOps(indexNameWrapper)
        val aliasActions = AliasActions()
        aliasActions.add(
            AliasAction.Add(
                AliasActionParameters.builder()
                    .withIndices(*indexOperations.indexCoordinates.indexNames)
                    .withAliases(aliasNameWrapper.indexName)
                    .build()
            )
        )
        return indexOperations.alias(aliasActions)
    }

    // Elasticsearch GET /_alias/{alias_name}
    override fun findIndexNamesByAlias(aliasNameWrapper: IndexCoordinates): Set<String> {
        return try {
            val indexOperations: IndexOperations = elasticsearchOperations.indexOps(aliasNameWrapper)
            val aliasData = indexOperations.getAliases(aliasNameWrapper.indexName)

            aliasData.keys
        } catch (e: Exception) {
            logger.info("Alias name: [ ${aliasNameWrapper.indexName} ] not found: ${e.message}")
            emptySet()
        }
    }

    override fun deleteIndex(indexNameWrapper: IndexCoordinates): Boolean {
        val indexOperations: IndexOperations = elasticsearchOperations.indexOps(indexNameWrapper)
        return indexOperations.delete()
    }

    override fun createIndex(indexNameWrapper: IndexCoordinates, clazz: Class<T>) {
        val settings = elasticsearchOperations.indexOps(clazz).createSettings()
        val mappings = elasticsearchOperations.indexOps(clazz).createMapping()
        val indexOperations: IndexOperations = elasticsearchOperations.indexOps(indexNameWrapper)
        if (!indexOperations.exists()) {
            indexOperations.create(settings, mappings)
            logger.info("Index created: [ ${clazz.typeName} ] ${indexOperations.indexCoordinates.indexName}")
        }
    }

    override fun getOperations(): ElasticsearchOperations {
        return elasticsearchOperations
    }
}
