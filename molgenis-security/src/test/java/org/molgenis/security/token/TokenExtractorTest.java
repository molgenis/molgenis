package org.molgenis.security.token;

import static org.testng.Assert.assertEquals;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

public class TokenExtractorTest
{

	@Test
	public void getToken()
	{
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader(TokenExtractor.TOKEN_HEADER, "thetoken");
		assertEquals(TokenExtractor.getToken(req), "thetoken");
	}
}
