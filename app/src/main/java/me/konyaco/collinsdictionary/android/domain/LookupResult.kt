package me.konyaco.collinsdictionary.android.domain

enum class ResultSource {
    CACHE,
    NETWORK,
}

sealed interface LookupResult {
    val query: String
    val source: ResultSource

    data class Entry(
        override val query: String,
        override val source: ResultSource,
        val resolvedWord: String,
        val word: Word,
    ) : LookupResult

    data class NotFound(
        override val query: String,
        override val source: ResultSource,
        val alternatives: List<String>,
    ) : LookupResult
}
