package org.molgenis.security.twofactor;

import org.molgenis.data.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { TwoFactorAuthenticationFilterTest.Config.class })
public class TwoFactorAuthenticationFilterTest extends AbstractTestNGSpringContextTests
{

	@Configuration
	static class Config
	{
		@Bean
		public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter()
		{
			return new TwoFactorAuthenticationFilter(appSettings(), twoFactorAuthenticationService(),
					redirectStrategy());
		}

		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public RedirectStrategy redirectStrategy()
		{
			return new DefaultRedirectStrategy();
		}

	}

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	private TwoFactorAuthenticationFilter filter;

	@Test
	public void testDoFilterInternalIsConfigured() throws IOException, ServletException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
		SecurityContextHolder.getContext().setAuthentication(token);
		when(token.isAuthenticated()).thenReturn(true);

		request.setRequestURI("/login");
		when(appSettings.getTwoFactorAuthentication()).thenReturn(TwoFactorAuthenticationSetting.ENFORCED.toString());
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		String iniitalRedirectUrl =
				TwoFactorAuthenticationController.URI + TwoFactorAuthenticationController.TWO_FACTOR_CONFIGURED_URI;
		assertEquals(iniitalRedirectUrl, response.getRedirectedUrl());

	}

	@Test
	public void testDoFilterInternalIsNotConfigured() throws IOException, ServletException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
		when(token.isAuthenticated()).thenReturn(true);
		SecurityContextHolder.getContext().setAuthentication(token);

		request.setRequestURI("/login");
		when(appSettings.getTwoFactorAuthentication()).thenReturn(TwoFactorAuthenticationSetting.ENFORCED.toString());
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(false);

		filter.doFilterInternal(request, response, chain);

		String configuredRedirectUrl =
				TwoFactorAuthenticationController.URI + TwoFactorAuthenticationController.TWO_FACTOR_INITIAL_URI;
		assertEquals(configuredRedirectUrl, response.getRedirectedUrl());

	}

	@Test
	public void testDoFilterInternalNotAuthenticated() throws IOException, ServletException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		request.setRequestURI("/login");
		when(appSettings.getTwoFactorAuthentication()).thenReturn(TwoFactorAuthenticationSetting.DISABLED.toString());

		filter.doFilterInternal(request, response, chain);
		verify(chain).doFilter(request, response);

	}
}
