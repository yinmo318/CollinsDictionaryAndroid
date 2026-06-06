package me.konyaco.collinsdictionary.android.storage

import java.nio.file.Files
import me.konyaco.collinsdictionary.android.domain.SearchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CacheStoreTest {
    @Test
    fun searchCacheUsesSafeFileNames() {
        val dir = Files.createTempDirectory("collins-cache-test").toFile()
        try {
            val store = CacheStore(dir)
            val query = "../unsafe word"
            val result = SearchResult.PreciseWord("safe")

            store.saveSearch(query, result)

            assertEquals(result, store.getSearch(query))
            assertFalse(dir.resolve("search").resolve(query).exists())
        } finally {
            dir.deleteRecursively()
        }
    }
}
