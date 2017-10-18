package org.molgenis.security.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.service.exception.EmailAlreadyExistsException;
import org.molgenis.security.core.service.exception.UnknownTokenException;
import org.molgenis.security.core.service.exception.UsernameAlreadyExistsException;
import org.molgenis.security.core.service.impl.UserDetailsServiceImpl;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.settings.AuthenticationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.springframework.http.HttpMethod.POST;

public class GoogleAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(GoogleAuthenticationProcessingFilter.class);

	public static final String GOOGLE_AUTHENTICATION_URL = "/login/google";
	static final String PARAM_ID_TOKEN = "id_token";
	private static final String PROFILE_KEY_GIVEN_NAME = "given_name";
	private static final String PROFILE_KEY_FAMILY_NAME = "family_name";

	private final GooglePublicKeysManager googlePublicKeysManager;
	private final UserDetailsServiceImpl userDetailsService;
	private final AuthenticationSettings authenticationSettings;
	private final UserService userService;

	public GoogleAuthenticationProcessingFilter(GooglePublicKeysManager googlePublicKeysManager,
			UserDetailsServiceImpl userDetailsService, AuthenticationSettings authenticationSettings,
			UserService userService)
	{
		super(new AntPathRequestMatcher(GOOGLE_AUTHENTICATION_URL, POST.toString()));

		setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error"));

		this.googlePublicKeysManager = requireNonNull(googlePublicKeysManager);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.userService = requireNonNull(userService);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		if (!authenticationSettings.getGoogleSignIn())
		{
			throw new AuthenticationServiceException("Google authentication not available");
		}

		String idTokenString = request.getParameter(PARAM_ID_TOKEN);

		if (idTokenString != null)
		{
			// verify token string is valid
			GoogleIdToken idToken;
			try
			{
				idToken = verify(idTokenString);
			}
			catch (GeneralSecurityException e)
			{
				throw new UnknownTokenException(e.getMessage(), e);
			}

			// google token is null implies that verification failed
			if (idToken != null)
			{
				return createAuthentication(idToken.getPayload());
			}
			else
			{
				throw new BadCredentialsException(format("Token [%s] verification failed", idTokenString));
			}
		}
		throw new UnknownTokenException(PARAM_ID_TOKEN + " parameter not present in request.");
	}

	private GoogleIdToken verify(String idTokenString) throws GeneralSecurityException, IOException
	{
		List<String> audience = Collections.singletonList(authenticationSettings.getGoogleAppClientId());
		GoogleIdTokenVerifier googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
				googlePublicKeysManager).setAudience(audience).build();
		return googleIdTokenVerifier.verify(idTokenString);
	}

	private Authentication createAuthentication(Payload payload)
	{
		String email = payload.getEmail();
		if (email == null)
		{
			throw new AuthenticationServiceException(
					"Google URI token is missing required [email] claim, did you forget to specify scope [email]?");
		}
		Boolean emailVerified = payload.getEmailVerified();
		if (emailVerified != null && !emailVerified)
		{
			throw new AuthenticationServiceException("Google account email is not verified");
		}
		String principal = payload.getSubject();

		return runAsSystem(() ->
		{
			User user = userService.findByGoogleAccountIdIfPresent(principal)
								   .orElseGet(() -> userService.connectExistingUser(email, principal).orElseGet(
										   () -> createNewUser(payload, email, principal)));
			if (!user.isActive())
			{
				throw new DisabledException(MolgenisLoginController.ERROR_MESSAGE_DISABLED);
			}
			return new UsernamePasswordAuthenticationToken(user.getUsername(), payload.getAccessTokenHash(),
					userDetailsService.getAuthorities(user));
		});
	}

	private User createNewUser(Payload payload, String email, String principal)
	{
		if (!authenticationSettings.getSignUp())
		{
			throw new AuthenticationServiceException("Google authentication not possible: sign up disabled");
		}

		if (authenticationSettings.getSignUpModeration())
		{
			throw new AuthenticationServiceException("Google authentication not possible: sign up moderation enabled");
		}
		User.Builder builder = User.builder()
								   .username(email)
								   .password(UUID.randomUUID().toString())
								   .email(email)
								   .active(true)
								   .superuser(false)
								   .changePassword(false)
								   .googleAccountId(principal);
		if (payload.containsKey(PROFILE_KEY_GIVEN_NAME))
		{
			builder.firstName(payload.get(PROFILE_KEY_GIVEN_NAME).toString());
		}
		if (payload.containsKey(PROFILE_KEY_FAMILY_NAME))
		{
			builder.lastName(payload.get(PROFILE_KEY_FAMILY_NAME).toString());
		}
		LOG.info("first login for [{}], creating MOLGENIS user", email);
		try
		{
			return userService.add(builder.build());
		}
		catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e)
		{
			throw new AuthenticationServiceException("User already registered", e);
		}
	}

}
