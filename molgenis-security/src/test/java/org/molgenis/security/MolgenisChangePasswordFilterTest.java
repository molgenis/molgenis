package org.molgenis.security;

import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.molgenis.security.account.AccountController.CHANGE_PASSWORD_URI;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { MolgenisChangePasswordFilterTest.Config.class })
@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class MolgenisChangePasswordFilterTest extends AbstractTestNGSpringContextTests
{
	@Mock
	private UserService userService;
	@Mock
	private User user;
	@Mock
	private FilterChain chain;

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private MolgenisChangePasswordFilter filter;

	private MockitoSession mockitoSession;

	@BeforeMethod
	public void beforeMethod()
	{
		mockitoSession = mockitoSession().strictness(STRICT_STUBS).initMocks(this).startMocking();
		filter = new MolgenisChangePasswordFilter(userService, redirectStrategy);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	@WithMockUser
	public void testDoFilterChangePassword() throws IOException, ServletException
	{
		when(userService.findByUsername("user")).thenReturn(user);
		when(user.isChangePassword()).thenReturn(true);
		request.setRequestURI("/login");

		filter.doFilter(request, response, chain);

		assertEquals(response.getRedirectedUrl(), CHANGE_PASSWORD_URI);
		verifyZeroInteractions(chain);
	}

	@Test
	@WithMockUser
	public void testDoFilterNoChangePassword() throws IOException, ServletException
	{
		when(userService.findByUsername("user")).thenReturn(user);
		when(user.isChangePassword()).thenReturn(false);
		request.setRequestURI("/login");

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	public void testDoFilterIgnoreOwnUri() throws IOException, ServletException
	{
		request.setRequestURI("/account/password/change");

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
		verifyZeroInteractions(userService);
	}

	@Test
	@WithMockUser
	public void testDoFilterChangePasswordHackyUri() throws IOException, ServletException
	{
		when(userService.findByUsername("user")).thenReturn(user);
		when(user.isChangePassword()).thenReturn(true);
		request.setRequestURI("/api/v2/account/password/change");

		filter.doFilter(request, response, chain);

		assertEquals(response.getRedirectedUrl(), CHANGE_PASSWORD_URI);
		verifyZeroInteractions(chain);
	}

	@Configuration
	static class Config
	{

	}
}