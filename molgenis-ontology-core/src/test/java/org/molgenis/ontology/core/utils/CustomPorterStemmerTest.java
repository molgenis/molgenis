package org.molgenis.ontology.core.utils;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class CustomPorterStemmerTest
{
	private final CustomPorterStemmer customPorterStemmer = new CustomPorterStemmer();

	@Test
	public void replaceIllegalCharacter()
	{
		assertEquals(customPorterStemmer.replaceIllegalCharacter("Hello__world!"), "Hello world");
		assertEquals(customPorterStemmer.replaceIllegalCharacter("Hello__world! 1234"), "Hello world 1234");
		assertEquals(customPorterStemmer.replaceIllegalCharacter("Hello_#45_world! 1234"), "Hello 45 world 1234");
	}

	@Test
	public void stemPhrase()
	{
		assertEquals(customPorterStemmer.cleanStemPhrase("i like smoking!"), "i like smoke");
		assertEquals(customPorterStemmer.cleanStemPhrase("it`s not possibilities!"), "it not possibl");
	}
}
