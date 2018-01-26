package org.molgenis.security;

import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.molgenis.security.account.AccountController.CHANGE_PASSWORD_URI;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { MolgenisChangePasswordFilterTest.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class MolgenisChangePasswordFilterTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MolgenisChangePasswordFilter filter;

	@Autowired
	private UserService userService;

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
	public void testDoFilterChangePassword() throws IOException, ServletException
	{
		User user = mock(User.class);
		when(user.isChangePassword()).thenReturn(true);
		when(userService.getUser("user")).thenReturn(user);

		request.setRequestURI("/login");

		filter.doFilter(request, response, chain);

		assertEquals(response.getRedirectedUrl(), CHANGE_PASSWORD_URI);
	}

	@Test
	@WithMockUser(username = "user")
	public void testDoFilterNoChangePassword() throws IOException, ServletException
	{
		User user = mock(User.class);
		when(user.isChangePassword()).thenReturn(false);
		when(userService.getUser("user")).thenReturn(user);

		request.setRequestURI("/login");

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	@WithMockUser(username = "user")
	public void testDoFilterIgnoreOwnUri() throws IOException, ServletException
	{
		request.setRequestURI("/account/password/change");

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	@WithMockUser(username = "user")
	public void testDoFilterChangePasswordHackyUri() throws IOException, ServletException
	{
		User user = mock(User.class);
		when(user.isChangePassword()).thenReturn(true);
		when(userService.getUser("user")).thenReturn(user);

		request.setRequestURI("/api/v2/account/password/change");

		filter.doFilter(request, response, chain);

		assertEquals(response.getRedirectedUrl(), CHANGE_PASSWORD_URI);
	}

	@Configuration
	static class Config
	{
		@Bean
		public MolgenisChangePasswordFilter molgenisChangePasswordFilter()
		{
			return new MolgenisChangePasswordFilter(userService(), redirectStrategy());
		}

		@Bean
		public RedirectStrategy redirectStrategy()
		{
			return new DefaultRedirectStrategy();
		}

		@Bean
		public UserService userService()
		{
			return mock(UserServiceImpl.class);
		}
	}
}