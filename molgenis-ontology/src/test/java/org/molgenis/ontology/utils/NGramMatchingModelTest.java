package org.molgenis.ontology.utils;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class NGramMatchingModelTest
{

	@Test
	public void testExample_1()
	{
		String originalString = "cranio facial dystonia";

		String string1 = "cranial";

		double stringMatching1 = NGramMatchingModel.stringMatching(originalString, string1);

		assertTrue(stringMatching1 < 50);
	}

	@Test
	public void testExample_2()
	{
		String originalString = "cranio facial dystonia";

		String string1 = "cranial dystonia";
		String string2 = "craniofacial dystonia";

		double stringMatching1 = NGramMatchingModel.stringMatching(originalString, string1);
		double stringMatching2 = NGramMatchingModel.stringMatching(originalString, string2);

		assertTrue(stringMatching1 < stringMatching2);
	}
}
