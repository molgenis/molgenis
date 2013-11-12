package org.molgenis.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.UserAccountServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
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
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return mock(PasswordEncoder.class);
		}
	}

	@Autowired
	private UserAccountServiceImpl userAccountServiceImpl;

	@Autowired
	private Database database;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeClass
	public static void setUpBeforeClass()
	{
		SecurityContext context = mock(SecurityContext.class);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("username").getMock();
		Authentication authentication = when(mock(Authentication.class).getPrincipal()).thenReturn(userDetails)
				.getMock();
		when(context.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(context);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getCurrentUser() throws DatabaseException
	{
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getUsername()).thenReturn("username");
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUserSuccess = mock(Query.class);
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.USERNAME, "username")).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUserSuccess.find()).thenReturn(Arrays.<MolgenisUser> asList(existingMolgenisUser));

		assertEquals("username", userAccountServiceImpl.getCurrentUser().getUsername());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateCurrentUser() throws DatabaseException
	{
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getUsername()).thenReturn("username");
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUserSuccess = mock(Query.class);
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.USERNAME, "username")).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUserSuccess.find()).thenReturn(Arrays.<MolgenisUser> asList(existingMolgenisUser));

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getUsername()).thenReturn("username");
		when(updatedMolgenisUser.getPassword()).thenReturn("encrypted-password");

		userAccountServiceImpl.updateCurrentUser(updatedMolgenisUser);
		verify(passwordEncoder, never()).encode("encrypted-password");
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = RuntimeException.class)
	public void updateCurrentUser_wrongUser() throws DatabaseException
	{
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUserSuccess = mock(Query.class);
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.USERNAME, "username")).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUserSuccess.find()).thenReturn(Arrays.<MolgenisUser> asList(existingMolgenisUser));

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getUsername()).thenReturn("wrong-username");
		when(updatedMolgenisUser.getPassword()).thenReturn("encrypted-password");

		userAccountServiceImpl.updateCurrentUser(updatedMolgenisUser);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateCurrentUser_changePassword() throws DatabaseException
	{
		when(passwordEncoder.matches("new-password", "encrypted-password")).thenReturn(true);
		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");
		when(existingMolgenisUser.getUsername()).thenReturn("username");

		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUserSuccess = mock(Query.class);
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.USERNAME, "username")).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUserSuccess.find()).thenReturn(Arrays.<MolgenisUser> asList(existingMolgenisUser));

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getPassword()).thenReturn("new-password");
		when(updatedMolgenisUser.getUsername()).thenReturn("username");

		userAccountServiceImpl.updateCurrentUser(updatedMolgenisUser);
		verify(passwordEncoder, times(1)).encode("new-password");
	}

	@Test
	public void validateCurrentUserPassword() throws DatabaseException
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
