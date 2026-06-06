package me.konyaco.collinsdictionary.android.network

import me.konyaco.collinsdictionary.android.domain.CobuildDictionary
import me.konyaco.collinsdictionary.android.domain.CobuildDictionarySection
import me.konyaco.collinsdictionary.android.domain.Definition
import me.konyaco.collinsdictionary.android.domain.DefinitionEntry
import me.konyaco.collinsdictionary.android.domain.ExampleSentence
import me.konyaco.collinsdictionary.android.domain.Pronunciation
import me.konyaco.collinsdictionary.android.domain.Word
import me.konyaco.collinsdictionary.android.domain.WordForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class CollinsHtmlParser {
    fun parseAlternatives(html: String): List<String> {
        val document = Jsoup.parse(html)
        val mainContent = document.getElementById("main_content") ?: return emptyList()
        val columnLinks = mainContent.select(".columns2 a")
        val links = if (columnLinks.isNotEmpty()) {
            columnLinks
        } else {
            mainContent.select("a[href*=/dictionary/english/]")
        }

        return links
            .map { it.text().trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun parseWord(html: String): Word? {
        val document = Jsoup.parse(html)
        val mainContent = document.getElementById("main_content") ?: return null
        val sections = findCobuildSections(mainContent).mapNotNull(::parseSection)

        return sections
            .takeIf { it.isNotEmpty() }
            ?.let { Word(CobuildDictionary(it)) }
    }

    private fun findCobuildSections(mainContent: Element): List<Element> {
        val directSections = mainContent.select("div.dictionary.Cob_Adv_Brit.dictentry")
        if (directSections.isNotEmpty()) return directSections

        val groupedSections = mainContent.select("div.dictionary.Cob_Adv_Brit div.dictentry.dictlink")
        if (groupedSections.isNotEmpty()) return groupedSections

        return mainContent.select("div.dictentry")
    }

    private fun parseSection(section: Element): CobuildDictionarySection? {
        val word = section.selectFirst(".title_container h2 span")
            ?.text()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: section.selectFirst(".orth")
                ?.text()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
            ?: return null

        val definitions = parseDefinitions(section)
        if (definitions.isEmpty()) return null

        return CobuildDictionarySection(
            word = word,
            frequency = section.selectFirst(".word-frequency-img")
                ?.attr("data-band")
                ?.toIntOrNull(),
            forms = parseWordForms(section),
            pronunciation = parsePronunciation(section),
            definitionEntries = definitions,
        )
    }

    private fun parseWordForms(section: Element): List<WordForm> {
        val formElement = section.selectFirst(".form.inflected_forms.type-infl") ?: return emptyList()
        val result = mutableListOf<WordForm>()
        val pendingDescriptions = mutableListOf<String>()

        for (child in formElement.children()) {
            when {
                child.hasClass("type-gram") -> {
                    child.ownText().trim().takeIf { it.isNotBlank() }?.let(pendingDescriptions::add)
                }
                child.hasClass("orth") -> {
                    val spell = child.ownText().trim()
                    if (spell.isNotBlank()) {
                        if (pendingDescriptions.isEmpty()) {
                            result.add(WordForm(description = "", spell = spell))
                        } else {
                            pendingDescriptions.forEach { description ->
                                result.add(WordForm(description = description, spell = spell))
                            }
                            pendingDescriptions.clear()
                        }
                    }
                }
            }
        }

        return result
    }

    private fun parsePronunciation(section: Element): Pronunciation {
        val rawIpa = section.selectFirst(".pron")?.text()?.trim()
        val soundUrl = section.selectFirst("[data-src-mp3]")
            ?.attr("data-src-mp3")
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        return Pronunciation(
            ipa = rawIpa?.trim('/'),
            soundUrl = soundUrl,
        )
    }

    private fun parseDefinitions(section: Element): List<DefinitionEntry> {
        val definitionRoot = section.selectFirst(".content.definitions.cobuild.br") ?: section
        return definitionRoot.select(".hom").mapIndexedNotNull { zeroBasedIndex, hom ->
            val type = hom.selectFirst(".gramGrp.pos")?.text()
                ?: hom.selectFirst(".gramGrp .pos")?.text()
                ?: hom.selectFirst(".pos")?.text()
                ?: return@mapIndexedNotNull null

            val sense = hom.selectFirst(".sense") ?: hom
            val definitionText = sense.selectFirst(".def")
                ?.text()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return@mapIndexedNotNull null

            DefinitionEntry(
                index = zeroBasedIndex + 1,
                type = type,
                definition = Definition(
                    text = definitionText,
                    examples = parseExamples(sense),
                    synonyms = parseSynonyms(sense),
                ),
                extraDefinitions = parseExtraDefinitions(sense),
            )
        }
    }

    private fun parseExamples(sense: Element): List<ExampleSentence> {
        return sense.select(".cit.type-example").mapNotNull { example ->
            val sentence = example.selectFirst(".quote")
                ?.text()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            ExampleSentence(
                sentence = sentence,
                grammarPattern = example.selectFirst(".gramGrp")
                    ?.text()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() },
                soundUrl = example.selectFirst("[data-src-mp3]")
                    ?.attr("data-src-mp3")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() },
            )
        }
    }

    private fun parseSynonyms(sense: Element): List<String> {
        return sense.select(".thes .form.ref")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun parseExtraDefinitions(sense: Element): List<Definition> {
        return sense.select(".sense").drop(1).mapNotNull { extraSense ->
            val text = extraSense.selectFirst(".def")
                ?.text()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            Definition(
                text = text,
                examples = parseExamples(extraSense),
                synonyms = parseSynonyms(extraSense),
            )
        }
    }
}
