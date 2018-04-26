package org.molgenis.security.google;

import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.GroupMemberFactory;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.security.core.token.UnknownTokenException;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.user.UserDetailsService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoogleAuthenticationProcessingFilterTest
{
	private GoogleAuthenticationProcessingFilter googleAuthenticationProcessingFilter;
	private AuthenticationSettings authenticationSettings;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@BeforeMethod
	public void setUp()
	{
		authenticationSettings = mock(AuthenticationSettings.class);
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		DataService dataService = mock(DataService.class);
		GooglePublicKeysManager googlePublicKeysManager = mock(GooglePublicKeysManager.class);
		UserFactory userFactory = mock(UserFactory.class);
		GroupMemberFactory groupMemberFactory = mock(GroupMemberFactory.class);
		googleAuthenticationProcessingFilter = new GoogleAuthenticationProcessingFilter(googlePublicKeysManager,
				dataService, userDetailsService, authenticationSettings, userFactory, groupMemberFactory);
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void GoogleAuthenticationProcessingFilter()
	{
		new GoogleAuthenticationProcessingFilter(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = AuthenticationServiceException.class)
	public void attemptAuthenticationGoogleSignInDisabled()
			throws AuthenticationException, IOException, ServletException
	{
		when(authenticationSettings.getGoogleSignIn()).thenReturn(false);
		googleAuthenticationProcessingFilter.attemptAuthentication(request, response);
	}

	@Test(expectedExceptions = UnknownTokenException.class)
	public void attemptAuthenticationMissingToken() throws AuthenticationException, IOException, ServletException
	{
		when(authenticationSettings.getGoogleSignIn()).thenReturn(true);
		when(request.getParameter(GoogleAuthenticationProcessingFilter.PARAM_ID_TOKEN)).thenReturn(null);
		googleAuthenticationProcessingFilter.attemptAuthentication(request, response);
	}
}
