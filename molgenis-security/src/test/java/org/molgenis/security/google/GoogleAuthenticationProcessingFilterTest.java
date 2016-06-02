package org.molgenis.security.google;

public class GoogleAuthenticationProcessingFilterTest
{
	//	private GoogleAuthenticationProcessingFilter googleAuthenticationProcessingFilter;
	//	private AppSettings appSettings;
	//	private HttpServletRequest request;
	//	private HttpServletResponse response;
	//
	//	@BeforeMethod
	//	public void setUp()
	//	{
	//		appSettings = mock(AppSettings.class);
	//		MolgenisUserDetailsService molgenisUserDetailsService = mock(MolgenisUserDetailsService.class);
	//		DataService dataService = mock(DataService.class);
	//		GooglePublicKeysManager googlePublicKeysManager = mock(GooglePublicKeysManager.class);
	//		googleAuthenticationProcessingFilter = new GoogleAuthenticationProcessingFilter(googlePublicKeysManager,
	//				dataService, molgenisUserDetailsService, appSettings, , );
	//		request = mock(HttpServletRequest.class);
	//		response = mock(HttpServletResponse.class);
	//	}
	//
	//	@Test(expectedExceptions = NullPointerException.class)
	//	public void GoogleAuthenticationProcessingFilter()
	//	{
	//		new GoogleAuthenticationProcessingFilter(null, null, null, null, , );
	//	}
	//
	//	@Test(expectedExceptions = AuthenticationServiceException.class)
	//	public void attemptAuthenticationGoogleSignInDisabled()
	//			throws AuthenticationException, IOException, ServletException
	//	{
	//		when(appSettings.getGoogleSignIn()).thenReturn(false);
	//		googleAuthenticationProcessingFilter.attemptAuthentication(request, response);
	//	}
	//
	//	@Test(expectedExceptions = UnknownTokenException.class)
	//	public void attemptAuthenticationMissingToken() throws AuthenticationException, IOException, ServletException
	//	{
	//		when(appSettings.getGoogleSignIn()).thenReturn(true);
	//		when(request.getParameter(GoogleAuthenticationProcessingFilter.PARAM_ID_TOKEN)).thenReturn(null);
	//		googleAuthenticationProcessingFilter.attemptAuthentication(request, response);
	//	}
}
