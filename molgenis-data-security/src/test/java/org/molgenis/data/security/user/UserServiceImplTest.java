package org.molgenis.data.security.user;

import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.data.security.user.UserServiceImplTest.Config;
import org.molgenis.data.support.QueryImpl;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { Config.class })
public class UserServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@Configuration
	static class Config
	{
		@Bean
		public UserServiceImpl molgenisUserServiceImpl()
		{
			return new UserServiceImpl(dataService());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}

	@Autowired
	private UserServiceImpl molgenisUserServiceImpl;

	@Autowired
	private DataService dataService;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisUserServiceImpl()
	{
		new UserServiceImpl(null);
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
	public void getUser()
	{
		String username = "username";

		User existingUser = mock(User.class);
		when(existingUser.getId()).thenReturn("1");
		when(existingUser.getUsername()).thenReturn(username);
		when(existingUser.getPassword()).thenReturn("encrypted-password");

		when(dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, username),
				User.class)).thenReturn(existingUser);

		assertEquals(molgenisUserServiceImpl.getUser(username), existingUser);
	}
}
