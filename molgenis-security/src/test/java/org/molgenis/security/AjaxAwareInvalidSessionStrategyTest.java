package org.molgenis.security;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;

public class AjaxAwareInvalidSessionStrategyTest
{
	private static final String INVALID_SESSION_URL = "http://www.molgenis.org/login?expired";

	private AjaxAwareInvalidSessionStrategy ajaxAwareInvalidSessionStrategy;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		ajaxAwareInvalidSessionStrategy = new AjaxAwareInvalidSessionStrategy(INVALID_SESSION_URL);
	}

	@Test
	public void testOnInvalidSessionDetected() throws IOException, ServletException
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.encodeRedirectURL(INVALID_SESSION_URL)).thenReturn(INVALID_SESSION_URL);
		ajaxAwareInvalidSessionStrategy.onInvalidSessionDetected(request, response);
		verify(response).sendRedirect(INVALID_SESSION_URL);
	}

	@Test
	public void testOnInvalidSessionDetectedXmlHttpRequest() throws IOException, ServletException
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		Enumeration<String> headerValueEnumeration = enumeration(singleton("XMLHttpRequest"));
		when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		when(request.getHeaders("X-Requested-With")).thenReturn(headerValueEnumeration);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ajaxAwareInvalidSessionStrategy.onInvalidSessionDetected(request, response);
		verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}