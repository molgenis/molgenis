package org.molgenis.semanticsearch.string;

import static org.testng.Assert.assertEquals;

import org.tartarus.snowball.ext.PorterStemmer;
import org.testng.annotations.Test;

public class StemmerTest {
  @Test
  public void replaceIllegalCharacter() {
    assertEquals(Stemmer.replaceIllegalCharacter("Hello__world!"), "hello world");
    assertEquals(Stemmer.replaceIllegalCharacter("Hello__world! 1234"), "hello world 1234");
    assertEquals(Stemmer.replaceIllegalCharacter("Hello_#45_world! 1234"), "hello 45 world 1234");
  }

  @Test
  public void stemPhrase() {
    assertEquals(Stemmer.cleanStemPhrase("i like smoking!"), "i like smoke");
    assertEquals(Stemmer.cleanStemPhrase("it`s not possibilities!"), "it not possibl");
  }

  @Test
  public void stem() {
    assertEquals(Stemmer.stem("use"), "us");
    assertEquals(Stemmer.stem("hypertension"), "hypertens");

    PorterStemmer porterStemmer = new PorterStemmer();
    porterStemmer.setCurrent("use");
    porterStemmer.stem();
    assertEquals(porterStemmer.getCurrent(), "us");
  }
}
