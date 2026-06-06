package me.konyaco.collinsdictionary.android.network

import me.konyaco.collinsdictionary.android.domain.SearchResult
import me.konyaco.collinsdictionary.android.domain.Word

class CollinsRemoteDictionary(
    private val httpClient: CollinsHttpClient = CollinsHttpClient(),
    private val parser: CollinsHtmlParser = CollinsHtmlParser(),
) {
    suspend fun search(word: String): SearchResult {
        return httpClient.search(word)
    }

    suspend fun getDefinition(word: String): Word? {
        return parser.parseWord(httpClient.getDefinition(word))
    }
}
