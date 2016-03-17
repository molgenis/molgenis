package org.molgenis.security.token;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.molgenis.auth.MolgenisToken;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.token.UnknownTokenException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceTokenServiceTest
{
	private DataServiceTokenService tokenService;
	private TokenGenerator tokenGenerator;
	private DataService dataService;
	private UserDetailsService userDetailsService;

	@BeforeMethod
	public void beforeMethod()
	{
		tokenGenerator = mock(TokenGenerator.class);
		dataService = mock(DataService.class);
		userDetailsService = mock(UserDetailsService.class);
		tokenService = new DataServiceTokenService(tokenGenerator, dataService, userDetailsService);
	}

	@Test
	public void findUserByToken()
	{
		MolgenisToken molgenisToken = new MolgenisToken();
		molgenisToken.setToken("token");
		MolgenisUser user = new MolgenisUser();
		user.setUsername("admin");
		molgenisToken.setMolgenisUser(user);

		when(
				dataService.findOne(MolgenisToken.ENTITY_NAME, new QueryImpl().eq(MolgenisToken.TOKEN, "token"),
						MolgenisToken.class)).thenReturn(molgenisToken);

		UserDetails userDetails = new User("admin", "admin", Arrays.asList(new SimpleGrantedAuthority("admin")));
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		assertEquals(tokenService.findUserByToken("token"), userDetails);
	}

	@Test(expectedExceptions = UnknownTokenException.class)
	public void findUserByTokenExpired()
	{
		MolgenisToken molgenisToken = new MolgenisToken();
		molgenisToken.setToken("token");
		molgenisToken.setExpirationDate(DateUtils.addDays(new Date(), -1));

		when(
				dataService.findOne(MolgenisToken.ENTITY_NAME, new QueryImpl().eq(MolgenisToken.TOKEN, "token"),
						MolgenisToken.class)).thenReturn(molgenisToken);

		tokenService.findUserByToken("token");
	}

	@Test
	public void generateAndStoreToken()
	{
		MolgenisUser user = new MolgenisUser();

		when(
				dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, "admin"),
						MolgenisUser.class)).thenReturn(user);

		when(tokenGenerator.generateToken()).thenReturn("token");
		assertEquals(tokenService.generateAndStoreToken("admin", "description"), "token");

		MolgenisToken molgenisToken = new MolgenisToken();
		molgenisToken.setToken("token");
		verify(dataService).add(MolgenisToken.ENTITY_NAME, molgenisToken);
	}

	@Test
	public void removeToken()
	{
		MolgenisToken molgenisToken = new MolgenisToken();
		molgenisToken.setToken("token");

		when(
				dataService.findOne(MolgenisToken.ENTITY_NAME, new QueryImpl().eq(MolgenisToken.TOKEN, "token"),
						MolgenisToken.class)).thenReturn(molgenisToken);

		tokenService.removeToken("token");
		verify(dataService).delete(MolgenisToken.ENTITY_NAME, molgenisToken);
	}
}
