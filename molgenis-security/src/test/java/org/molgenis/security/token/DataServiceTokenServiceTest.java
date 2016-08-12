package org.molgenis.security.token;

import org.apache.commons.lang3.time.DateUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisToken;
import org.molgenis.auth.MolgenisTokenFactory;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.security.core.token.UnknownTokenException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.MolgenisTokenMetaData.MOLGENIS_TOKEN;
import static org.molgenis.auth.MolgenisTokenMetaData.TOKEN;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.auth.MolgenisUserMetaData.USERNAME;
import static org.testng.Assert.assertEquals;

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
				return mock(MolgenisToken.class);
			}
		});
		tokenService = new DataServiceTokenService(tokenGenerator, dataService, userDetailsService,
				molgenisTokenFactory);
	}

	@Test
	public void findUserByToken()
	{
		MolgenisUser user = mock(MolgenisUser.class);
		when(user.getUsername()).thenReturn("admin");
		MolgenisToken molgenisToken = mock(MolgenisToken.class);
		when(molgenisToken.getToken()).thenReturn("token");
		when(molgenisToken.getMolgenisUser()).thenReturn(user);

		Query<MolgenisToken> q = mock(Query.class);
		when(q.eq(TOKEN, "token")).thenReturn(q);
		when(q.findOne()).thenReturn(molgenisToken);
		when(dataService.query(MOLGENIS_TOKEN, MolgenisToken.class)).thenReturn(q);

		UserDetails userDetails = new User("admin", "admin", singletonList(new SimpleGrantedAuthority("admin")));
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		assertEquals(tokenService.findUserByToken("token"), userDetails);
	}

	@Test(expectedExceptions = UnknownTokenException.class)
	public void findUserByTokenExpired()
	{
		MolgenisToken molgenisToken = mock(MolgenisToken.class);
		when(molgenisToken.getToken()).thenReturn("token");
		when(molgenisToken.getExpirationDate()).thenReturn(DateUtils.addDays(new Date(), -1));

		Query<MolgenisToken> q = mock(Query.class);
		when(q.eq(TOKEN, "token")).thenReturn(q);
		when(q.findOne()).thenReturn(molgenisToken);
		when(dataService.query(MOLGENIS_TOKEN, MolgenisToken.class)).thenReturn(q);

		tokenService.findUserByToken("token");
	}

	@Test
	public void generateAndStoreToken()
	{
		MolgenisUser user = mock(MolgenisUser.class);

		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(USERNAME, "admin")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		when(tokenGenerator.generateToken()).thenReturn("token");
		assertEquals(tokenService.generateAndStoreToken("admin", "description"), "token");

		ArgumentCaptor<MolgenisToken> argumentCaptor = ArgumentCaptor.forClass(MolgenisToken.class);
		verify(dataService).add(eq(MOLGENIS_TOKEN), argumentCaptor.capture());
		MolgenisToken molgenisToken = argumentCaptor.getValue();
		verify(molgenisToken).setToken("token");
	}

	@Test
	public void removeToken()
	{
		MolgenisToken molgenisToken = mock(MolgenisToken.class);
		when(molgenisToken.getToken()).thenReturn("token");

		Query<MolgenisToken> q = mock(Query.class);
		when(q.eq(TOKEN, "token")).thenReturn(q);
		when(q.findOne()).thenReturn(molgenisToken);
		when(dataService.query(MOLGENIS_TOKEN, MolgenisToken.class)).thenReturn(q);

		tokenService.removeToken("token");
		verify(dataService).delete(MOLGENIS_TOKEN, molgenisToken);
	}
}
