package me.konyaco.collinsdictionary.android.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.konyaco.collinsdictionary.android.domain.LookupResult
import me.konyaco.collinsdictionary.android.domain.ResultSource
import me.konyaco.collinsdictionary.android.domain.SearchResult
import me.konyaco.collinsdictionary.android.network.CollinsRemoteDictionary
import me.konyaco.collinsdictionary.android.storage.CacheStore

class DictionaryRepository(
    private val cacheStore: CacheStore,
    private val remoteDictionary: CollinsRemoteDictionary = CollinsRemoteDictionary(),
) {
    fun lookup(rawQuery: String): Flow<LookupResult> = flow {
        val query = normalizeQuery(rawQuery)
        require(query.isNotBlank()) { "Search word cannot be blank." }

        emitCachedResult(query)

        val remoteSearch = remoteDictionary.search(query)
        cacheStore.saveSearch(query, remoteSearch)
        if (remoteSearch is SearchResult.Redirect) {
            cacheStore.saveSearch(remoteSearch.redirectTo, SearchResult.PreciseWord(remoteSearch.redirectTo))
        }

        emit(resolveRemoteResult(query, remoteSearch))
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<LookupResult>.emitCachedResult(query: String) {
        when (val cachedSearch = cacheStore.getSearch(query)) {
            is SearchResult.PreciseWord -> {
                cacheStore.getDefinition(cachedSearch.word)?.let { word ->
                    emit(
                        LookupResult.Entry(
                            query = query,
                            source = ResultSource.CACHE,
                            resolvedWord = cachedSearch.word,
                            word = word,
                        )
                    )
                }
            }
            is SearchResult.Redirect -> {
                cacheStore.getDefinition(cachedSearch.redirectTo)?.let { word ->
                    emit(
                        LookupResult.Entry(
                            query = query,
                            source = ResultSource.CACHE,
                            resolvedWord = cachedSearch.redirectTo,
                            word = word,
                        )
                    )
                }
            }
            is SearchResult.NotFound -> {
                emit(
                    LookupResult.NotFound(
                        query = query,
                        source = ResultSource.CACHE,
                        alternatives = cachedSearch.alternatives,
                    )
                )
            }
            null -> Unit
        }
    }

    private suspend fun resolveRemoteResult(
        query: String,
        searchResult: SearchResult,
    ): LookupResult {
        return when (searchResult) {
            is SearchResult.PreciseWord -> resolveRemoteDefinition(
                query = query,
                word = searchResult.word,
            )
            is SearchResult.Redirect -> resolveRemoteDefinition(
                query = query,
                word = searchResult.redirectTo,
            )
            is SearchResult.NotFound -> LookupResult.NotFound(
                query = query,
                source = ResultSource.NETWORK,
                alternatives = searchResult.alternatives,
            )
        }
    }

    private suspend fun resolveRemoteDefinition(query: String, word: String): LookupResult {
        val definition = remoteDictionary.getDefinition(word)
            ?: return LookupResult.NotFound(
                query = query,
                source = ResultSource.NETWORK,
                alternatives = emptyList(),
            )

        cacheStore.saveDefinition(word, definition)
        return LookupResult.Entry(
            query = query,
            source = ResultSource.NETWORK,
            resolvedWord = word,
            word = definition,
        )
    }

    private fun normalizeQuery(query: String): String {
        return query.trim().replace(Regex("\\s+"), " ")
    }
}
