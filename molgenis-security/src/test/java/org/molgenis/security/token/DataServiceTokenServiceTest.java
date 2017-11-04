package org.molgenis.security.token;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.Token;
import org.molgenis.data.security.model.TokenFactory;
import org.molgenis.data.security.model.UserEntity;
import org.molgenis.data.security.service.impl.DataServiceTokenService;
import org.molgenis.security.core.service.exception.UnknownTokenException;
import org.molgenis.security.core.service.impl.TokenGenerator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.model.TokenMetaData.TOKEN;
import static org.molgenis.data.security.model.TokenMetaData.TOKEN_ATTR;
import static org.molgenis.data.security.model.UserMetadata.USER;
import static org.molgenis.data.security.model.UserMetadata.USERNAME;
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
		UserEntity user = mock(UserEntity.class);
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
		UserEntity user = mock(UserEntity.class);

		@SuppressWarnings("unchecked")
		Query<UserEntity> q = mock(Query.class);
		when(q.eq(USERNAME, "admin")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(USER, UserEntity.class)).thenReturn(q);

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
