package org.molgenis.semanticsearch.string;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.semanticsearch.string.NGramDistanceAlgorithm.stringMatching;

import org.junit.jupiter.api.Test;

class NgramDistanceAlgorithmTest {
  @Test
  void calculateScore() {
    assertEquals(100.0, stringMatching("hypertensive", "hypertensive"));

    assertEquals(
        NGramDistanceAlgorithm.stringMatching("hypertensive disorder", "hypertensive order"),
        72.727,
        0.001);

    assertEquals(0.0, stringMatching("hypertensive", "diabetes"));

    assertEquals(0.0, stringMatching("", ""));
  }

  @Test
  void createNGrams() {
    assertEquals(
        "{hy=1, te=1, s$=1, rt=1, pe=1, ns=1, yp=1, en=1, ^h=1, er=1}",
        NGramDistanceAlgorithm.createNGrams("hypertensions", true).toString());

    assertEquals(
        "{d$=1, rt=1, or=1, ns=1, di=1, ^d=1, en=1, ^h=1, is=1, er=1, hy=1, te=1, s$=1, rd=1, pe=1, yp=1, so=1}",
        NGramDistanceAlgorithm.createNGrams("hypertensive disorder", true).toString());

    assertEquals(
        "{d$=2, rt=1, or=2, ns=1, di=2, ^d=2, en=1, ^h=1, is=2, er=1, hy=1, te=1, s$=1, rd=2, pe=1, yp=1, so=2}",
        NGramDistanceAlgorithm.createNGrams("hypertensive disorder disorder", true).toString());

    assertEquals(
        "{t$=1, pa=1, te=1, ^p=1, nt=1, ai=1, en=1, it=1}",
        NGramDistanceAlgorithm.createNGrams("WHERE IS PAitent", true).toString());

    assertEquals(
        "{nt=1, ai=1, en=1, ^i=1, it=1, er=1, t$=1, wh=1, pa=1, te=1, ^p=1, re=1, ^w=1, i$=1, he=1, e$=1}",
        NGramDistanceAlgorithm.createNGrams("WHERE IS PAitent", false).toString());
  }
}
