package org.molgenis.security.account;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.service.exception.EmailAlreadyExistsException;
import org.molgenis.security.core.service.exception.MolgenisUserException;
import org.molgenis.security.core.service.exception.UsernameAlreadyExistsException;
import org.molgenis.security.settings.AuthenticationSettings;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;

public class AccountServiceImplTest
{
	@InjectMocks
	private AccountServiceImpl accountService;
	@Mock
	private UserService userService;
	@Mock
	private MailSender mailSender;
	@Mock
	private User user;
	@Mock
	private AppSettings appSettings;
	@Mock
	private AuthenticationSettings authenticationSettings;
	@Mock
	private IdGenerator idGenerator;
	@Mock
	private RegisterRequest registerRequest;
	private MockitoSession mockito;

	@BeforeMethod
	public void setUp()
	{
		mockito = Mockito.mockitoSession().initMocks(this).strictness(STRICT_STUBS).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockito.finishMocking();
		accountService = null;
	}

	@Test
	public void activateUser()
	{
		when(appSettings.getTitle()).thenReturn("Molgenis title");
		when(userService.activateUserUsingCode("123")).thenReturn(Optional.of(user));
		when(user.getFormattedName()).thenReturn("Jan Jansen");
		when(user.getEmail()).thenReturn("jan.jansen@activation.nl");

		accountService.activateUser("123");

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("jan.jansen@activation.nl");
		expected.setText("Dear Jan Jansen,\n\nyour registration request for Molgenis title was approved.\n"
				+ "Your account is now active.\n");
		expected.setSubject("Your registration request for Molgenis title");
		verify(mailSender).send(expected);
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void activateUser_invalidActivationCode()
	{
		accountService.activateUser("invalid");
	}

	@Test
	public void createUser() throws URISyntaxException, UsernameAlreadyExistsException, EmailAlreadyExistsException
	{
		when(idGenerator.generateId(SECURE_RANDOM)).thenReturn("3541db68-435b-416b-8c2c-cf2edf6ba435");
		when(appSettings.getTitle()).thenReturn("Molgenis title");
		when(authenticationSettings.getSignUpModeration()).thenReturn(false);

		when(registerRequest.getUsername()).thenReturn("jansenj");
		when(registerRequest.getFirstname()).thenReturn("Jan");
		when(registerRequest.getMiddleNames()).thenReturn("Piet Hein");
		when(registerRequest.getLastname()).thenReturn("Jansen");
		when(registerRequest.getEmail()).thenReturn("jan.jansen@activation.nl");
		when(registerRequest.getPassword()).thenReturn("password");

		accountService.register(registerRequest, "http://molgenis.org/activate");

		verify(userService).add(User.builder()
									.username("jansenj")
									.firstName("Jan")
									.middleNames("Piet Hein")
									.lastName("Jansen")
									.email("jan.jansen@activation.nl")
									.password("password")
									.active(false)
									.activationCode("3541db68-435b-416b-8c2c-cf2edf6ba435")
									.build());

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("jan.jansen@activation.nl");
		expected.setSubject("User registration for Molgenis title");
		expected.setText(
				"User registration for Molgenis title\n" + "User name: jansenj Full name: Jan Piet Hein Jansen\n"
						+ "In order to activate the user visit the following URL:\n"
						+ "http://molgenis.org/activate/3541db68-435b-416b-8c2c-cf2edf6ba435\n\n");

		verify(mailSender).send(expected);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void resetPassword()
	{
		when(idGenerator.generateId(SHORT_SECURE_RANDOM)).thenReturn("newPassword");
		when(appSettings.getTitle()).thenReturn("Molgenis title");

		when(userService.findByEmail("user@molgenis.org")).thenReturn(User.builder()
																		  .id("abcde")
																		  .username("jansenj")
																		  .firstName("Jan")
																		  .middleNames("Piet Hein")
																		  .lastName("Jansen")
																		  .email("jan.jansen@activation.nl")
																		  .password("oldPassword")
																		  .active(true)
																		  .changePassword(true)
																		  .build());

		accountService.resetPassword("user@molgenis.org");

		verify(userService).update(User.builder()
									   .id("abcde")
									   .username("jansenj")
									   .firstName("Jan")
									   .middleNames("Piet Hein")
									   .lastName("Jansen")
									   .email("jan.jansen@activation.nl")
									   .password("newPassword")
									   .active(true)
									   .changePassword(true)
									   .build());

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("jan.jansen@activation.nl");
		expected.setSubject("Your new password request");
		expected.setText("Somebody, probably you, requested a new password for Molgenis title.\n"
				+ "The new password is: newPassword\n"
				+ "Note: we strongly recommend you reset your password after log-in!");
		verify(mailSender).send(expected);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void resetPassword_invalidEmailAddress()
	{
		doThrow(new IllegalArgumentException("User with username [invalid-user@molgenis.org] not found.")).when(
				userService).findByEmail("invalid-user@molgenis.org");

		accountService.resetPassword("invalid-user@molgenis.org");
	}

	@Test
	public void changePassword()
	{
		User user = User.builder()
						.id("abcde")
						.username("jansenj")
						.firstName("Jan")
						.middleNames("Piet Hein")
						.lastName("Jansen")
						.email("jan.jansen@activation.nl")
						.password("oldPassword")
						.active(true)
						.changePassword(true)
						.build();
		when(userService.findByUsername("jansenj")).thenReturn(user);

		accountService.changePassword("jansenj", "newpass");

		verify(userService).update(user.toBuilder().changePassword(false).password("newpass").build());
	}
}
