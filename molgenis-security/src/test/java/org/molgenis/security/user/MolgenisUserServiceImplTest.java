package org.molgenis.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class MolgenisUserServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@Configuration
	static class Config
	{
		@Bean
		public MolgenisUserServiceImpl molgenisUserServiceImpl()
		{
			return new MolgenisUserServiceImpl(dataService());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}

	@Autowired
	private MolgenisUserServiceImpl molgenisUserServiceImpl;

	@Autowired
	private DataService dataService;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisUserServiceImpl()
	{
		new MolgenisUserServiceImpl(null);
	}

	@BeforeClass
	public static void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("username").getMock();
		Authentication authentication = when(mock(Authentication.class).getPrincipal()).thenReturn(userDetails)
				.getMock();
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test
	public void getUser() throws DatabaseException
	{
		String username = "username";

		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getUsername()).thenReturn(username);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		when(dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, username)))
				.thenReturn(existingMolgenisUser);

		assertEquals(existingMolgenisUser, molgenisUserServiceImpl.getUser(username));
	}
}
