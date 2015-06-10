package org.molgenis.data.semanticsearch.string;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class StemmerTest
{
	Stemmer stemmer = new Stemmer("EN");

	@Test
	public void stem()
	{
		assertEquals(stemmer.stem("use"), "use");
		assertEquals(stemmer.stem("hypertension"), "hypertens");
	}
}
