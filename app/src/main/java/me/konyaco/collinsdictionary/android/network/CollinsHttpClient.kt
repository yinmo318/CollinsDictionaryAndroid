package me.konyaco.collinsdictionary.android.network

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.konyaco.collinsdictionary.android.domain.SearchResult
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class CollinsHttpClient(
    private val parser: CollinsHtmlParser = CollinsHtmlParser(),
) {
    private val baseUrl = "https://www.collinsdictionary.com/".toHttpUrl()

    private val followRedirectClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    private val noRedirectClient = followRedirectClient.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    suspend fun search(word: String): SearchResult = withContext(Dispatchers.IO) {
        noRedirectClient.newCall(request(buildSearchUrl(word))).execute().use { response ->
            if (!response.isRedirect) {
                throw IOException("Unexpected Collins search response: HTTP ${response.code}")
            }

            val redirectedUrl = response.header("Location")
                ?.let(::resolveUrl)
                ?: throw IOException("Collins search did not include a redirect location.")

            when {
                redirectedUrl.isDictionaryUrl() -> {
                    val redirectedWord = redirectedUrl.pathSegments.lastOrNull()
                        ?.takeIf { it.isNotBlank() }
                        ?: throw IOException("Collins dictionary redirect did not include a word.")

                    if (redirectedWord.equals(word, ignoreCase = true)) {
                        SearchResult.PreciseWord(redirectedWord)
                    } else {
                        SearchResult.Redirect(redirectedWord)
                    }
                }
                redirectedUrl.isSpellcheckUrl() -> {
                    val spellcheckHtml = followRedirectClient.newCall(request(redirectedUrl))
                        .execute()
                        .use { spellcheckResponse ->
                            if (!spellcheckResponse.isSuccessful) {
                                throw IOException("Collins spellcheck failed: HTTP ${spellcheckResponse.code}")
                            }
                            spellcheckResponse.body?.string().orEmpty()
                        }

                    SearchResult.NotFound(parser.parseAlternatives(spellcheckHtml))
                }
                else -> throw IOException("Unexpected Collins redirect: $redirectedUrl")
            }
        }
    }

    suspend fun getDefinition(word: String): String = withContext(Dispatchers.IO) {
        followRedirectClient.newCall(request(buildDefinitionUrl(word))).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Collins definition failed: HTTP ${response.code}")
            }
            response.body?.string().orEmpty()
        }
    }

    private fun request(url: HttpUrl): Request {
        return Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
    }

    private fun buildSearchUrl(word: String): HttpUrl {
        return baseUrl.newBuilder()
            .addPathSegment("search")
            .addQueryParameter("dictCode", "english")
            .addQueryParameter("q", word)
            .build()
    }

    private fun buildDefinitionUrl(word: String): HttpUrl {
        return baseUrl.newBuilder()
            .addPathSegments("dictionary/english")
            .addPathSegment(word)
            .build()
    }

    private fun resolveUrl(location: String): HttpUrl {
        return location.toHttpUrlOrNull()
            ?: baseUrl.resolve(location)
            ?: throw IOException("Could not resolve Collins redirect: $location")
    }

    private fun HttpUrl.isDictionaryUrl(): Boolean {
        return host == baseUrl.host && encodedPath.startsWith("/dictionary/english/")
    }

    private fun HttpUrl.isSpellcheckUrl(): Boolean {
        return host == baseUrl.host && encodedPath.startsWith("/spellcheck/english")
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Android) CollinsDictionaryAndroid/2.0"
    }
}
