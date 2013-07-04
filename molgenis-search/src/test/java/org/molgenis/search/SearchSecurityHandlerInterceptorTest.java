package org.molgenis.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SearchSecurityHandlerInterceptorTest
{
	private SearchSecurityHandlerInterceptor interceptor;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private ApplicationContext applicationContext;

	private Login login;
	private MolgenisSettings molgenisSettings;

	@BeforeMethod
	public void beforeMethod()
	{
		interceptor = new SearchSecurityHandlerInterceptor();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

		applicationContext = mock(ApplicationContext.class);
		new ApplicationContextProvider().setApplicationContext(applicationContext);
		login = mock(Login.class);
		molgenisSettings = mock(MolgenisSettings.class);
		when(applicationContext.getBean(Login.class)).thenReturn(login);
		when(applicationContext.getBean(MolgenisSettings.class)).thenReturn(molgenisSettings);
	}

	@Test
	public void testPreHandleAllowAnonymous() throws Exception
	{
		when(molgenisSettings.getProperty(SearchSecurityHandlerInterceptor.KEY_ACTION_ALLOW_ANONYMOUS_SEARCH, "false"))
				.thenReturn("true");
		when(login.isAuthenticated()).thenReturn(false);
		when(login.isLoginRequired()).thenReturn(false);
		assertTrue(interceptor.preHandle(request, response, null));
	}

	@Test
	public void testPreHandleAuthenticated() throws Exception
	{
		when(login.isAuthenticated()).thenReturn(true);
		when(login.isLoginRequired()).thenReturn(true);
		assertTrue(interceptor.preHandle(request, response, null));
	}

	@Test
	public void testPreHandleMissingLogin() throws Exception
	{
		when(applicationContext.getBean(Login.class)).thenReturn(null);
		assertFalse(interceptor.preHandle(request, response, null));
		assertEquals(response.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);
	}

}
