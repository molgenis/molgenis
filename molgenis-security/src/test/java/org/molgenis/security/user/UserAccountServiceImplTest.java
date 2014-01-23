package org.molgenis.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.UserAccountServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class UserAccountServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public UserAccountServiceImpl userAccountServiceImpl()
		{
			return new UserAccountServiceImpl();
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return mock(PasswordEncoder.class);
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}
	}

	private static final String USERNAME_USER = "username";
	private static Authentication AUTHENTICATION_PREVIOUS;
	private Authentication authentication;

	@Autowired
	private UserAccountServiceImpl userAccountServiceImpl;

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
		authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test
	public void getCurrentUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);

		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(molgenisUserService.getUser(USERNAME_USER)).thenReturn(existingMolgenisUser);
		assertEquals(userAccountServiceImpl.getCurrentUser(), existingMolgenisUser);
	}

	@Test
	public void updateCurrentUser()
	{
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getUsername()).thenReturn(USERNAME_USER);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		when(molgenisUserService.getUser(USERNAME_USER)).thenReturn(existingMolgenisUser);

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getUsername()).thenReturn("username");
		when(updatedMolgenisUser.getPassword()).thenReturn("encrypted-password");

		userAccountServiceImpl.updateCurrentUser(updatedMolgenisUser);
		verify(passwordEncoder, never()).encode("encrypted-password");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void updateCurrentUser_wrongUser()
	{
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		when(molgenisUserService.getUser(USERNAME_USER)).thenReturn(existingMolgenisUser);

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getUsername()).thenReturn("wrong-username");
		when(updatedMolgenisUser.getPassword()).thenReturn("encrypted-password");

		userAccountServiceImpl.updateCurrentUser(updatedMolgenisUser);
	}

	@Test
	public void updateCurrentUser_changePassword()
	{
		when(passwordEncoder.matches("new-password", "encrypted-password")).thenReturn(true);
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");
		when(existingMolgenisUser.getUsername()).thenReturn("username");

		when(molgenisUserService.getUser(USERNAME_USER)).thenReturn(existingMolgenisUser);

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getPassword()).thenReturn("new-password");
		when(updatedMolgenisUser.getUsername()).thenReturn("username");

		userAccountServiceImpl.updateCurrentUser(updatedMolgenisUser);
		verify(passwordEncoder, times(1)).encode("new-password");
	}

	@Test
	public void validateCurrentUserPassword()
	{
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");
		when(existingMolgenisUser.getUsername()).thenReturn("username");
		when(passwordEncoder.matches("password", "encrypted-password")).thenReturn(true);
		assertTrue(userAccountServiceImpl.validateCurrentUserPassword("password"));
		assertFalse(userAccountServiceImpl.validateCurrentUserPassword("wrong-password"));
	}
}
