package org.molgenis.core.ui;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MolgenisUiUtilsTest
{

	@Test
	public void getCurrentUri()
	{
		String uri = "/menu/test";
		String queryString = "a=b&c=d";

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("javax.servlet.forward.request_uri", uri);
		request.setQueryString(queryString);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String currentUri = MolgenisUiUtils.getCurrentUri();
		assertNotNull(currentUri);
		assertEquals(currentUri, uri + "?" + queryString);

		request.setQueryString(null);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		currentUri = MolgenisUiUtils.getCurrentUri();
		assertNotNull(currentUri);
		assertEquals(currentUri, uri);
	}
}
