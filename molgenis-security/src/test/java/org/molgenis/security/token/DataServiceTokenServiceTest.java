package org.molgenis.security.token;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.Token;
import org.molgenis.data.security.auth.TokenFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.token.UnknownTokenException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.TokenMetaData.TOKEN;
import static org.molgenis.data.security.auth.TokenMetaData.TOKEN_ATTR;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.data.security.auth.UserMetaData.USERNAME;
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
		TokenFactory tokenFactory = mock(TokenFactory.class);
		when(tokenFactory.create()).thenAnswer(invocation -> mock(Token.class));
		tokenService = new DataServiceTokenService(tokenGenerator, dataService, userDetailsService, tokenFactory);
	}

	@Test
	public void findUserByToken()
	{
		User user = mock(User.class);
		when(user.getUsername()).thenReturn("admin");
		Token token = mock(Token.class);
		when(token.getToken()).thenReturn("token");
		when(token.getUser()).thenReturn(user);

		@SuppressWarnings("unchecked")
		Query<Token> q = mock(Query.class);
		when(q.eq(TOKEN_ATTR, "token")).thenReturn(q);
		when(q.findOne()).thenReturn(token);
		when(dataService.query(TOKEN, Token.class)).thenReturn(q);

		UserDetails userDetails = new org.springframework.security.core.userdetails.User("admin", "admin",
				singletonList(new SimpleGrantedAuthority("admin")));
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

		assertEquals(tokenService.findUserByToken("token"), userDetails);
	}

	@Test(expectedExceptions = UnknownTokenException.class)
	public void findUserByTokenExpired()
	{
		Token token = mock(Token.class);
		when(token.getToken()).thenReturn("token");
		when(token.isExpired()).thenReturn(true);

		@SuppressWarnings("unchecked")
		Query<Token> q = mock(Query.class);
		when(q.eq(TOKEN_ATTR, "token")).thenReturn(q);
		when(q.findOne()).thenReturn(token);
		when(dataService.query(TOKEN, Token.class)).thenReturn(q);

		tokenService.findUserByToken("token");
	}

	@Test
	public void generateAndStoreToken()
	{
		User user = mock(User.class);

		@SuppressWarnings("unchecked")
		Query<User> q = mock(Query.class);
		when(q.eq(USERNAME, "admin")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(USER, User.class)).thenReturn(q);

		when(tokenGenerator.generateToken()).thenReturn("token");
		assertEquals(tokenService.generateAndStoreToken("admin", "description"), "token");

		ArgumentCaptor<Token> argumentCaptor = ArgumentCaptor.forClass(Token.class);
		verify(dataService).add(eq(TOKEN), argumentCaptor.capture());
		Token token = argumentCaptor.getValue();
		verify(token).setToken("token");
	}

	@Test
	public void removeToken()
	{
		Token token = mock(Token.class);
		when(token.getToken()).thenReturn("token");

		@SuppressWarnings("unchecked")
		Query<Token> q = mock(Query.class);
		when(q.eq(TOKEN_ATTR, "token")).thenReturn(q);
		when(q.findOne()).thenReturn(token);
		when(dataService.query(TOKEN, Token.class)).thenReturn(q);

		tokenService.removeToken("token");
		verify(dataService).delete(TOKEN, token);
	}
}
