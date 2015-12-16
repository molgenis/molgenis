package org.molgenis.security.google;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.token.UnknownTokenException;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;

public class GoogleAuthenticationProcessingFilterTest
{
	private GoogleAuthenticationProcessingFilter googleAuthenticationProcessingFilter;
	private AppSettings appSettings;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@BeforeMethod
	public void setUp()
	{
		appSettings = mock(AppSettings.class);
		MolgenisUserDetailsService molgenisUserDetailsService = mock(MolgenisUserDetailsService.class);
		DataService dataService = mock(DataService.class);
		GooglePublicKeysManager googlePublicKeysManager = mock(GooglePublicKeysManager.class);
		googleAuthenticationProcessingFilter = new GoogleAuthenticationProcessingFilter(googlePublicKeysManager,
				dataService, molgenisUserDetailsService, appSettings);
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void GoogleAuthenticationProcessingFilter()
	{
		new GoogleAuthenticationProcessingFilter(null, null, null, null);
	}

	@Test(expectedExceptions = AuthenticationServiceException.class)
	public void attemptAuthenticationGoogleSignInDisabled()
			throws AuthenticationException, IOException, ServletException
	{
		when(appSettings.getGoogleSignIn()).thenReturn(false);
		googleAuthenticationProcessingFilter.attemptAuthentication(request, response);
	}

	@Test(expectedExceptions = UnknownTokenException.class)
	public void attemptAuthenticationMissingToken() throws AuthenticationException, IOException, ServletException
	{
		when(appSettings.getGoogleSignIn()).thenReturn(true);
		when(request.getParameter(GoogleAuthenticationProcessingFilter.PARAM_ID_TOKEN)).thenReturn(null);
		googleAuthenticationProcessingFilter.attemptAuthentication(request, response);
	}
}
