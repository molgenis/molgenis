package org.molgenis.diseasematcher.service;

import static org.testng.AssertJUnit.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

public class PhenotipsServiceTest
{
	@Test
	public void testBuildQueryURIString() throws UnsupportedEncodingException
	{
		PhenotipsService ps = new PhenotipsService();

		List<String> terms = Arrays.asList("HP:0000252", "HP:0004322", "HP:0009900");
		String uri = ps.buildQueryURIString(terms);

		String targetUri = "http://playground.phenotips.org/bin/get/PhenoTips/OmimPredictService?q=1&format=html&limit=500&symptom=HP%3A0000252&symptom=HP%3A0004322&symptom=HP%3A0009900";
		assertEquals(targetUri, uri);
	}
}
