package org.molgenis.security.account;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.Group;
import org.molgenis.auth.GroupMember;
import org.molgenis.auth.GroupMemberFactory;
import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.internet.MimeMessage;
import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.GroupMetaData.GROUP;
import static org.molgenis.auth.GroupMetaData.NAME;
import static org.molgenis.auth.UserMetaData.*;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;

@ContextConfiguration
public class AccountServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AccountService accountService;

	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private IdGenerator idGenerator;

	@BeforeMethod
	public void setUp()
	{
		reset(dataService, idGenerator);
		when(appSettings.getSignUpModeration()).thenReturn(false);

		Group allUsersGroup = mock(Group.class);
		@SuppressWarnings("unchecked")
		Query<Group> q = mock(Query.class);
		when(q.eq(NAME, ALL_USER_GROUP)).thenReturn(q);
		when(q.findOne()).thenReturn(allUsersGroup);
		when(dataService.query(GROUP, Group.class)).thenReturn(q);
		reset(javaMailSender);
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	public void activateUser()
	{
		User user = mock(User.class);
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
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
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
		User user = mock(User.class);
		when(user.getEmail()).thenReturn("user@molgenis.org");
		accountService.createUser(user, "http://molgenis.org/activate");
		ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
		verify(dataService).add(eq(USER), argument.capture());
		verify(argument.getValue()).setActive(false);
		// TODO improve test
	}

	@SuppressWarnings("unchecked")
	@Test
	public void resetPassword()
	{
		User user = mock(User.class);
		when(user.getPassword()).thenReturn("password");
		when(idGenerator.generateId(SHORT_SECURE_RANDOM)).thenReturn("newPassword");

		Query<User> q = mock(Query.class);
		when(q.eq(EMAIL, "user@molgenis.org")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(USER, User.class)).thenReturn(q);

		accountService.resetPassword("user@molgenis.org");
		verify(dataService).update(USER, user);
		verify(user).setPassword("newPassword");
		verify(javaMailSender).send(any(SimpleMailMessage.class));
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
					molgenisGroupMemberFactory(), idGenerator());
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
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
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
			when(groupMemberFactory.create()).thenAnswer(new Answer<GroupMember>()
			{
				@Override
				public GroupMember answer(InvocationOnMock invocationOnMock) throws Throwable
				{
					return mock(GroupMember.class);
				}
			});
			return groupMemberFactory;
		}
	}
}
