package org.molgenis.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class MolgenisUserServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public MolgenisUserServiceImpl molgenisUserServiceImpl()
		{
			return new MolgenisUserServiceImpl(database());
		}

		@Bean
		public Database database()
		{
			return mock(Database.class);
		}
	}

	@Autowired
	private MolgenisUserServiceImpl molgenisUserServiceImpl;

	@Autowired
	private Database database;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisUserServiceImpl()
	{
		new MolgenisUserServiceImpl(null);
	}

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

		assertEquals("username", molgenisUserServiceImpl.getCurrentUser().getUsername());
	}

}
