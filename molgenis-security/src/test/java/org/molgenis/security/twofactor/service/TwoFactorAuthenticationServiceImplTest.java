package org.molgenis.security.twofactor.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretFactory;
import org.molgenis.security.twofactor.model.UserSecretMetaData;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.model.UserSecretMetaData.USER_SECRET;
import static org.testng.Assert.*;

public class TwoFactorAuthenticationServiceImplTest
{
	private User user = User.builder()
							.id("userId")
							.username("user")
							.email("user@example.com")
							.password("password")
							.twoFactorAuthentication(true)
							.build();
	@Mock
	private UserSecret userSecret;
	@Mock
	private OtpService otpService;
	@Mock
	private DataService dataService;
	@Mock
	private UserAccountService userAccountService;
	@Mock
	private UserService userService;
	@Mock
	private IdGenerator idGenerator;
	@Mock
	private Stream<UserSecret> userSecrets;
	@Mock
	private UserSecretFactory userSecretFactory;

	@InjectMocks
	private TwoFactorAuthenticationServiceImpl twoFactorAuthenticationService;

	private MockitoSession mockitoSession;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		twoFactorAuthenticationService = null; // final fields won't be reset by mockito's @InjectMocks
		mockitoSession = Mockito.mockitoSession().strictness(Strictness.STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void testGenerateSecretKey()
	{
		when(idGenerator.generateId(IdGenerator.Strategy.SECURE_RANDOM)).thenReturn("secretKey");

		assertEquals(twoFactorAuthenticationService.generateSecretKey(), "secretKey");
	}

	@Test
	public void testEnableForUser()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user.toBuilder().twoFactorAuthentication(false).build());

		twoFactorAuthenticationService.enableForUser();

		verify(userService).update(user);
	}

	@Test
	public void testSaveSecretForUser()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(userSecretFactory.create()).thenReturn(userSecret);

		twoFactorAuthenticationService.saveSecretForUser("secretKey");

		verify(userSecret).setSecret("secretKey");
		verify(userSecret).setUserId("userId");
		verify(dataService).add(USER_SECRET, userSecret);
	}

	@Test(expectedExceptions = InternalAuthenticationServiceException.class, expectedExceptionsMessageRegExp = "No secretKey found")
	public void testSaveSecretForUserNull()
	{
		twoFactorAuthenticationService.saveSecretForUser(null);
	}

	@Test
	public void testResetSecretForUser()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(dataService.findAll(USER_SECRET, new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, "userId"),
				UserSecret.class)).thenReturn(userSecrets);

		twoFactorAuthenticationService.resetSecretForUser();

		verify(dataService).delete(USER_SECRET, userSecrets);
	}

	@Test
	public void isConfiguredForUserTest()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(dataService.findOne(USER_SECRET, new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, "userId"),
				UserSecret.class)).thenReturn(userSecret);
		when(userSecret.getSecret()).thenReturn("secretKey");

		assertTrue(twoFactorAuthenticationService.isConfiguredForUser());
	}

	@Test(expectedExceptions = TooManyLoginAttemptsException.class)
	public void testUserIsBlocked()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(dataService.findOne(USER_SECRET, new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, "userId"),
				UserSecret.class)).thenReturn(userSecret);
		when(userSecret.getFailedLoginAttempts()).thenReturn(3);
		when(userSecret.hasRecentFailedLoginAttempt(30)).thenReturn(true);

		twoFactorAuthenticationService.userIsBlocked();
	}

	@Test
	public void testUserisBlockedMoreAttemptsLeft()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(dataService.findOne(USER_SECRET, new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, "userId"),
				UserSecret.class)).thenReturn(userSecret);
		when(userSecret.getFailedLoginAttempts()).thenReturn(2);

		assertFalse(twoFactorAuthenticationService.userIsBlocked());
	}

	@Test
	public void testUserisBlockedTimeoutPassed()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(dataService.findOne(USER_SECRET, new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, "userId"),
				UserSecret.class)).thenReturn(userSecret);
		when(userSecret.getFailedLoginAttempts()).thenReturn(3);
		when(userSecret.hasRecentFailedLoginAttempt(30)).thenReturn(false);

		assertFalse(twoFactorAuthenticationService.userIsBlocked());
	}

	@Test
	public void testDisableForUser()
	{
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(dataService.findOne(USER_SECRET, new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, "userId"),
				UserSecret.class)).thenReturn(userSecret);

		twoFactorAuthenticationService.disableForUser();

		verify(userService).update(user.toBuilder().twoFactorAuthentication(false).build());
		verify(dataService).delete(USER_SECRET, userSecret);
	}
}
