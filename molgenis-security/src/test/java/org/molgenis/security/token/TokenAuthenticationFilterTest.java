package org.molgenis.security.token;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class TokenAuthenticationFilterTest
{
	private TokenAuthenticationFilter filter;
	private AuthenticationProvider authenticationProvider;

	@BeforeMethod
	public void beforeMethod()
	{
		authenticationProvider = mock(AuthenticationProvider.class);
		filter = new TokenAuthenticationFilter(authenticationProvider);
	}

	@Test
	public void doFilter() throws IOException, ServletException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		RestAuthenticationToken auth = new RestAuthenticationToken("admin", "admin",
				Arrays.asList(new SimpleGrantedAuthority("admin")), "token");

		request.setRequestURI("/api/v1/dataset");
		request.addHeader(TokenExtractor.TOKEN_HEADER, "token");
		when(authenticationProvider.authenticate(new RestAuthenticationToken("token"))).thenReturn(auth);

		filter.doFilter(request, response, chain);
		verify(chain).doFilter(request, response);

		assertEquals(SecurityContextHolder.getContext().getAuthentication(), auth);
	}
}
