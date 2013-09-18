package org.molgenis.omx.auth.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import javax.mail.internet.MimeMessage;

import org.mockito.ArgumentCaptor;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
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

@ContextConfiguration
public class AccountServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public AccountService accountService()
		{
			return new AccountService();
		}

		@Bean
		public Database database() throws DatabaseException
		{
			return mock(Database.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{

			MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
			when(molgenisSettings.getProperty("plugin.auth.activation_mode")).thenReturn("user");
			return molgenisSettings;
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
	}

	@Autowired
	private AccountService accountService;

	@Autowired
	private Database database;

	@Autowired
	private JavaMailSender javaMailSender;

	@BeforeMethod
	public void setUp() throws DatabaseException
	{
		reset(database);
		when(
				database.find(MolgenisUser.class, new QueryRule(MolgenisUser.ACTIVE, Operator.EQUALS, false),
						new QueryRule(MolgenisUser.ACTIVATIONCODE, Operator.EQUALS, "123"))).thenReturn(
				Collections.<MolgenisUser> singletonList(new MolgenisUser()));
		when(
				database.find(MolgenisUser.class, new QueryRule(MolgenisUser.ACTIVE, Operator.EQUALS, false),
						new QueryRule(MolgenisUser.ACTIVATIONCODE, Operator.EQUALS, "456"))).thenReturn(
				Collections.<MolgenisUser> emptyList());

		reset(javaMailSender);
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	public void activateUser() throws DatabaseException
	{
		accountService.activateUser("123");

		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(database).update(argument.capture());
		assertTrue(argument.getValue().getActive());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@SuppressWarnings("unchecked")
	@Test
	public void activateUser_invalidActivationCode() throws DatabaseException
	{
		accountService.activateUser("invalid");
		verify(database).find(any(Class.class), any(QueryRule.class), any(QueryRule.class));
		verifyNoMoreInteractions(database);
		verifyNoMoreInteractions(javaMailSender);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void activateUser_alreadyActivated() throws DatabaseException
	{
		accountService.activateUser("456");
		verify(database).find(any(Class.class), any(QueryRule.class), any(QueryRule.class));
		verifyNoMoreInteractions(database);
		verifyNoMoreInteractions(javaMailSender);
	}

	@Test
	public void createUser() throws DatabaseException, URISyntaxException
	{
		MolgenisUser molgenisUser = new MolgenisUser();
		molgenisUser.setEmail("user@molgenis.org");
		accountService.createUser(molgenisUser, new URI("http://molgenis.org/activate"));
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(database).add(argument.capture());
		assertFalse(argument.getValue().getActive());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@Test
	public void resetPassword() throws DatabaseException
	{
		@SuppressWarnings("unchecked")
		Query<MolgenisUser> query = mock(Query.class);
		when(query.eq(MolgenisUser.EMAIL, "user@molgenis.org")).thenReturn(query);
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getPassword()).thenReturn("password");
		when(query.find()).thenReturn(Arrays.asList(molgenisUser));
		when(database.query(MolgenisUser.class)).thenReturn(query);

		accountService.resetPassword("user@molgenis.org");
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(database).update(argument.capture());
		assertNotNull(argument.getValue().getPassword());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}
}
