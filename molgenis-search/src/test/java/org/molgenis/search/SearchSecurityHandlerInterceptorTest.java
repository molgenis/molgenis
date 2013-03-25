package org.molgenis.search;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.security.SimpleLogin;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SearchSecurityHandlerInterceptorTest
{
	private SearchSecurityHandlerInterceptor interceptor;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeMethod
	public void beforeMethod()
	{
		interceptor = new SearchSecurityHandlerInterceptor();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void testPreHandleAuthenticated() throws Exception
	{
		request.getSession().setAttribute("login", new SimpleLogin()
		{

			@Override
			public boolean isAuthenticated()
			{
				return true;
			}

			@Override
			public boolean isLoginRequired()
			{
				return true;
			}

		});
		assertTrue(interceptor.preHandle(request, response, null));
	}

	@Test
	public void testPreHandleMissingLogin() throws Exception
	{
		assertFalse(interceptor.preHandle(request, response, null));
		assertEquals(response.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Test
	public void testPreHandleLoginNotRequired() throws Exception
	{
		request.getSession().setAttribute("login", new SimpleLogin()
		{

			@Override
			public boolean isAuthenticated()
			{
				return false;
			}

			@Override
			public boolean isLoginRequired()
			{
				return false;
			}

		});
		assertTrue(interceptor.preHandle(request, response, null));
	}

}
