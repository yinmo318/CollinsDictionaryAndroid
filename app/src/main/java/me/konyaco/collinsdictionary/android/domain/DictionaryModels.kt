package me.konyaco.collinsdictionary.android.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchResult {
    @Serializable
    @SerialName("precise")
    data class PreciseWord(val word: String) : SearchResult

    @Serializable
    @SerialName("redirect")
    data class Redirect(val redirectTo: String) : SearchResult

    @Serializable
    @SerialName("not_found")
    data class NotFound(val alternatives: List<String>) : SearchResult
}

@Serializable
data class Word(
    val cobuildDictionary: CobuildDictionary,
)

@Serializable
data class CobuildDictionary(
    val sections: List<CobuildDictionarySection>,
)

@Serializable
data class CobuildDictionarySection(
    val word: String,
    val frequency: Int?,
    val forms: List<WordForm>,
    val pronunciation: Pronunciation,
    val definitionEntries: List<DefinitionEntry>,
)

@Serializable
data class WordForm(
    val description: String,
    val spell: String,
)

@Serializable
data class Pronunciation(
    val ipa: String?,
    val soundUrl: String?,
)

@Serializable
data class DefinitionEntry(
    val index: Int,
    val type: String,
    val definition: Definition,
    val extraDefinitions: List<Definition>,
)

@Serializable
data class Definition(
    val text: String,
    val examples: List<ExampleSentence>,
    val synonyms: List<String>,
)

@Serializable
data class ExampleSentence(
    val sentence: String,
    val grammarPattern: String?,
    val soundUrl: String?,
)
