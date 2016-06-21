package org.molgenis.security.token;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.MolgenisTokenMetaData.MOLGENIS_TOKEN;
import static org.molgenis.auth.MolgenisTokenMetaData.TOKEN;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.auth.MolgenisUserMetaData.USERNAME;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisToken;
import org.molgenis.auth.MolgenisTokenFactory;
import org.molgenis.auth.MolgenisTokenMetaData;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
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
		MolgenisTokenFactory molgenisTokenFactory = mock(MolgenisTokenFactory.class);
		when(molgenisTokenFactory.create()).thenAnswer(new Answer<MolgenisToken>()
		{
			@Override
			public MolgenisToken answer(InvocationOnMock invocation) throws Throwable
			{
				return new MolgenisToken(mock(MolgenisTokenMetaData.class));
			}
		});
		tokenService = new DataServiceTokenService(tokenGenerator, dataService, userDetailsService,
				molgenisTokenFactory);
	}

	@Test
	public void findUserByToken()
	{
		MolgenisToken molgenisToken = new MolgenisToken(mock(MolgenisTokenMetaData.class));
		molgenisToken.setToken("token");
		MolgenisUser user = new MolgenisUser(mock(MolgenisUserMetaData.class));
		user.setUsername("admin");
		molgenisToken.setMolgenisUser(user);

		when(dataService.findOne(MOLGENIS_TOKEN, new QueryImpl().eq(TOKEN, "token"), MolgenisToken.class))
				.thenReturn(molgenisToken);

		UserDetails userDetails = new User("admin", "admin", Arrays.asList(new SimpleGrantedAuthority("admin")));
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		assertEquals(tokenService.findUserByToken("token"), userDetails);
	}

	@Test(expectedExceptions = UnknownTokenException.class)
	public void findUserByTokenExpired()
	{
		MolgenisToken molgenisToken = new MolgenisToken(mock(MolgenisTokenMetaData.class));
		molgenisToken.setToken("token");
		molgenisToken.setExpirationDate(DateUtils.addDays(new Date(), -1));

		when(dataService.findOne(MOLGENIS_TOKEN, new QueryImpl().eq(TOKEN, "token"), MolgenisToken.class))
				.thenReturn(molgenisToken);

		tokenService.findUserByToken("token");
	}

	@Test
	public void generateAndStoreToken()
	{
		MolgenisUser user = new MolgenisUser(mock(MolgenisUserMetaData.class));

		when(dataService.findOne(MOLGENIS_USER, new QueryImpl().eq(USERNAME, "admin"), MolgenisUser.class))
				.thenReturn(user);

		when(tokenGenerator.generateToken()).thenReturn("token");
		assertEquals(tokenService.generateAndStoreToken("admin", "description"), "token");

		MolgenisToken molgenisToken = new MolgenisToken(mock(MolgenisTokenMetaData.class));
		molgenisToken.setToken("token");

		ArgumentCaptor<MolgenisToken> argumentCaptor = ArgumentCaptor.forClass(MolgenisToken.class);
		verify(dataService).add(eq(MOLGENIS_TOKEN), argumentCaptor.capture());
		assertEquals(argumentCaptor.getValue().getToken(), "token");
	}

	@Test
	public void removeToken()
	{
		MolgenisToken molgenisToken = new MolgenisToken(mock(MolgenisTokenMetaData.class));
		molgenisToken.setToken("token");

		when(dataService.findOne(MOLGENIS_TOKEN, new QueryImpl().eq(TOKEN, "token"), MolgenisToken.class))
				.thenReturn(molgenisToken);

		tokenService.removeToken("token");
		verify(dataService).delete(MOLGENIS_TOKEN, molgenisToken);
	}
}
