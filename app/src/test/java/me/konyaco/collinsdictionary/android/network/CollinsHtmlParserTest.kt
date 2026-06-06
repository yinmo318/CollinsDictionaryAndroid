package me.konyaco.collinsdictionary.android.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CollinsHtmlParserTest {
    private val parser = CollinsHtmlParser()

    @Test
    fun parsesCobuildWordSection() {
        val word = parser.parseWord(
            """
            <html>
              <body>
                <div id="main_content">
                  <div class="dictionary Cob_Adv_Brit dictentry">
                    <div class="title_container"><h2><span>hello</span></h2></div>
                    <span class="word-frequency-img" data-band="4"></span>
                    <div class="mini_h2">
                      <span class="pron">həloʊ</span>
                      <span data-src-mp3="https://example.com/hello.mp3"></span>
                    </div>
                    <span class="form inflected_forms type-infl">
                      <span class="type-gram">plural</span>
                      <span class="orth">hellos</span>
                    </span>
                    <div class="content definitions cobuild br ">
                      <div class="hom">
                        <span class="gramGrp pos">exclamation</span>
                        <div class="sense">
                          <span class="def">You say hello when you greet someone.</span>
                          <span class="cit type-example">
                            <span class="quote">Hello, John!</span>
                          </span>
                          <div class="thes">
                            <span class="form ref">hi</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </body>
            </html>
            """.trimIndent()
        )

        assertNotNull(word)
        val section = requireNotNull(word).cobuildDictionary.sections.single()
        assertEquals("hello", section.word)
        assertEquals(4, section.frequency)
        assertEquals("həloʊ", section.pronunciation.ipa)
        assertEquals("https://example.com/hello.mp3", section.pronunciation.soundUrl)
        assertEquals("hellos", section.forms.single().spell)
        assertEquals("exclamation", section.definitionEntries.single().type)
        assertEquals("hi", section.definitionEntries.single().definition.synonyms.single())
    }

    @Test
    fun parsesSpellcheckAlternatives() {
        val alternatives = parser.parseAlternatives(
            """
            <div id="main_content">
              <div class="columns2">
                <p><a href="/dictionary/english/interchangeable">interchangeable</a></p>
                <p><a href="/dictionary/english/interchangeably">interchangeably</a></p>
              </div>
            </div>
            """.trimIndent()
        )

        assertEquals(listOf("interchangeable", "interchangeably"), alternatives)
    }
}
