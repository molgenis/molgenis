package org.molgenis.semanticsearch.string;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.semanticsearch.string.Stemmer.cleanStemPhrase;

import org.junit.jupiter.api.Test;
import org.tartarus.snowball.ext.PorterStemmer;

class StemmerTest {
  @Test
  void replaceIllegalCharacter() {
    assertEquals("hello world", Stemmer.replaceIllegalCharacter("Hello__world!"));
    assertEquals("hello world 1234", Stemmer.replaceIllegalCharacter("Hello__world! 1234"));
    assertEquals("hello 45 world 1234", Stemmer.replaceIllegalCharacter("Hello_#45_world! 1234"));
  }

  @Test
  void stemPhrase() {
    assertEquals("i like smoke", cleanStemPhrase("i like smoking!"));
    assertEquals("it not possibl", cleanStemPhrase("it`s not possibilities!"));
  }

  @Test
  void stem() {
    assertEquals("us", Stemmer.stem("use"));
    assertEquals("hypertens", Stemmer.stem("hypertension"));

    PorterStemmer porterStemmer = new PorterStemmer();
    porterStemmer.setCurrent("use");
    porterStemmer.stem();
    assertEquals("us", porterStemmer.getCurrent());
  }
}
