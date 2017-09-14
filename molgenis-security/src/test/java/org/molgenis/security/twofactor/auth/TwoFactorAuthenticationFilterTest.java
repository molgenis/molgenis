package org.molgenis.security.twofactor.auth;

import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationServiceImpl;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.security.user.UserAccountServiceImpl;
import org.molgenis.security.user.UserService;
import org.molgenis.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.DISABLED;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENFORCED;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { TwoFactorAuthenticationFilterTest.Config.class })
public class TwoFactorAuthenticationFilterTest extends AbstractTestNGSpringContextTests
{

	@Autowired
	private AuthenticationSettings authenticationSettings;
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
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		String initialRedirectUrl =
				TwoFactorAuthenticationController.URI + TwoFactorAuthenticationController.TWO_FACTOR_CONFIGURED_URI;
		assertEquals(response.getRedirectedUrl(), initialRedirectUrl);
	}

	@Test
	public void testDoFilterInternalIsConfiguredChangePassword() throws IOException, ServletException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
		SecurityContextHolder.getContext().setAuthentication(token);
		when(token.isAuthenticated()).thenReturn(true);

		request.setRequestURI("/account/password/change");
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		assertEquals(response.getRedirectedUrl(), null);
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
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(false);

		filter.doFilterInternal(request, response, chain);

		String configuredRedirectUrl =
				TwoFactorAuthenticationController.URI + TwoFactorAuthenticationController.TWO_FACTOR_ACTIVATION_URI;
		assertEquals(response.getRedirectedUrl(), configuredRedirectUrl);

	}

	@Test
	public void testDoFilterInternalNotAuthenticated() throws IOException, ServletException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		request.setRequestURI("/login");
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(DISABLED);

		filter.doFilterInternal(request, response, chain);
		verify(chain).doFilter(request, response);

	}

	@Configuration
	static class Config
	{
		@Bean
		public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter()
		{
			return new TwoFactorAuthenticationFilter(authenticationSettings(), twoFactorAuthenticationService(),
					redirectStrategy(), userAccountService());
		}

		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}

		@Bean
		public AuthenticationSettings authenticationSettings()
		{
			return mock(AuthenticationSettings.class);
		}

		@Bean
		public RedirectStrategy redirectStrategy()
		{
			return new DefaultRedirectStrategy();
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return mock(UserAccountServiceImpl.class);
		}

		@Bean
		public UserService userService()
		{
			return mock(UserServiceImpl.class);
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return new BCryptPasswordEncoder();
		}

	}
}
