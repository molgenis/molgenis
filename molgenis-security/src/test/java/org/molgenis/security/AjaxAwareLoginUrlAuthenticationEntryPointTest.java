package org.molgenis.security;

import org.springframework.security.core.AuthenticationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.OPTIONS;

public class AjaxAwareLoginUrlAuthenticationEntryPointTest
{
	private AjaxAwareLoginUrlAuthenticationEntryPoint ajaxAwareLoginUrlAuthenticationEntryPoint;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		ajaxAwareLoginUrlAuthenticationEntryPoint = new AjaxAwareLoginUrlAuthenticationEntryPoint("/login");
	}

	@Test
	public void testCommencePreflight() throws Exception
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn(OPTIONS.toString()).getMock();
		HttpServletResponse response = mock(HttpServletResponse.class);
		AuthenticationException authException = mock(AuthenticationException.class);
		ajaxAwareLoginUrlAuthenticationEntryPoint.commence(request, response, authException);
		verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	@Test
	public void testCommenceRest() throws Exception
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		Enumeration<String> headerValueEnumeration = enumeration(singleton("XMLHttpRequest"));
		when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		when(request.getHeaders("X-Requested-With")).thenReturn(headerValueEnumeration);
		HttpServletResponse response = mock(HttpServletResponse.class);
		AuthenticationException authException = mock(AuthenticationException.class);
		ajaxAwareLoginUrlAuthenticationEntryPoint.commence(request, response, authException);
		verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Test
	public void testCommenceOther() throws Exception
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("molgenis.org");
		when(request.getServerPort()).thenReturn(80);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.encodeRedirectURL("http://molgenis.org/login")).thenReturn("http://molgenis.org/login");
		AuthenticationException authException = mock(AuthenticationException.class);
		ajaxAwareLoginUrlAuthenticationEntryPoint.commence(request, response, authException);
		verify(response).sendRedirect("http://molgenis.org/login");
	}
}