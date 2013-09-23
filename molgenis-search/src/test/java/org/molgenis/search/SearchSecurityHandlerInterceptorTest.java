package org.molgenis.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertTrue;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.SecurityUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SearchSecurityHandlerInterceptorTest
{
	private SearchSecurityHandlerInterceptor interceptor;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private Authentication authentication;
	private MolgenisSettings molgenisSettings;

	@BeforeMethod
	public void beforeMethod()
	{
		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		molgenisSettings = mock(MolgenisSettings.class);
		interceptor = new SearchSecurityHandlerInterceptor(molgenisSettings);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void testPreHandleAllowAnonymous() throws Exception
	{
		when(molgenisSettings.getProperty(SearchSecurityHandlerInterceptor.KEY_ACTION_ALLOW_ANONYMOUS_SEARCH, "false"))
				.thenReturn("true");
		when(authentication.isAuthenticated()).thenReturn(false);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn(
				SecurityUtils.ANONYMOUS_USERNAME).getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);
		assertTrue(interceptor.preHandle(request, response, null));
	}

	@Test
	public void testPreHandleAuthenticated() throws Exception
	{
		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("not-anonymous-user")
				.getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);
		assertTrue(interceptor.preHandle(request, response, null));
	}
}
