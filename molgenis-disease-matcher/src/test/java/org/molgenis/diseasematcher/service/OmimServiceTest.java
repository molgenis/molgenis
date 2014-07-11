package org.molgenis.diseasematcher.service;

import static org.testng.AssertJUnit.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.molgenis.diseasematcher.service.OmimService;
import org.testng.annotations.Test;

public class OmimServiceTest
{

	@Test
	public void testBuildQueryURI() throws UnsupportedEncodingException
	{
		OmimService os = new OmimService(Arrays.asList("a,b,c.".split(",")));
		String uri = os.buildQueryURIString("12345", "KLJCV347VSDFJLKJ38723LKJLKJ");

		String targetUri = "http://api.europe.omim.org/api/entry?mimNumber=12345&include=text&include=clinicalSynopsis&format=json&apiKey=KLJCV347VSDFJLKJ38723LKJLKJ";
		assertEquals(uri, targetUri);
	}
}
