package org.molgenis.compute.db.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

public class BasicAuthenticationTest
{

	@Test
	public void getUsernamePassword()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Basic YWRtaW46dGVzdA==");

		BasicAuthentication.Result result = BasicAuthentication.getUsernamePassword(request);
		assertNotNull(result);
		assertEquals(result.getUsername(), "admin");
		assertEquals(result.getPassword(), "test");
	}

	@Test
	public void missingHeader()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		BasicAuthentication.Result result = BasicAuthentication.getUsernamePassword(request);
		assertNull(result);
	}
}
