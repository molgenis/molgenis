package org.molgenis.security.token;

public class DataServiceTokenServiceTest
{
	//	private DataServiceTokenService tokenService;
	//	private TokenGenerator tokenGenerator;
	//	private DataService dataService;
	//	private UserDetailsService userDetailsService;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		tokenGenerator = mock(TokenGenerator.class);
	//		dataService = mock(DataService.class);
	//		userDetailsService = mock(UserDetailsService.class);
	//		tokenService = new DataServiceTokenService(tokenGenerator, dataService, userDetailsService, );
	//	}
	//
	//	@Test
	//	public void findUserByToken()
	//	{
	//		MolgenisToken molgenisToken = new MolgenisToken();
	//		molgenisToken.setToken("token");
	//		MolgenisUser user = new MolgenisUser();
	//		user.setUsername("admin");
	//		molgenisToken.setMolgenisUser(user);
	//
	//		when(
	//				dataService.findOne(MolgenisTokenMetaData.TAG, new QueryImpl<MolgenisToken>().eq(
	//						MolgenisTokenMetaData.TOKEN, "token"),
	//						MolgenisToken.class)).thenReturn(molgenisToken);
	//
	//		UserDetails userDetails = new User("admin", "admin", Arrays.asList(new SimpleGrantedAuthority("admin")));
	//		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
	//
	//		assertEquals(tokenService.findUserByToken("token"), userDetails);
	//	}
	//
	//	@Test(expectedExceptions = UnknownTokenException.class)
	//	public void findUserByTokenExpired()
	//	{
	//		MolgenisToken molgenisToken = new MolgenisToken();
	//		molgenisToken.setToken("token");
	//		molgenisToken.setExpirationDate(DateUtils.addDays(new Date(), -1));
	//
	//		when(
	//				dataService.findOne(MolgenisTokenMetaData.TAG, new QueryImpl<MolgenisToken>().eq(
	//						MolgenisTokenMetaData.TOKEN, "token"),
	//						MolgenisToken.class)).thenReturn(molgenisToken);
	//
	//		tokenService.findUserByToken("token");
	//	}
	//
	//	@Test
	//	public void generateAndStoreToken()
	//	{
	//		MolgenisUser user = new MolgenisUser();
	//
	//		when(
	//				dataService.findOne(MolgenisUserMetaData.TAG, new QueryImpl<MolgenisUser>().eq(
	//						MolgenisUserMetaData.USERNAME, "admin"),
	//						MolgenisUser.class)).thenReturn(user);
	//
	//		when(tokenGenerator.generateToken()).thenReturn("token");
	//		assertEquals(tokenService.generateAndStoreToken("admin", "description"), "token");
	//
	//		MolgenisToken molgenisToken = new MolgenisToken();
	//		molgenisToken.setToken("token");
	//		verify(dataService).add(MolgenisTokenMetaData.TAG, molgenisToken);
	//	}
	//
	//	@Test
	//	public void removeToken()
	//	{
	//		MolgenisToken molgenisToken = new MolgenisToken();
	//		molgenisToken.setToken("token");
	//
	//		when(
	//				dataService.findOne(MolgenisTokenMetaData.TAG, new QueryImpl<MolgenisToken>().eq(
	//						MolgenisTokenMetaData.TOKEN, "token"),
	//						MolgenisToken.class)).thenReturn(molgenisToken);
	//
	//		tokenService.removeToken("token");
	//		verify(dataService).delete(MolgenisTokenMetaData.TAG, molgenisToken);
	//	}
}
