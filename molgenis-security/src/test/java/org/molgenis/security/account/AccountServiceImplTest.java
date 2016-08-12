package org.molgenis.security.account;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisGroupMemberFactory;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.MolgenisUserService;
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
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisGroupMetaData.NAME;
import static org.molgenis.auth.MolgenisUserMetaData.*;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;
import static org.testng.Assert.assertNotNull;

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

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);
		when(appSettings.getSignUpModeration()).thenReturn(false);

		MolgenisGroup allUsersGroup = mock(MolgenisGroup.class);
		Query<MolgenisGroup> q = mock(Query.class);
		when(q.eq(NAME, ALL_USER_GROUP)).thenReturn(q);
		when(q.findOne()).thenReturn(allUsersGroup);
		when(dataService.query(MOLGENIS_GROUP, MolgenisGroup.class)).thenReturn(q);
		reset(javaMailSender);
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	public void activateUser()
	{
		MolgenisUser user = mock(MolgenisUser.class);
		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(ACTIVE, false)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(ACTIVATIONCODE, "123")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		accountService.activateUser("123");

		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(dataService).update(eq(MOLGENIS_USER), argument.capture());
		verify(user).setActive(true);
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void activateUser_invalidActivationCode()
	{
		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(ACTIVE, false)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(ACTIVATIONCODE, "invalid")).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		accountService.activateUser("invalid");
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void activateUser_alreadyActivated()
	{
		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(ACTIVE, false)).thenReturn(q);
		when(q.and()).thenReturn(q);
		when(q.eq(ACTIVATIONCODE, "456")).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		accountService.activateUser("456");
	}

	@Test
	public void createUser() throws URISyntaxException, UsernameAlreadyExistsException, EmailAlreadyExistsException
	{
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getEmail()).thenReturn("user@molgenis.org");
		accountService.createUser(molgenisUser, "http://molgenis.org/activate");
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(dataService).add(eq(MOLGENIS_USER), argument.capture());
		verify(argument.getValue()).setActive(false);
		// TODO improve test
	}

	@SuppressWarnings("unchecked")
	@Test
	public void resetPassword()
	{
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getPassword()).thenReturn("password");

		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(EMAIL, "user@molgenis.org")).thenReturn(q);
		when(q.findOne()).thenReturn(molgenisUser);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		accountService.resetPassword("user@molgenis.org");
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(dataService).update(eq(MOLGENIS_USER), argument.capture());
		assertNotNull(argument.getValue().getPassword());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void resetPassword_invalidEmailAddress()
	{
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getPassword()).thenReturn("password");

		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(EMAIL, "invalid-user@molgenis.org")).thenReturn(q);
		when(q.findOne()).thenReturn(null);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		accountService.resetPassword("invalid-user@molgenis.org");
	}

	@Test
	public void changePassword()
	{
		MolgenisUser user = mock(MolgenisUser.class);
		when(user.getUsername()).thenReturn("test");
		when(user.getPassword()).thenReturn("oldpass");

		Query<MolgenisUser> q = mock(Query.class);
		when(q.eq(USERNAME, "test")).thenReturn(q);
		when(q.findOne()).thenReturn(user);
		when(dataService.query(MOLGENIS_USER, MolgenisUser.class)).thenReturn(q);

		accountService.changePassword("test", "newpass");

		ArgumentCaptor<MolgenisUser> captor = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(dataService).update(eq(MOLGENIS_USER), captor.capture());
		verify(captor.getValue()).setPassword("newpass");
	}

	@Configuration
	static class Config
	{
		@Bean
		public AccountService accountService()
		{
			return new AccountServiceImpl(dataService(), mailSender(), molgenisUserService(), appSettings(),
					molgenisGroupMemberFactory());
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
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public MolgenisGroupMemberFactory molgenisGroupMemberFactory()
		{
			MolgenisGroupMemberFactory molgenisGroupMemberFactory = mock(MolgenisGroupMemberFactory.class);
			when(molgenisGroupMemberFactory.create()).thenAnswer(new Answer<MolgenisGroupMember>()
			{
				@Override
				public MolgenisGroupMember answer(InvocationOnMock invocationOnMock) throws Throwable
				{
					return mock(MolgenisGroupMember.class);
				}
			});
			return molgenisGroupMemberFactory;
		}
	}
}
