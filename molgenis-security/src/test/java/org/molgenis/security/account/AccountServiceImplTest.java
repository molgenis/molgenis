package org.molgenis.security.account;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupMember;
import org.molgenis.data.security.auth.GroupMemberFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

import static org.mockito.Mockito.*;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.GroupMetaData.NAME;
import static org.molgenis.data.security.auth.UserMetaData.*;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;

@ContextConfiguration
public class AccountServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Autowired
	private AccountService accountService;

	@Autowired
	private DataService dataService;

	@Autowired
	private MailSender mailSender;

	@Mock
	private User user;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private AuthenticationSettings authenticationSettings;

	@Autowired
	private IdGenerator idGenerator;

	public AccountServiceImplTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUp()
	{
		when(appSettings.getTitle()).thenReturn("Molgenis title");
		when(authenticationSettings.getSignUpModeration()).thenReturn(false);

		Group allUsersGroup = mock(Group.class);
		@SuppressWarnings("unchecked")
		Query<Group> q = mock(Query.class);
		when(q.eq(NAME, ALL_USER_GROUP)).thenReturn(q);
		when(q.findOne()).thenReturn(allUsersGroup);
		when(dataService.query(GROUP, Group.class)).thenReturn(q);

		when(user.getUsername()).thenReturn("jansenj");
		when(user.getFirstName()).thenReturn("Jan");
		when(user.getMiddleNames()).thenReturn("Piet Hein");
		when(user.getLastName()).thenReturn("Jansen");
		when(user.getEmail()).thenReturn("jan.jansen@activation.nl");
		when(user.getPassword()).thenReturn("password");
	}

	@Test
	public void activateUser()
	{
		@SuppressWarnings("unchecked")
		Query<User> q = mock(Query.class);
		when(q.eq(ACTIVE, false)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(ACTIVATIONCODE, "123")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.activateUser("123");

		ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
		verify(dataService).update(eq(USER), argument.capture());
		verify(user).setActive(true);

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
		@SuppressWarnings("unchecked")
		Query<User> q = mock(Query.class);
		when(q.eq(ACTIVE, false)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(ACTIVATIONCODE, "invalid")).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.activateUser("invalid");
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void activateUser_alreadyActivated()
	{
		@SuppressWarnings("unchecked")
		Query<User> q = mock(Query.class);
		when(q.eq(ACTIVE, false)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(ACTIVATIONCODE, "456")).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.activateUser("456");
	}

	@Test
	public void createUser() throws URISyntaxException, UsernameAlreadyExistsException, EmailAlreadyExistsException
	{
		when(idGenerator.generateId(SECURE_RANDOM)).thenReturn("3541db68-435b-416b-8c2c-cf2edf6ba435");

		accountService.createUser(user, "http://molgenis.org/activate");

		ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
		verify(dataService).add(eq(USER), argument.capture());
		verify(argument.getValue()).setActive(false);

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("jan.jansen@activation.nl");
		expected.setSubject("User registration for Molgenis title");
		expected.setText("User registration for Molgenis title\n" + "User name: jansenj Full name: Jan Jansen\n"
				+ "In order to activate the user visit the following URL:\n"
				+ "http://molgenis.org/activate/3541db68-435b-416b-8c2c-cf2edf6ba435\n\n");

		verify(mailSender).send(expected);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void resetPassword()
	{
		when(idGenerator.generateId(SHORT_SECURE_RANDOM)).thenReturn("newPassword");

		Query<User> q = mock(Query.class);
		when(q.eq(EMAIL, "user@molgenis.org")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.resetPassword("user@molgenis.org");

		verify(dataService).update(USER, user);
		verify(user).setPassword("newPassword");
		when(user.getPassword()).thenReturn("newPassword");

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("jan.jansen@activation.nl");
		expected.setSubject("Your new password request");
		expected.setText("Somebody, probably you, requested a new password for Molgenis title.\n"
				+ "The new password is: newPassword\n"
				+ "Note: we strongly recommend you reset your password after log-in!");
		verify(mailSender).send(expected);
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void resetPassword_invalidEmailAddress()
	{
		User user = mock(User.class);
		when(user.getPassword()).thenReturn("password");

		@SuppressWarnings("unchecked")
		Query<User> q = mock(Query.class);
		when(q.eq(EMAIL, "invalid-user@molgenis.org")).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.resetPassword("invalid-user@molgenis.org");
	}

	@Test
	public void changePassword()
	{
		User user = mock(User.class);
		when(user.getUsername()).thenReturn("test");
		when(user.getPassword()).thenReturn("oldpass");

		@SuppressWarnings("unchecked")
		Query<User> q = mock(Query.class);
		when(q.eq(USERNAME, "test")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.changePassword("test", "newpass");

		verify(dataService).update(USER, user);
		verify(user).setPassword("newpass");
	}

	@Configuration
	static class Config
	{
		@Bean
		public AccountService accountService()
		{
			return new AccountServiceImpl(dataService(), mailSender(), molgenisUserService(), appSettings(),
					authenticationSettings(), molgenisGroupMemberFactory(), idGenerator());
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public AuthenticationSettings authenticationSettings()
		{
			return mock(AuthenticationSettings.class);
		}

		@Bean
		public MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		public UserService molgenisUserService()
		{
			return mock(UserService.class);
		}

		@Bean
		public GroupMemberFactory molgenisGroupMemberFactory()
		{
			GroupMemberFactory groupMemberFactory = mock(GroupMemberFactory.class);
			when(groupMemberFactory.create()).thenAnswer(invocationOnMock -> mock(GroupMember.class));
			return groupMemberFactory;
		}
	}
}
