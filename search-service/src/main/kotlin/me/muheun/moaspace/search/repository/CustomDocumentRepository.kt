package me.muheun.moaspace.search.repository

import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates

interface CustomDocumentRepository<T> {

    fun <S : T> save(entity: S, indexName: IndexCoordinates): S

    fun <S : T> saveAll(entities: Iterable<S>, indexName: IndexCoordinates): Iterable<S>

    fun setAlias(indexNameWrapper: IndexCoordinates, aliasNameWrapper: IndexCoordinates): Boolean

    fun findIndexNamesByAlias(aliasNameWrapper: IndexCoordinates): Set<String>

    fun deleteIndex(indexNameWrapper: IndexCoordinates): Boolean

    fun createIndex(indexNameWrapper: IndexCoordinates, clazz: Class<T>)

    fun getOperations(): ElasticsearchOperations
}
