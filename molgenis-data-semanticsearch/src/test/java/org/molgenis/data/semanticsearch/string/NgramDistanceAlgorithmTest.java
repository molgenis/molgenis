package org.molgenis.data.semanticsearch.string;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class NgramDistanceAlgorithmTest
{
	@Test
	public void createNGrams()
	{
		assertEquals(NGramDistanceAlgorithm.createNGrams("hypertensions", true).toString(),
				"{hy=1, te=1, s$=1, rt=1, pe=1, ns=1, yp=1, en=1, ^h=1, er=1}");

		assertEquals(NGramDistanceAlgorithm.createNGrams("hypertensive disorder", true).toString(),
				"{d$=1, rt=1, or=1, ns=1, di=1, ^d=1, en=1, ^h=1, is=1, er=1, hy=1, te=1, s$=1, rd=1, pe=1, yp=1, so=1}");

		assertEquals(NGramDistanceAlgorithm.createNGrams("hypertensive disorder disorder", true).toString(),
				"{d$=2, rt=1, or=2, ns=1, di=2, ^d=2, en=1, ^h=1, is=2, er=1, hy=1, te=1, s$=1, rd=2, pe=1, yp=1, so=2}");

		assertEquals(NGramDistanceAlgorithm.createNGrams("WHERE IS PAitent", true).toString(),
				"{t$=1, pa=1, te=1, ^p=1, nt=1, ai=1, en=1, it=1}");

		assertEquals(NGramDistanceAlgorithm.createNGrams("WHERE IS PAitent", false).toString(),
				"{nt=1, ai=1, en=1, is=1, ^i=1, it=1, er=1, t$=1, wh=1, s$=1, pa=1, te=1, ^p=1, re=1, ^w=1, he=1, e$=1}");
	}
}
