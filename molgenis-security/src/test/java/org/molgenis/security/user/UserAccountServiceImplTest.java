package org.molgenis.security.user;

import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { UserAccountServiceImplTest.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class UserAccountServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private UserAccountServiceImpl userAccountService;
	@Autowired
	private UserService userService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private User user = User.builder()
							.username("user")
							.id("1")
							.password("encrypted-password")
							.email("jan@example.com")
							.build();

	private User anonymous = User.builder()
								 .username("anonymous")
								 .id("1")
								 .password("encrypted-password")
								 .email("anonymous@example.com")
								 .build();

	@Test
	@WithMockUser
	public void testGetCurrentUserIfPresent() throws Exception
	{
		when(userService.findByUsernameIfPresent("user")).thenReturn(Optional.of(user));

		assertEquals(userAccountService.getCurrentUserIfPresent(), Optional.of(user));
	}

	@Test
	@WithMockUser
	public void testGetCurrentUserIfPresentNotFoundInDatabase() throws Exception
	{
		when(userService.findByUsernameIfPresent("user")).thenReturn(Optional.empty());

		assertEquals(userAccountService.getCurrentUserIfPresent(), Optional.empty());
	}

	@Test
	@WithAnonymousUser
	public void testGetCurrentUserIfPresentAnonymous() throws Exception
	{
		when(userService.findByUsernameIfPresent("anonymous")).thenReturn(Optional.of(anonymous));

		assertEquals(userAccountService.getCurrentUserIfPresent(), Optional.of(anonymous));
	}

	@Test
	public void testGetCurrentUserNoAuthentication()
	{
		assertEquals(userAccountService.getCurrentUserIfPresent(), Optional.empty());
	}

	@Test
	@WithMockUser
	public void updateCurrentUser()
	{
		User updatedUser = user.toBuilder().password("updated").build();

		userAccountService.updateCurrentUser(updatedUser);
		verify(passwordEncoder, never()).encode(any());
		verify(userService).update(updatedUser);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	@WithMockUser
	public void updateCurrentUser_wrongUser()
	{
		User updatedUser = user.toBuilder().username("other").password("updated").build();
		userAccountService.updateCurrentUser(updatedUser);
	}

	@Test
	@WithMockUser
	public void updateCurrentUser_changePassword()
	{
		when(passwordEncoder.matches("new-password", "encrypted-password")).thenReturn(true);

		User updatedUser = User.builder()
							   .username("user")
							   .id("1")
							   .password("new-password")
							   .twoFactorAuthentication(false)
							   .active(true)
							   .superuser(false)
							   .email("jan@example.com")
							   .changePassword(false)
							   .build();

		userAccountService.updateCurrentUser(updatedUser);

		verify(userService).update(updatedUser);
	}

	@Test
	@WithMockUser
	public void validateCurrentUserPassword()
	{
		when(userService.findByUsernameIfPresent("user")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "encrypted-password")).thenReturn(true);
		assertTrue(userAccountService.validateCurrentUserPassword("password"));
		assertFalse(userAccountService.validateCurrentUserPassword("wrong-password"));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public UserAccountServiceImpl userAccountServiceImpl()
		{
			return new UserAccountServiceImpl(userService(), passwordEncoder());
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return mock(PasswordEncoder.class);
		}

		@Bean
		public UserService userService()
		{
			return mock(UserService.class);
		}
	}
}
