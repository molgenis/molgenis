package org.molgenis.security.twofactor.auth;

import org.molgenis.data.security.user.UserService;
import org.molgenis.data.security.user.UserServiceImpl;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationServiceImpl;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.security.user.UserAccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.DISABLED;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENFORCED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { TwoFactorAuthenticationFilterTest.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class TwoFactorAuthenticationFilterTest extends AbstractTestNGSpringContextTests
{

	@Autowired
	private AuthenticationSettings authenticationSettings;
	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	@Autowired
	private TwoFactorAuthenticationFilter filter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private FilterChain chain;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		chain = mock(FilterChain.class);
	}

	@Test
	@WithMockUser(username = "user")
	public void testDoFilterInternalIsConfigured() throws IOException, ServletException
	{
		request.setRequestURI("/login");
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		String initialRedirectUrl =
				TwoFactorAuthenticationController.URI + TwoFactorAuthenticationController.TWO_FACTOR_CONFIGURED_URI;
		assertEquals(response.getRedirectedUrl(), initialRedirectUrl);
	}

	@Test
	@WithMockUser(username = "user")
	public void testDoFilterInternalIsConfiguredChangePassword() throws IOException, ServletException
	{
		request.setRequestURI("/account/password/change");
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		assertNull(response.getRedirectedUrl());
	}

	@Test
	@WithMockUser(username = "user")
	public void testDoFilterInternalIsNotConfigured() throws IOException, ServletException
	{
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
		request.setRequestURI("/login");
		when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(DISABLED);

		filter.doFilterInternal(request, response, chain);
		verify(chain).doFilter(request, response);

	}

	@Test
	@WithMockUser
	public void testDoFilterInternalRecoveryAuthenticated() throws IOException, ServletException
	{
		SecurityContext previous = SecurityContextHolder.getContext();
		try
		{
			SecurityContext testContext = SecurityContextHolder.createEmptyContext();
			SecurityContextHolder.setContext(testContext);
			testContext.setAuthentication(new RecoveryAuthenticationToken("recovery"));

			request.setRequestURI("/menu/main/dataexplorer");
			when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);

			filter.doFilterInternal(request, response, chain);
			verify(chain).doFilter(request, response);
		}
		finally
		{
			SecurityContextHolder.setContext(previous);
		}
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
