package me.konyaco.collinsdictionary.android.storage

import java.io.File
import java.security.MessageDigest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.konyaco.collinsdictionary.android.domain.SearchResult
import me.konyaco.collinsdictionary.android.domain.Word

class CacheStore(
    rootDir: File,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    },
) {
    private val searchDir = rootDir.resolve("search").also { it.mkdirs() }
    private val definitionDir = rootDir.resolve("definition").also { it.mkdirs() }

    fun getSearch(query: String): SearchResult? {
        val file = fileFor(searchDir, query)
        return readOrDelete(file) {
            json.decodeFromString<SearchResult>(it)
        }
    }

    fun saveSearch(query: String, result: SearchResult) {
        atomicWrite(fileFor(searchDir, query), json.encodeToString(result))
    }

    fun getDefinition(word: String): Word? {
        val file = fileFor(definitionDir, word)
        return readOrDelete(file) {
            json.decodeFromString<Word>(it)
        }
    }

    fun saveDefinition(word: String, definition: Word) {
        atomicWrite(fileFor(definitionDir, word), json.encodeToString(definition))
    }

    private fun <T> readOrDelete(file: File, decode: (String) -> T): T? {
        if (!file.exists()) return null

        return try {
            decode(file.readText())
        } catch (_: SerializationException) {
            file.delete()
            null
        } catch (_: IllegalArgumentException) {
            file.delete()
            null
        }
    }

    private fun fileFor(dir: File, rawKey: String): File {
        return dir.resolve("${sha256(normalizeKey(rawKey))}.json")
    }

    private fun atomicWrite(file: File, value: String) {
        file.parentFile?.mkdirs()
        val temp = file.resolveSibling("${file.name}.tmp")
        temp.writeText(value)

        if (!temp.renameTo(file)) {
            file.delete()
            if (!temp.renameTo(file)) {
                throw IllegalStateException("Could not write cache file: ${file.name}")
            }
        }
    }

    private fun normalizeKey(value: String): String {
        return value.trim().lowercase()
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it.toInt() and 0xff) }
    }
}
